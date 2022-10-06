package com.hwf.fruitmall.common;

import com.google.common.collect.Sets;
import com.hwf.fruitmall.exception.FruitMallException;
import com.hwf.fruitmall.exception.FruitMallExceptionEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 常量值
 */
@Component
public class Constant {
    //盐值
    public static final String SALT="8svbsvjkweDF,.03[";

    //当前登录的用户，保存到session中
    public static final String IMOOC_MALL_USER = "imooc_mall_user";

    //文件上传路径，在配置文件中配置

    public static  String FILE_UPLOAD_DIR; //对于静态变量，需要为其设置set方法来赋值，直接将@value注解添加到静态变量上面是无法注入的

    @Value("${file.upload.dir}")
    public void setFileUploadDir(String fileUploadDir){
        FILE_UPLOAD_DIR=fileUploadDir;
    }


    public interface ProductListOrderBy{
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price desc","price asc");
    }


    public interface SaleStatus{
        int notSale=0;   //商品下架状态
        int Sale =1 ;   //商品上架状态
    }

    public interface Cart{
        int UN_CHECKED=0;   //购物车未选中
        int CHECKED =1 ;   //购物车被选中
    }

    public enum OrderStatusEnum{
        CANCELED("用户已取消",0),
        NOT_PAID("用户未付款",10),
        PAID("已付款",20),
        DELIVERED("已发货",30),
        FINISHED("交易完成",40);

        private String value;
        private int code;

        public static OrderStatusEnum codeOf(int code){
            for(OrderStatusEnum orderStatusEnum:values()){
                if(orderStatusEnum.getCode()==code){
                    return orderStatusEnum;
                }
            }
            throw new FruitMallException(FruitMallExceptionEnum.NO_ENUM);
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        OrderStatusEnum(String value, int code) {
            this.value = value;
            this.code = code;
        }
    }

}
