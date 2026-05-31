package com.spike.relay.controller;

import com.spike.relay.common.Result;
import com.spike.relay.dto.OrderPublishDTO;
import com.spike.relay.entity.DeliveryOrder;
import com.spike.relay.service.DeliveryOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class DeliveryOrderController {

    @Autowired
    private DeliveryOrderService deliveryOrderService;

    @PostMapping("/publish")
    public Result<DeliveryOrder> publishOrder(@RequestBody OrderPublishDTO dto) {
        DeliveryOrder order = deliveryOrderService.publishOrder(dto);
        if (order != null && order.getId() != null) {
            return Result.success("发布成功，等待同学接单", order);
        }
        return Result.fail("发布失败，请重试");
    }

    @GetMapping("/detail/{orderId}")
    public Result<DeliveryOrder> getOrderDetail(@PathVariable Long orderId) {
        DeliveryOrder order = deliveryOrderService.getOrderDetail(orderId);
        if (order == null) {
            return Result.fail(404, "订单不存在");
        }
        return Result.success(order);
    }

    @PostMapping("/accept")
    public Result<Void> acceptOrder(@RequestBody Map<String, Object> params) {
        Long orderId = Long.valueOf(params.get("orderId").toString());
        boolean success = deliveryOrderService.acceptOrder(orderId);
        return success ? Result.success("接单成功", null) : Result.fail("接单失败，该订单已被抢占");
    }

    @PostMapping("/take")
    public Result<Void> takePackage(@RequestBody Map<String, Object> params) {
        try {
            Long orderId = Long.valueOf(params.get("orderId").toString());
            String pickupPhotoUrl = (String) params.get("pickupPhotoUrl");
            boolean success = deliveryOrderService.takePackage(orderId, pickupPhotoUrl);
            return success ? Result.success("取件成功，开始配送", null) : Result.fail("取件失败，订单状态异常或无权操作");
        } catch (IllegalArgumentException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @PostMapping("/deliver")
    public Result<Void> deliverOrder(@RequestBody Map<String, Object> params) {
        try {
            Long orderId = Long.valueOf(params.get("orderId").toString());
            String deliveryPhotoUrl = (String) params.get("deliveryPhotoUrl");
            boolean success = deliveryOrderService.deliverOrder(orderId, deliveryPhotoUrl);
            return success ? Result.success("送达确认成功", null) : Result.fail("送达失败，订单状态异常或无权操作");
        } catch (IllegalArgumentException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    @PostMapping("/confirm")
    public Result<Void> confirmOrder(@RequestBody Map<String, Object> params) {
        Long orderId = Long.valueOf(params.get("orderId").toString());
        boolean success = deliveryOrderService.confirmOrder(orderId);
        return success ? Result.success("确认收货成功", null) : Result.fail("确认失败，订单状态异常或无权操作");
    }

    @GetMapping("/list/hall")
    public Result<List<DeliveryOrder>> getHallOrders() {
        return Result.success(deliveryOrderService.getHallOrders());
    }

    @GetMapping("/list/mine")
    public Result<List<DeliveryOrder>> getMyOrders(
            @RequestParam(defaultValue = "publish") String role) {
        return Result.success(deliveryOrderService.getMyOrders(role));
    }
}
