package com.hwf.fruitmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hwf.fruitmall.exception.FruitMallException;
import com.hwf.fruitmall.exception.FruitMallExceptionEnum;
import com.hwf.fruitmall.model.dao.CategoryMapper;
import com.hwf.fruitmall.model.pojo.Category;
import com.hwf.fruitmall.model.request.AddCategoryReq;
import com.hwf.fruitmall.service.CategoryService;
import com.hwf.fruitmall.model.vo.CategoryVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 *  目录分类实现类
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    CategoryMapper categoryMapper;

    @Override
    public void add(AddCategoryReq addCategoryReq){
        Category category = new Category();
        BeanUtils.copyProperties(addCategoryReq,category); //这里会把两个对象中相同属性和属性名得字段进行拷贝，省区大量得set操作
        Category categoryOld = categoryMapper.selectByName(addCategoryReq.getName());
        Category categoryParent = categoryMapper.selectByPrimaryKey(addCategoryReq.getParentId());

        if(categoryOld!=null){ //说明之前有重名得目录，不允许创建
            throw new FruitMallException(FruitMallExceptionEnum.NAME_EXISTED);
        }
        if(categoryParent==null){ //校验父级id
            throw new FruitMallException(FruitMallExceptionEnum.REQUEST_PARAM_ERROR);
        }

        int count = categoryMapper.insertSelective(category);
        if(count==0)
            throw new FruitMallException(FruitMallExceptionEnum.CREATE_FAILED);
    }

    @Override
    public void update(Category updateCategory){
        if(updateCategory.getName()!=null){
            Category categoryOld = categoryMapper.selectByName(updateCategory.getName());
            if(categoryOld != null && !categoryOld.getId().equals(updateCategory.getId())){
                throw new FruitMallException(FruitMallExceptionEnum.NAME_EXISTED);
            }
            int count = categoryMapper.updateByPrimaryKeySelective(updateCategory);
            if(count==0){
                throw new FruitMallException(FruitMallExceptionEnum.UPDATE_FAILED);
            }
        }
    }

    @Override
    public void delete(Integer id){
        Category categoryOld = categoryMapper.selectByPrimaryKey(id);
        if(categoryOld==null){
            //查不到记录，无法删除，删除失败
            throw new FruitMallException(FruitMallExceptionEnum.DELETE_FAILED);
        }
        int count = categoryMapper.deleteByPrimaryKey(id);
        if(count==0){
            throw new FruitMallException(FruitMallExceptionEnum.DELETE_FAILED);
        }
    }


    @Override
    public PageInfo<Category> listForAdmin(Integer pageNum, Integer pageSize){
        PageHelper.startPage(pageNum,pageSize,"type,order_num");  //先按type作为第一优先级排序，再按照order_num排序

        List<Category> categoryList = categoryMapper.selectList();
        return new PageInfo<>(categoryList);
    }

    @Override
    public List<CategoryVO> listCategoryForCustomer(){
        List<CategoryVO> categoryVOList = new ArrayList<>();
        recursivelyFindCategories(categoryVOList,0);
        return categoryVOList;

    }

    @Override
    public List<CategoryVO> listCategoryForCustomer(Integer parentId){
        List<CategoryVO> categoryVOList = new ArrayList<>();
        recursivelyFindCategories(categoryVOList,parentId);
        return categoryVOList;

    }

    private void recursivelyFindCategories(List<CategoryVO>categoryVOList,Integer parentId){
        //递归获取所有子类别，并组合成为一个目录树
        List<Category> categoryList =  categoryMapper.selectCategoriesByParentId(parentId);
        if(!CollectionUtils.isEmpty(categoryList)){
            for (int i = 0; i < categoryList.size(); i++) {
                Category category = categoryList.get(i);
                CategoryVO categoryVO = new CategoryVO();
                BeanUtils.copyProperties(category,categoryVO);
                categoryVOList.add(categoryVO);

                recursivelyFindCategories(categoryVO.getChildCategory(),categoryVO.getId());
            }
        }

    }


}
