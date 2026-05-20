package com.trade.service.impl;

import com.api.client.CartClient;
import com.api.client.ItemClient;
import com.api.client.PayClient;
import com.api.dto.OrderDetailDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.common.domain.CartClearDTO;
import com.hmall.common.exception.BadRequestException;
import com.hmall.common.utils.UserContext;
import com.api.dto.ItemDTO;


import com.trade.constant.MQConstants;
import com.trade.domain.dto.OrderFormDTO;
import com.trade.domain.po.Order;
import com.trade.domain.po.OrderDetail;
import com.trade.mapper.OrderMapper;
import com.trade.service.IOrderDetailService;
import com.trade.service.IOrderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2023-05-05
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

//    private final IItemService itemService;
    private final IOrderDetailService detailService;
//    private final ICartService cartService;

    private final CartClient cartClient;
    private final ItemClient itemClient;
    private final RabbitTemplate rabbitTemplate;
    private final PayClient payClient;
    private final IOrderDetailService orderDetailService;
    @Override
    @GlobalTransactional
    public Long createOrder(OrderFormDTO orderFormDTO) {
        // 1.订单数据
        Order order = new Order();
        // 1.1.查询商品
        List<OrderDetailDTO> detailDTOS = orderFormDTO.getDetails();
        // 1.2.获取商品id和数量的Map
        Map<Long, Integer> itemNumMap = detailDTOS.stream()
                .collect(Collectors.toMap(OrderDetailDTO::getItemId, OrderDetailDTO::getNum));
        Set<Long> itemIds = itemNumMap.keySet();
        // 1.3.查询商品
        List<ItemDTO> items = itemClient.queryItemByIds(itemIds);
        if (items == null || items.size() < itemIds.size()) {
            throw new BadRequestException("商品不存在");
        }
        // 1.4.基于商品价格、购买数量计算商品总价：totalFee
        int total = 0;
        for (ItemDTO item : items) {
            total += item.getPrice() * itemNumMap.get(item.getId());
        }
        order.setTotalFee(total);
        // 1.5.其它属性
        order.setPaymentType(orderFormDTO.getPaymentType());
        order.setUserId(UserContext.getUser());
        order.setStatus(1);
        // 1.6.将Order写入数据库order表中
        save(order);

        // 2.保存订单详情
        List<OrderDetail> details = buildDetails(order.getId(), items, itemNumMap);
        detailService.saveBatch(details);

        // 3.清理购物车商品
//        cartClient.removeByItemIds(itemIds);


        try {
            rabbitTemplate.convertAndSend("trade.topic", "order.create", itemIds, message -> {
                message.getMessageProperties().setHeader("userId", UserContext.getUser());
                return message;
            });
        } catch (Exception e) {
            log.error("清理购物车的消息发送失败，用户id：{}，商品id：{}",
                    UserContext.getUser(), itemIds, e);
        }

        // 4.扣减库存
        try {
            itemClient.deductStock(detailDTOS);
        } catch (Exception e) {
            throw new RuntimeException("库存不足！");
        }
        // 5.发送延时消息，检测订单是否支付成功
        rabbitTemplate.convertAndSend(
                MQConstants.DELAY_EXCHANGE_NAME,
                MQConstants.DELAY_ORDER_KEY,
                order.getId(),
                message -> {
                    message.getMessageProperties().setDelay(10000);
                    return message;
                }
        );
        return order.getId();
    }

    @Override
    public void markOrderPaySuccess(Long orderId) {
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(2);
        order.setPayTime(LocalDateTime.now());
        updateById(order);
    }

    @Override
    public void cancelOrder(Long orderId) {
//        Order order = getById(orderId);
//        order.setStatus(5);
//        update(order).
        // 1.修改为已取消
        lambdaUpdate()
                .eq(Order::getId,orderId)
                .set(Order::getStatus,5)
                .update();
        // 2.更新支付单已取消
        payClient.updatePayStatus(orderId);


        // 3.回复库存

        orderDetailService.recoverStock(orderId);


    }

    private List<OrderDetail> buildDetails(Long orderId, List<ItemDTO> items, Map<Long, Integer> numMap) {
        List<OrderDetail> details = new ArrayList<>(items.size());
        for (ItemDTO item : items) {
            OrderDetail detail = new OrderDetail();
            detail.setName(item.getName());
            detail.setSpec(item.getSpec());
            detail.setPrice(item.getPrice());
            detail.setNum(numMap.get(item.getId()));
            detail.setItemId(item.getId());
            detail.setImage(item.getImage());
            detail.setOrderId(orderId);
            details.add(detail);
        }
        return details;
    }
}
