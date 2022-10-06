package com.hwf.fruitmall.service;

import com.github.pagehelper.PageInfo;
import com.hwf.fruitmall.model.pojo.Category;
import com.hwf.fruitmall.model.request.AddCategoryReq;
import com.hwf.fruitmall.model.vo.CategoryVO;

import java.util.List;

/**
 * 目录service
 */
public interface CategoryService {
    void add(AddCategoryReq addCategoryReq);

    void update(Category updateCategory);

    void delete(Integer id);

    PageInfo listForAdmin(Integer pageNum, Integer pageSize);

    List<CategoryVO> listCategoryForCustomer();

    List<CategoryVO> listCategoryForCustomer(Integer parentId);
}
