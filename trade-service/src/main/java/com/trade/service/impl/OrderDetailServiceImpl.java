package com.trade.service.impl;

import com.api.client.ItemClient;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.trade.domain.po.OrderDetail;
import com.trade.mapper.OrderDetailMapper;
import com.trade.service.IOrderDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单详情表 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2023-05-05
 */
@Service
@RequiredArgsConstructor
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements IOrderDetailService {

    private final ItemClient itemClient;
    @Override
    public void recoverStock(Long orderId) {
        lambdaQuery()
                .eq(OrderDetail::getOrderId, orderId)
                .list()
                .forEach(detail -> itemClient.recoverStock(detail.getItemId(), detail.getNum()));


    }
}
