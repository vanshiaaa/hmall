package com.pay.controller;

import com.hmall.common.exception.BizIllegalException;

import com.hmall.common.utils.BeanUtils;
import com.pay.domain.dto.PayApplyDTO;
import com.pay.domain.dto.PayOrderFormDTO;
import com.pay.domain.vo.PayOrderVO;
import com.pay.enums.PayType;
import com.pay.service.IPayOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "支付相关接口")
@RestController
@RequestMapping("pay-orders")
@RequiredArgsConstructor
public class PayController {

    private final IPayOrderService payOrderService;

    @ApiOperation("生成支付单")
    @PostMapping
    public String applyPayOrder(@RequestBody PayApplyDTO applyDTO){
        if(!PayType.BALANCE.equalsValue(applyDTO.getPayType())){
            // 目前只支持余额支付
            throw new BizIllegalException("抱歉，目前只支持余额支付");
        }
        return payOrderService.applyPayOrder(applyDTO);
    }

    @ApiOperation("尝试基于用户余额支付")
    @ApiImplicitParam(value = "支付单id", name = "id")
    @PostMapping("{id}")
    public void tryPayOrderByBalance(@PathVariable("id") Long id, @RequestBody PayOrderFormDTO payOrderFormDTO){
        payOrderFormDTO.setId(id);
        payOrderService.tryPayOrderByBalance(payOrderFormDTO);
    }

    @ApiOperation("查询支付单")
    @GetMapping
    public List<PayOrderVO> queryPayOrders(){
        return BeanUtils.copyList(payOrderService.list(), PayOrderVO.class);
    }
}
//package com.pay.controller;
//
//import com.hmall.common.exception.BizIllegalException;
//import com.hmall.common.utils.BeanUtils;
//import com.pay.domain.dto.PayApplyDTO;
//import com.pay.domain.dto.PayOrderFormDTO;
//import com.pay.domain.vo.PayOrderVO;
//import com.pay.enums.PayType;
//import com.pay.service.IPayOrderService;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiImplicitParam;
//import io.swagger.annotations.ApiOperation;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.bind.annotation.*;
//
//import javax.validation.Valid;
//import java.util.List;
//
//@Api(tags = "支付相关接口")
//@RestController
//@RequestMapping("pay-orders")
//@RequiredArgsConstructor
//@Slf4j  // 添加日志
//public class PayController {
//
//    private final IPayOrderService payOrderService;
//
//    @ApiOperation("生成支付单")
//    @PostMapping
//    public String applyPayOrder(@RequestBody @Valid PayApplyDTO applyDTO){  // ← 添加 @Valid
//        log.info("接收到的支付参数: bizOrderNo={}, payChannelCode={}, amount={}, payType={}",
//                applyDTO.getBizOrderNo(), applyDTO.getPayChannelCode(),
//                applyDTO.getAmount(), applyDTO.getPayType());
//
//        if(!PayType.BALANCE.equalsValue(applyDTO.getPayType())){
//            // 目前只支持余额支付
//            throw new BizIllegalException("抱歉，目前只支持余额支付");
//        }
//        return payOrderService.applyPayOrder(applyDTO);
//    }
//
//    @ApiOperation("尝试基于用户余额支付")
//    @ApiImplicitParam(value = "支付单id", name = "id")
//    @PostMapping("{id}")
//    public void tryPayOrderByBalance(@PathVariable("id") Long id, @RequestBody @Valid PayOrderFormDTO payOrderFormDTO){
//        payOrderFormDTO.setId(id);
//        payOrderService.tryPayOrderByBalance(payOrderFormDTO);
//    }
//
//    @ApiOperation("查询支付单")
//    @GetMapping
//    public List<PayOrderVO> queryPayOrders(){
//        return BeanUtils.copyList(payOrderService.list(), PayOrderVO.class);
//    }
//}