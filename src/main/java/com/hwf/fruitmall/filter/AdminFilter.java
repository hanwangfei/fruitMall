package com.hwf.fruitmall.filter;

import com.hwf.fruitmall.common.ApiRestResponse;
import com.hwf.fruitmall.common.Constant;
import com.hwf.fruitmall.exception.FruitMallExceptionEnum;
import com.hwf.fruitmall.model.pojo.Category;
import com.hwf.fruitmall.model.pojo.User;
import com.hwf.fruitmall.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 管理员校验过滤器
 */
public class AdminFilter implements Filter {

    @Autowired
    private UserService userService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override  //校验用户当前状态必须是管理员登录
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute(Constant.IMOOC_MALL_USER);
        if (currentUser == null) {
            servletResponse.setContentType("text/html;charset=utf-8");
            PrintWriter out = ((HttpServletResponse) servletResponse).getWriter();

            out.write("{\n" +
                    "    \"status\": 10007,\n" +
                    "    \"msg\": \"用户未登录\",\n" +
                    "    \"data\": null\n" +
                    "}");
            out.flush();
            out.close();
            return;

        }
        if (userService.checkAdminRole(currentUser)) {//校验是否是管理员
            filterChain.doFilter(servletRequest, servletResponse);

        } else {
            servletResponse.setContentType("text/html;charset=utf-8");
            PrintWriter out = ((HttpServletResponse) servletResponse).getWriter();
            out.write("{\n" +
                    "    \"status\": 10009,\n" +
                    "    \"msg\": \"无管理员权限\",\n" +
                    "    \"data\": null\n" +
                    "}");
            out.flush();
            out.close();
        }


    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
