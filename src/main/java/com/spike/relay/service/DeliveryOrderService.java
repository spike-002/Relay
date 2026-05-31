package com.spike.relay.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.spike.relay.dto.OrderPublishDTO;
import com.spike.relay.entity.DeliveryOrder;

import java.util.List;

public interface DeliveryOrderService extends IService<DeliveryOrder> {

    DeliveryOrder publishOrder(OrderPublishDTO dto);

    List<DeliveryOrder> getHallOrders();

    /**
     * 查询我的订单
     * @param role publish-我发布的 / take-我接的
     */
    List<DeliveryOrder> getMyOrders(String role);

    boolean acceptOrder(Long orderId);

    boolean takePackage(Long orderId, String pickupPhotoUrl);

    boolean deliverOrder(Long orderId, String deliveryPhotoUrl);

    boolean confirmOrder(Long orderId);

    DeliveryOrder getOrderDetail(Long orderId);
}
