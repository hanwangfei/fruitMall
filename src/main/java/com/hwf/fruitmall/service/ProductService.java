package com.hwf.fruitmall.service;

import com.github.pagehelper.PageInfo;
import com.hwf.fruitmall.model.pojo.Product;
import com.hwf.fruitmall.model.request.AddProductReq;
import com.hwf.fruitmall.model.request.ProductListReq;
import com.hwf.fruitmall.model.request.UpdateProductReq;

/**
 * 商品service
 */
public interface ProductService {

    void add(AddProductReq addProductReq);

    void update(UpdateProductReq updateProductReq);

    void delete(Integer id);

    void batchUpdateSellStatus(Integer[] ids, Integer sellStatus);

    PageInfo listForAdmin(Integer pageNum, Integer pageSize);

    Product detail(Integer id);

    PageInfo list(ProductListReq productListReq);
}
