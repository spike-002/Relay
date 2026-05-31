package com.spike.relay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.spike.relay.dto.OrderPublishDTO;
import com.spike.relay.entity.DeliveryOrder;
import com.spike.relay.enums.OrderStatusEnum;
import com.spike.relay.mapper.DeliveryOrderMapper;
import com.spike.relay.service.DeliveryOrderService;
import com.spike.relay.service.WalletService;
import com.spike.relay.util.UserContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class DeliveryOrderServiceImpl
        extends ServiceImpl<DeliveryOrderMapper, DeliveryOrder>
        implements DeliveryOrderService {

    @Autowired
    private WalletService walletService;

    /** 留痕照片必须来自本平台存储，防止客户端伪造任意字符串绕过拍照 */
    @org.springframework.beans.factory.annotation.Value("${storage.local.url-prefix}")
    private String storageUrlPrefix;

    private void requireOwnStorageUrl(String url, String errMsg) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException(errMsg);
        }
        if (!url.startsWith(storageUrlPrefix)) {
            throw new IllegalArgumentException("留痕照片无效，请通过平台拍照上传");
        }
    }

    @Override
    public DeliveryOrder publishOrder(OrderPublishDTO dto) {
        DeliveryOrder order = new DeliveryOrder();
        order.setOrderNo(generateOrderNo());
        order.setPublisherId(UserContextHolder.getUserId());
        order.setCabinetName(dto.getPickupCabinet());
        order.setCabinetSlot(dto.getCabinetSlot());
        order.setTargetBuilding(dto.getTargetBuilding());
        order.setTargetAddress(dto.getTargetAddress());
        order.setItemType(dto.getPackageSize());
        order.setFee(dto.getFee() != null ? dto.getFee() : java.math.BigDecimal.ZERO);
        order.setTip(dto.getTip() != null ? dto.getTip() : java.math.BigDecimal.ZERO);
        order.setExpectedTime(dto.getExpectedTime());
        order.setExpectedArrivalTime(dto.getExpectedArrivalTime());
        order.setRemark(dto.getRemark());
        order.setPhone(dto.getPhone());
        order.setStatus(OrderStatusEnum.PENDING.getCode());
        save(order);
        return order;
    }

    @Override
    public List<DeliveryOrder> getHallOrders() {
        return lambdaQuery()
                .eq(DeliveryOrder::getStatus, OrderStatusEnum.PENDING.getCode())
                .orderByDesc(DeliveryOrder::getCreateTime)
                .list();
    }

    @Override
    public List<DeliveryOrder> getMyOrders(String role) {
        Long userId = UserContextHolder.getUserId();
        return lambdaQuery()
                .eq("take".equals(role), DeliveryOrder::getTakerId, userId)
                .eq(!"take".equals(role), DeliveryOrder::getPublisherId, userId)
                .orderByDesc(DeliveryOrder::getCreateTime)
                .list();
    }

    @Override
    public boolean acceptOrder(Long orderId) {
        Long userId = UserContextHolder.getUserId();
        // 乐观更新：仅当订单仍为待接单时才抢占成功，避免并发竞态
        DeliveryOrder update = new DeliveryOrder();
        update.setStatus(OrderStatusEnum.ACCEPTED.getCode());
        update.setTakerId(userId);
        update.setTakenTime(LocalDateTime.now());
        return lambdaUpdate()
                .eq(DeliveryOrder::getId, orderId)
                .eq(DeliveryOrder::getStatus, OrderStatusEnum.PENDING.getCode())
                .update(update);
    }

    @Override
    public boolean takePackage(Long orderId, String pickupPhotoUrl) {
        requireOwnStorageUrl(pickupPhotoUrl, "必须上传快递盒与外卖柜格同框的取件凭证！");
        Long userId = UserContextHolder.getUserId();
        DeliveryOrder order = getById(orderId);
        if (order == null || order.getStatus() != OrderStatusEnum.ACCEPTED.getCode()) {
            return false;
        }
        if (!userId.equals(order.getTakerId())) {
            return false;
        }
        order.setStatus(OrderStatusEnum.DELIVERING.getCode());
        order.setPickupPhotoUrl(pickupPhotoUrl);
        order.setPickedTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        return updateById(order);
    }

    @Override
    public boolean deliverOrder(Long orderId, String deliveryPhotoUrl) {
        requireOwnStorageUrl(deliveryPhotoUrl, "必须上传送达凭证照片！");
        Long userId = UserContextHolder.getUserId();
        DeliveryOrder order = getById(orderId);
        if (order == null || order.getStatus() != OrderStatusEnum.DELIVERING.getCode()) {
            return false;
        }
        if (!userId.equals(order.getTakerId())) {
            return false;
        }
        order.setStatus(OrderStatusEnum.DELIVERED.getCode());
        order.setDeliveryPhotoUrl(deliveryPhotoUrl);
        order.setDeliveredTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        return updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmOrder(Long orderId) {
        Long userId = UserContextHolder.getUserId();
        DeliveryOrder order = getById(orderId);
        if (order == null || order.getStatus() != OrderStatusEnum.DELIVERED.getCode()) {
            return false;
        }
        if (!userId.equals(order.getPublisherId())) {
            return false;
        }
        // 原子状态流转 3->4：条件更新保证并发/重复提交下入账只发生一次
        boolean updated = lambdaUpdate()
                .eq(DeliveryOrder::getId, orderId)
                .eq(DeliveryOrder::getStatus, OrderStatusEnum.DELIVERED.getCode())
                .set(DeliveryOrder::getStatus, OrderStatusEnum.COMPLETED.getCode())
                .set(DeliveryOrder::getConfirmedTime, LocalDateTime.now())
                .set(DeliveryOrder::getUpdateTime, LocalDateTime.now())
                .update();
        if (!updated) {
            return false; // 已被并发确认，避免重复入账
        }
        // 零抽佣：跑腿费 + 小费全额入账给接单人
        BigDecimal fee = order.getFee() != null ? order.getFee() : BigDecimal.ZERO;
        BigDecimal tip = order.getTip() != null ? order.getTip() : BigDecimal.ZERO;
        BigDecimal income = fee.add(tip);
        walletService.income(order.getTakerId(), income, orderId,
                "订单 " + order.getOrderNo() + " 完成入账");
        return true;
    }

    @Override
    public DeliveryOrder getOrderDetail(Long orderId) {
        return getById(orderId);
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "RL" + timestamp + random;
    }
}
