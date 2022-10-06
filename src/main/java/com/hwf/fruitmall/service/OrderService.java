package com.hwf.fruitmall.service;

import com.github.pagehelper.PageInfo;
import com.hwf.fruitmall.model.request.CreateOrderReq;
import com.hwf.fruitmall.model.vo.CartVO;
import com.hwf.fruitmall.model.vo.OrderVO;

import java.util.List;

/**
 * 订单Service
 */
public interface OrderService {


    String create(CreateOrderReq createOrderReq);

    OrderVO detail(String orderNo);

    PageInfo listForCustomer(Integer pageNum, Integer pageSize);

    PageInfo listForAdmin(Integer pageNum, Integer pageSize);

    void cancel(String orderNo);

    String qrcode(String orderNo);

    void pay(String orderNo);

    void delivered(String orderNo);

    //完结
    void finish(String orderNo);
}
