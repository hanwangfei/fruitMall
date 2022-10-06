package com.hwf.fruitmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hwf.fruitmall.common.Constant;
import com.hwf.fruitmall.exception.FruitMallException;
import com.hwf.fruitmall.exception.FruitMallExceptionEnum;
import com.hwf.fruitmall.model.dao.ProductMapper;
import com.hwf.fruitmall.model.pojo.Product;
import com.hwf.fruitmall.model.request.AddProductReq;
import com.hwf.fruitmall.model.request.ProductListReq;
import com.hwf.fruitmall.model.request.UpdateProductReq;
import com.hwf.fruitmall.model.query.ProductListQuery;
import com.hwf.fruitmall.service.CategoryService;
import com.hwf.fruitmall.service.ProductService;
import com.hwf.fruitmall.model.vo.CategoryVO;
import com.mysql.cj.util.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品服务实现类
 */
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryService categoryService;

    @Override
    public void add(AddProductReq addProductReq){
        Product product = new Product();
        BeanUtils.copyProperties(addProductReq,product);

        Product productOld = productMapper.selectByName(addProductReq.getName());
        if(productOld!=null){
            throw new FruitMallException(FruitMallExceptionEnum.NAME_EXISTED);
        }
        int count = productMapper.insertSelective(product);
        if(count==0){
            throw new FruitMallException(FruitMallExceptionEnum.INSERT_FAILED);
        }
    }

    @Override
    public void update(UpdateProductReq updateProductReq){
        Product product = new Product();
        BeanUtils.copyProperties(updateProductReq,product);

        Product productOld = productMapper.selectByName(updateProductReq.getName());
        //同名且不同id,不能继续修改
        if(productOld!=null && !productOld.getId().equals(updateProductReq.getId())){
            throw new FruitMallException(FruitMallExceptionEnum.NAME_EXISTED);
        }
        int count = productMapper.updateByPrimaryKeySelective(product);
        if(count==0)
            throw new FruitMallException(FruitMallExceptionEnum.UPDATE_FAILED);
    }


    @Override
    public void delete(Integer id){
        Product productOld = productMapper.selectByPrimaryKey(id);
        //查不到该记录，无法删除
        if(productOld==null)
            throw new FruitMallException(FruitMallExceptionEnum.DELETE_FAILED);
        int count = productMapper.deleteByPrimaryKey(id);
        if(count==0)
            throw new FruitMallException(FruitMallExceptionEnum.DELETE_FAILED);
    }

    @Override
    public void batchUpdateSellStatus(Integer[] ids, Integer sellStatus){
        productMapper.batchUpdateSellStatus(ids,sellStatus);
    }
    @Override
    public PageInfo listForAdmin(Integer pageNum, Integer pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Product> productList = productMapper.selectListForAdmin();
        PageInfo pageInfo = new PageInfo<>(productList);
        return pageInfo;

    }

    @Override
    public Product detail(Integer id){
        return productMapper.selectByPrimaryKey(id);
    }

    @Override
    public PageInfo list(ProductListReq productListReq){
        ProductListQuery productListQuery = new ProductListQuery();

        //搜索处理
        if(!StringUtils.isNullOrEmpty(productListReq.getKeyword())){
            String keyword = new StringBuilder().append("%").append(productListReq.getKeyword()).append("%").toString();
            productListQuery.setKeyword(keyword);
        }
        //处理目录，如果查某个目录下的商品，需要查出该目录以及该目录子目录下的所有商品
        if(productListReq.getCategoryId()!= null){
            List<CategoryVO> categoryVOList = categoryService.listCategoryForCustomer(productListReq.getCategoryId());
            ArrayList<Integer> categoryIds = new ArrayList<>();
            categoryIds.add(productListReq.getCategoryId());
            getCategoryIds(categoryVOList,categoryIds);
            productListQuery.setCategoryIds(categoryIds);

        }

        //排序处理
        String orderBy = productListReq.getOrderBy();
        if(Constant.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)){
            PageHelper.startPage(productListReq.getPageNum(),productListReq.getPageSize(),orderBy);
        }else {
            PageHelper.startPage(productListReq.getPageNum(),productListReq.getPageSize());
            System.out.println("传入的排序规则异常");
        }

        List<Product> productList = productMapper.selectList(productListQuery);
        PageInfo pageInfo = new PageInfo<>(productList);
        return pageInfo;

    }

    private void getCategoryIds(List<CategoryVO> categoryVOList,ArrayList<Integer> categoryIds){
        for(int i =0;i<categoryVOList.size();i++){
            CategoryVO categoryVO = categoryVOList.get(i);
            if(categoryVO!=null){
                categoryIds.add(categoryVO.getId());
                getCategoryIds(categoryVO.getChildCategory(),categoryIds);
            }
        }
    }
}
