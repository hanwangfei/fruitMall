package com.hwf.fruitmall.controller;

import com.hwf.fruitmall.common.ApiRestResponse;
import com.hwf.fruitmall.common.Constant;
import com.hwf.fruitmall.exception.FruitMallException;
import com.hwf.fruitmall.exception.FruitMallExceptionEnum;
import com.hwf.fruitmall.model.pojo.User;
import com.hwf.fruitmall.service.impl.UserServiceImpl;
import com.mysql.cj.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * 用户控制器
 */
@Controller
public class UserController {
    @Autowired
    UserServiceImpl userService;

    @GetMapping("/test")
    @ResponseBody
    public User personalPage(){
        return userService.getUser();
    }

    @ResponseBody
    @PostMapping("/register")
    public ApiRestResponse register(@RequestParam("userName") String userName, @RequestParam("password") String password) throws FruitMallException {
        if(StringUtils.isNullOrEmpty(userName)){
            return ApiRestResponse.error(FruitMallExceptionEnum.NEED_USER_NAME);
        }
        if(StringUtils.isNullOrEmpty(password)){
            return ApiRestResponse.error(FruitMallExceptionEnum.NEED_PASSWORD);
        }

        //密码长度不能少于8位
        if(password.length()<8){
            return ApiRestResponse.error(FruitMallExceptionEnum.PASSWORD_TOO_SHORT);
        }

        userService.register(userName,password);
        return ApiRestResponse.success();

    }


    @PostMapping("/login")
    @ResponseBody
    public ApiRestResponse login(@RequestParam("userName") String userName, @RequestParam("password") String password, HttpSession session) throws FruitMallException {
        if(StringUtils.isNullOrEmpty(userName)){
            return ApiRestResponse.error(FruitMallExceptionEnum.NEED_USER_NAME);
        }
        if(StringUtils.isNullOrEmpty(password)){
            return ApiRestResponse.error(FruitMallExceptionEnum.NEED_PASSWORD);
        }
        User user = userService.login(userName,password);
        user.setPassword(null);//保存用户信息时，不保存密码
        session.setAttribute(Constant.IMOOC_MALL_USER,user);  //保存到session对象中
        return ApiRestResponse.success(user);
    }

    /**
     * 更新个性签名
     * @param session
     * @param signature
     * @return
     * @throws FruitMallException
     */
    @PostMapping("/user/update")
    @ResponseBody
    public ApiRestResponse updateUserInfo(HttpSession session,@RequestParam String signature) throws FruitMallException {
        User currentUser = (User) session.getAttribute(Constant.IMOOC_MALL_USER);
        if(currentUser==null){
            return ApiRestResponse.error(FruitMallExceptionEnum.NEED_LOGIN);
        }
        User user = new User();
        user.setId(currentUser.getId());
        user.setPersonalizedSignature(signature);
        userService.updateInformation(user);

        return ApiRestResponse.success();
    }

    /**
     * 登出，清除session
     * @param session
     * @return
     */
    @PostMapping("/user/logout")
    @ResponseBody
    public ApiRestResponse logout(HttpSession session){
        session.removeAttribute(Constant.IMOOC_MALL_USER);
        return ApiRestResponse.success();
    }

    /**
     * 管理员登录
     * @param userName
     * @param password
     * @param session
     * @return
     * @throws FruitMallException
     */
    @PostMapping("/adminLogin")
    @ResponseBody
    public ApiRestResponse adminLogin(@RequestParam("userName") String userName, @RequestParam("password") String password, HttpSession session) throws FruitMallException {
        if(StringUtils.isNullOrEmpty(userName)){
            return ApiRestResponse.error(FruitMallExceptionEnum.NEED_USER_NAME);
        }
        if(StringUtils.isNullOrEmpty(password)){
            return ApiRestResponse.error(FruitMallExceptionEnum.NEED_PASSWORD);
        }
        User user = userService.login(userName,password);
        //校验是否是管理员
        if (userService.checkAdminRole(user)) {
            //是管理员，执行操作
            user.setPassword(null);//保存用户信息时，不保存密码
            session.setAttribute(Constant.IMOOC_MALL_USER,user);  //保存到session对象中
            return ApiRestResponse.success(user);
        }else {
            return ApiRestResponse.error(FruitMallExceptionEnum.NEED_ADMIN);
        }

    }

}
