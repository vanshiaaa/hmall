package com.cart.listner;

import com.cart.domain.dto.CartClearDTO;
import com.cart.service.ICartService;
import com.hmall.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.internal.ws.RealWebSocket;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Component
@Slf4j
public class CartClearListener {
    private final ICartService cartService;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = "cart.clear.queue"),
                    exchange = @Exchange(name = "trade.topic"),
                    key = "order.create"
            )
    )
    public void listenClearCart(Collection<Long> itemIds, Message message){
        Long userId = message.getMessageProperties().getHeader("userId");
        UserContext.setUser(userId);

        cartService.removeByItemIds(itemIds);


    }

}
