package com.hwf.fruitmall.service.impl;

import com.hwf.fruitmall.common.Constant;
import com.hwf.fruitmall.exception.FruitMallException;
import com.hwf.fruitmall.exception.FruitMallExceptionEnum;
import com.hwf.fruitmall.model.dao.CartMapper;
import com.hwf.fruitmall.model.dao.ProductMapper;
import com.hwf.fruitmall.model.pojo.Cart;
import com.hwf.fruitmall.model.pojo.Product;
import com.hwf.fruitmall.service.CartService;
import com.hwf.fruitmall.model.vo.CartVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 购物车实现类
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CartMapper cartMapper;


    @Override
    public List<CartVO> list(Integer userId){
        List<CartVO> cartVOS = cartMapper.selectList(userId);
        for (CartVO cartVO:cartVOS){   //计算总价
            cartVO.setTotalPrice(cartVO.getPrice()*cartVO.getQuantity());
        }
        return cartVOS;
    }

    @Override
    public List<CartVO> add(Integer userId, Integer productId, Integer count){
        validProduct(productId,count);

        Cart cart = cartMapper.selectCartByUserIdAndProductId(userId,productId);
        if(cart==null){
            //这个商品之前不再购物车，需要新增记录
            cart=new Cart();
            cart.setProductId(productId);
            cart.setUserId(userId);
            cart.setQuantity(count);
            cart.setSelected(Constant.Cart.CHECKED);
            cartMapper.insertSelective(cart);
        }else {
            //这个商品已经在购物车中，则数量相加
            count += cart.getQuantity();
            Cart cartNew = new Cart();
            cartNew.setQuantity(count);
            cartNew.setId(cart.getId());
            cartNew.setProductId(cart.getProductId());
            cartNew.setUserId(cart.getUserId());
            cartNew.setSelected(Constant.Cart.CHECKED);

            cartMapper.updateByPrimaryKeySelective(cartNew);

        }
        return this.list(userId);
    }


    //验证这次添加是否合法
    private void validProduct(Integer productId, Integer count){

        Product product = productMapper.selectByPrimaryKey(productId);
        //判断商品是否存在，是否上架
        if(product==null|| product.getStatus().equals(Constant.SaleStatus.notSale)){
            throw  new FruitMallException(FruitMallExceptionEnum.NOT_SALE);
        }
        //判断商品库存
        if(count>product.getStock()){
            throw new FruitMallException(FruitMallExceptionEnum.NOT_ENOUGH);
        }
    }

    @Override
    public List<CartVO> update(Integer userId, Integer productId, Integer count){
        validProduct(productId,count);

        Cart cart = cartMapper.selectCartByUserIdAndProductId(userId,productId);
        if(cart==null){
            //这个商品之前不再购物车，无法更新
          throw new FruitMallException(FruitMallExceptionEnum.UPDATE_FAILED);
        }else {
            //这个商品已经在购物车中，则更新数量
            Cart cartNew = new Cart();
            cartNew.setQuantity(count);
            cartNew.setId(cart.getId());
            cartNew.setProductId(cart.getProductId());
            cartNew.setUserId(cart.getUserId());
            cartNew.setSelected(Constant.Cart.CHECKED);
            cartMapper.updateByPrimaryKeySelective(cartNew);
        }
        return this.list(userId);
    }

    @Override
    public List<CartVO> delete(Integer userId, Integer productId){
        Cart cart = cartMapper.selectCartByUserIdAndProductId(userId,productId);
        if(cart==null){
            //这个商品之前不再购物车，无法删除
            throw new FruitMallException(FruitMallExceptionEnum.DELETE_FAILED);
        }else {
            //这个商品已经在购物车中，可以删除
           cartMapper.deleteByPrimaryKey(cart.getId());
        }
        return this.list(userId);
    }


    @Override
    public List<CartVO> selectOrNot(Integer userId, Integer productId, Integer selected){
        Cart cart = cartMapper.selectCartByUserIdAndProductId(userId,productId);
        if(cart==null){
            //这个商品之前不再购物车，无法更改状态
            throw new FruitMallException(FruitMallExceptionEnum.UPDATE_FAILED);
        }else {
            //这个商品已经在购物车中，可以操作
            cartMapper.selectOrNot(userId,productId,selected);
        }
        return this.list(userId);
    }

    @Override
    public List<CartVO> selectAllOrNot(Integer userId,Integer selected){
        cartMapper.selectOrNot(userId,null,selected);
        return this.list(userId);
    }


}
