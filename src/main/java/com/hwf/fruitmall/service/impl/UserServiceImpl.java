package com.hwf.fruitmall.service.impl;

import com.hwf.fruitmall.exception.FruitMallException;
import com.hwf.fruitmall.exception.FruitMallExceptionEnum;
import com.hwf.fruitmall.model.dao.UserMapper;
import com.hwf.fruitmall.service.UserService;
import com.hwf.fruitmall.util.MD5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.hwf.fruitmall.model.pojo.User;

import java.security.NoSuchAlgorithmException;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;
    @Override
    public User getUser() {
        return userMapper.selectByPrimaryKey(1);
    }

    @Override
    public void register(String userName, String password) throws FruitMallException {
        //查询用户名是否存在，不允许重名
        User result = userMapper.selectByName(userName);
        if(result!=null){
            throw new FruitMallException(FruitMallExceptionEnum.NAME_EXISTED);
        }
        //写入数据库
        User user = new User();
        user.setUsername(userName);
       // user.setPassword(password);
        try {
            user.setPassword(MD5Utils.getMD5Str(password));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        int count = userMapper.insertSelective(user);
        if(count==0){
            throw new FruitMallException(FruitMallExceptionEnum.INSERT_FAILED);
        }

    }

    @Override
    public User login(String userName, String password) throws FruitMallException {
        String md5Password=null;
        try {
            md5Password = MD5Utils.getMD5Str(password);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        User user = userMapper.selectLogin(userName,md5Password);
        if(user==null){
            throw new FruitMallException(FruitMallExceptionEnum.WRONG_PASSWORD);
        }
        return user;
    }

    @Override
    public void updateInformation(User user) throws FruitMallException {
        //跟新个性签名
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if(updateCount>1){
            throw new FruitMallException(FruitMallExceptionEnum.UPDATE_FAILED);

        }
    }
    @Override
    public boolean checkAdminRole(User user){
        //1是普通用户，2是管理员
        return user.getRole().equals(2);
    }
}
