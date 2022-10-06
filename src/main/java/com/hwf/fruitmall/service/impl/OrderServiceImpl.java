package com.hwf.fruitmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.zxing.WriterException;
import com.hwf.fruitmall.common.Constant;
import com.hwf.fruitmall.exception.FruitMallException;
import com.hwf.fruitmall.exception.FruitMallExceptionEnum;
import com.hwf.fruitmall.filter.UserFilter;
import com.hwf.fruitmall.model.dao.CartMapper;
import com.hwf.fruitmall.model.dao.OrderItemMapper;
import com.hwf.fruitmall.model.dao.OrderMapper;
import com.hwf.fruitmall.model.dao.ProductMapper;
import com.hwf.fruitmall.model.pojo.Order;
import com.hwf.fruitmall.model.pojo.OrderItem;
import com.hwf.fruitmall.model.pojo.Product;
import com.hwf.fruitmall.model.request.CreateOrderReq;
import com.hwf.fruitmall.model.vo.CartVO;
import com.hwf.fruitmall.model.vo.OrderItemVO;
import com.hwf.fruitmall.model.vo.OrderVO;
import com.hwf.fruitmall.service.CartService;
import com.hwf.fruitmall.service.OrderService;
import com.hwf.fruitmall.service.UserService;
import com.hwf.fruitmall.util.OrderCodeFactory;
import com.hwf.fruitmall.util.QRCodeGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 订单service实现类
 */
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    CartService cartService;

    @Autowired
    ProductMapper productMapper;

    @Autowired
    CartMapper cartMapper;

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    OrderItemMapper orderItemMapper;

    @Autowired
    UserService userService;

    @Value("${file.upload.ip}")
    String ip;

    @Override
    //数据库事务
    @Transactional(rollbackFor = Exception.class)  //遇到任何得异常都会回滚
    public String create(CreateOrderReq createOrderReq){
        //拿到用户id
        Integer userId = UserFilter.currentUser.getId();
        //从购物车查询已勾选得商品
        List<CartVO> cartVOList = cartService.list(userId);
        ArrayList<CartVO> cartVoListTemp = new ArrayList<>();

        for (int i = 0; i < cartVOList.size(); i++) {
            CartVO cartVO = cartVOList.get(i);
            if(cartVO.getSelected().equals(Constant.Cart.CHECKED)){
                cartVoListTemp.add(cartVO);
            }

        }
        cartVOList = cartVoListTemp;
        //如果购物车已勾选得为空，报错
        if(CollectionUtils.isEmpty(cartVOList)){
            throw new FruitMallException(FruitMallExceptionEnum.CART_EMPTY);
        }
        //判断商品是否存在，上下架状态，库存
        validSaleStatusAndStock(cartVOList);
        //吧购物车对象转换为订单item对象
        List<OrderItem> orderItemList = cartVoListToOrderItemList(cartVOList);

        //扣库存
        for (int i = 0; i < orderItemList.size(); i++) {
            OrderItem orderItem = orderItemList.get(i);
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            int stock = product.getStock()-orderItem.getQuantity();
            if(stock<0){
                throw new FruitMallException(FruitMallExceptionEnum.NOT_ENOUGH);
            }
            product.setStock(stock);
            productMapper.updateByPrimaryKeySelective(product);
        }
        //把购物车中已勾选得商品删除
        cleanCart(cartVOList);
        //生成订单，订单号
        Order order = new Order();
        String orderNum = OrderCodeFactory.getOrderCode(Long.valueOf(userId));
        order.setOrderNo(orderNum);
        order.setUserId(userId);
        order.setTotalPrice(totalPrice(orderItemList));
        order.setReceiverName(createOrderReq.getReceiverName());
        order.setReceiverAddress(createOrderReq.getReceiverAddress());
        order.setReceiverMobile(createOrderReq.getReceiverMobile());

        order.setOrderStatus(Constant.OrderStatusEnum.NOT_PAID.getCode());
        order.setPostage(0);
        order.setPaymentType(1);
        //插入到order表中
        orderMapper.insertSelective(order);

        //循环保存每个商品到orderItem表
        for (int i = 0; i < orderItemList.size(); i++) {
            OrderItem orderItem = orderItemList.get(i);
            orderItem.setOrderNo(order.getOrderNo());
            orderItemMapper.insertSelective(orderItem);


        }
        //吧结果返回
        return orderNum;
    }

    private Integer totalPrice(List<OrderItem> orderItemList) {
        Integer totalPrice = 0;
        for (int i = 0; i < orderItemList.size(); i++) {
            OrderItem orderItem=orderItemList.get(i);
            totalPrice+=orderItem.getTotalPrice();
        }
        return totalPrice;
    }

    private void cleanCart(List<CartVO> cartVOList) {
        for (int i = 0; i < cartVOList.size(); i++) {
            CartVO cartVO = cartVOList.get(i);
            cartMapper.deleteByPrimaryKey(cartVO.getId());
        }
    }

    private List<OrderItem> cartVoListToOrderItemList(List<CartVO> cartVOList) {
        List<OrderItem> orderItemList = new ArrayList<>();
        for (int i = 0; i < cartVOList.size(); i++) {
            CartVO cartVO = cartVOList.get(i);

            OrderItem orderItem= new OrderItem();
            orderItem.setProductId(cartVO.getProductId());
            //记录商品快照信息
            orderItem.setProductName(cartVO.getProductName());
            orderItem.setProductImg(cartVO.getProductImage());
            orderItem.setUnitPrice(cartVO.getPrice());
            orderItem.setQuantity(cartVO.getQuantity());
            orderItem.setTotalPrice(cartVO.getTotalPrice());
            orderItemList.add(orderItem);
        }
        return orderItemList;

    }

    private void validSaleStatusAndStock(List<CartVO> cartVOList) {

        for (int i = 0; i < cartVOList.size(); i++) {
            CartVO cartVO = cartVOList.get(i);

            Product product = productMapper.selectByPrimaryKey(cartVO.getProductId());
            //判断商品是否存在，是否上架
            if(product==null|| product.getStatus().equals(Constant.SaleStatus.notSale)){
                throw  new FruitMallException(FruitMallExceptionEnum.NOT_SALE);
            }
            //判断商品库存
            if(cartVO.getQuantity()>product.getStock()){
                throw new FruitMallException(FruitMallExceptionEnum.NOT_ENOUGH);
            }

        }

    }

    @Override
    public OrderVO detail(String orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        //订单是否存在
        if(order==null){
            throw new FruitMallException(FruitMallExceptionEnum.NO_ORDER);
        }

        //如果订单存在，需要判断所属是否该用户
        Integer userId = UserFilter.currentUser.getId();
        if(!order.getUserId().equals(userId)){
            throw new FruitMallException(FruitMallExceptionEnum.NOT_YOUR_ORDER);
        }
        return getOrderVo(order);
    }

    private OrderVO getOrderVo(Order order) {
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order,orderVO);
        //获取订单对应的orderItemVoList
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(order.getOrderNo());
        List<OrderItemVO> orderItemVOList = new ArrayList<>();
        for (int i = 0; i < orderItemList.size(); i++) {
            OrderItem orderItem= orderItemList.get(i);
            OrderItemVO orderItemVO = new OrderItemVO();
            BeanUtils.copyProperties(orderItem,orderItemVO);
            orderItemVOList.add(orderItemVO);
        }
        orderVO.setOrderItemVOList(orderItemVOList);
        orderVO.setOrderStatusName(Constant.OrderStatusEnum.codeOf(orderVO.getOrderStatus()).getValue());
        return orderVO;
    }



    @Override
    public PageInfo listForCustomer(Integer pageNum, Integer pageSize){
        Integer userId = UserFilter.currentUser.getId();
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.selectForCustomer(userId);
        List<OrderVO> orderVOList = orderListToOrderVoList(orderList);
        PageInfo pageInfo = new PageInfo<>(orderList);
        pageInfo.setList(orderVOList);
        return pageInfo;
    }

    @Override
    public PageInfo listForAdmin(Integer pageNum, Integer pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.selectAllForAdmin();
        List<OrderVO> orderVOList = orderListToOrderVoList(orderList);
        PageInfo pageInfo = new PageInfo<>(orderList);
        pageInfo.setList(orderVOList);
        return pageInfo;
    }

    private List<OrderVO> orderListToOrderVoList(List<Order> orderList) {
        List<OrderVO> orderVOList = new ArrayList<>();
        for (int i = 0; i < orderList.size(); i++) {
            Order order = orderList.get(i);
            orderVOList.add(getOrderVo(order));
        }
        return orderVOList;
    }


    @Override
    public void cancel(String orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order==null)
            throw new FruitMallException(FruitMallExceptionEnum.NO_ORDER);

        Integer userId = UserFilter.currentUser.getId();
        if(!userId.equals(order.getUserId()))
            throw new FruitMallException(FruitMallExceptionEnum.NOT_YOUR_ORDER);

        //取消操作,只有未付款才允许取消，否则需要联系管理员进行订单拦截
        if(order.getOrderStatus().equals(Constant.OrderStatusEnum.NOT_PAID.getCode())){
            order.setOrderStatus(Constant.OrderStatusEnum.CANCELED.getCode());
            order.setEndTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
        }else{
            throw new FruitMallException(FruitMallExceptionEnum.WRONG_ORDER_STATUS);

        }

    }

    @Override
    public String qrcode(String orderNo){
        //获得ip
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

//        //在同一局域网下可以用手机或其他设备访问该二维码对应网址
//        try {
//            ip = InetAddress.getLocalHost().getHostAddress();
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
        String address = ip + ":" + request.getLocalPort();

        String payUrl = "http://" +address+"/pay?orderNo="+orderNo;
        try {
            QRCodeGenerator.generateQRCodeImage(payUrl,350,350,Constant.FILE_UPLOAD_DIR+orderNo+".png");
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String pngAddress = "http://"+address+"/images/"+orderNo+".png";
        return pngAddress;

    }


    @Override
    public void pay(String orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order==null)
            throw new FruitMallException(FruitMallExceptionEnum.NO_ORDER);

        if(order.getOrderStatus()== Constant.OrderStatusEnum.NOT_PAID.getCode()){
            order.setOrderStatus(Constant.OrderStatusEnum.PAID.getCode());
            order.setPayTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
        }else {
            throw new FruitMallException(FruitMallExceptionEnum.WRONG_ORDER_STATUS);
        }

    }


    //发货
    @Override
    public void delivered(String orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order==null)
            throw new FruitMallException(FruitMallExceptionEnum.NO_ORDER);

        if(order.getOrderStatus()== Constant.OrderStatusEnum.PAID.getCode()){
            order.setOrderStatus(Constant.OrderStatusEnum.DELIVERED.getCode());
            order.setDeliveryTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
        }else {
            throw new FruitMallException(FruitMallExceptionEnum.WRONG_ORDER_STATUS);
        }

    }

    //完结订单
    @Override
    public void finish(String orderNo){
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order==null)
            throw new FruitMallException(FruitMallExceptionEnum.NO_ORDER);

        //如果是普通用户，则只能完结自己的订单，而管理员却可以完结所有人的订单
        if(!userService.checkAdminRole(UserFilter.currentUser) && !order.getUserId().equals(UserFilter.currentUser.getId())){
            throw new FruitMallException(FruitMallExceptionEnum.NOT_YOUR_ORDER);
        }

        //发货后可以完结订单
        if(order.getOrderStatus()== Constant.OrderStatusEnum.DELIVERED.getCode()){
            order.setOrderStatus(Constant.OrderStatusEnum.FINISHED.getCode());
            order.setEndTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
        }else {
            throw new FruitMallException(FruitMallExceptionEnum.WRONG_ORDER_STATUS);
        }
    }






}
