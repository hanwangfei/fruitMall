package com.hwf.fruitmall.service;

import com.hwf.fruitmall.exception.FruitMallException;
import com.hwf.fruitmall.model.pojo.User;

public interface UserService {
    User getUser();

    void register(String userName,String password) throws FruitMallException;

    User login(String userName, String password) throws FruitMallException;

    void updateInformation(User user) throws FruitMallException;

    boolean checkAdminRole(User user);
}
