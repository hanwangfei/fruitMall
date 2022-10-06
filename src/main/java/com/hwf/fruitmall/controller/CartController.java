package com.hwf.fruitmall.controller;

import com.hwf.fruitmall.common.ApiRestResponse;
import com.hwf.fruitmall.filter.UserFilter;
import com.hwf.fruitmall.model.vo.CartVO;
import com.hwf.fruitmall.service.CartService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车controller
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    CartService cartService;

    @GetMapping("/list")
    @ApiOperation("购物车列表")
    public ApiRestResponse list(){
        //内部获取用户id,防止横向越权
        List<CartVO> cartVOS = cartService.list(UserFilter.currentUser.getId());
        return ApiRestResponse.success(cartVOS);
    }

    @PostMapping("/add")
    @ApiOperation("添加商品到购物车")
    public ApiRestResponse add(@RequestParam Integer productId, @RequestParam Integer count){
        List<CartVO> cartVOS = cartService.add( UserFilter.currentUser.getId(),productId,count);
        return ApiRestResponse.success(cartVOS);
    }

    @PostMapping("/update")
    @ApiOperation("更新购物车")
    public ApiRestResponse update(@RequestParam Integer productId, @RequestParam Integer count){
        List<CartVO> cartVOS = cartService.update( UserFilter.currentUser.getId(),productId,count);
        return ApiRestResponse.success(cartVOS);
    }

    @PostMapping("/delete")
    @ApiOperation("删除购物车")
    public ApiRestResponse delete(@RequestParam Integer productId){
        //不能传入userId,cartId,否则可以删除别人的购物车
        List<CartVO> cartVOS = cartService.delete( UserFilter.currentUser.getId(),productId);
        return ApiRestResponse.success(cartVOS);
    }

    @PostMapping("/select")
    @ApiOperation("选中/全不选中购物车的某商品")
    public ApiRestResponse select(@RequestParam Integer productId,@RequestParam Integer selected){

        List<CartVO> cartVOS = cartService.selectOrNot( UserFilter.currentUser.getId(),productId,selected);
        return ApiRestResponse.success(cartVOS);
    }

    @PostMapping("/selectAll")
    @ApiOperation("全选中/全不选中购物车的某商品")
    public ApiRestResponse selectAll(@RequestParam Integer productId,@RequestParam Integer selected){

        List<CartVO> cartVOS = cartService.selectAllOrNot( UserFilter.currentUser.getId(),selected);
        return ApiRestResponse.success(cartVOS);
    }
}
