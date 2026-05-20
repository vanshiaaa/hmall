package com.cart.domain.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel(description = "清除购物车商品表单实体")
public class CartClearDTO implements Serializable {
    private Long userId;
    private List<Long> itemIds;
}
