package com.hwf.fruitmall.controller;

import com.github.pagehelper.PageInfo;
import com.hwf.fruitmall.common.ApiRestResponse;
import com.hwf.fruitmall.common.Constant;
import com.hwf.fruitmall.exception.FruitMallException;
import com.hwf.fruitmall.exception.FruitMallExceptionEnum;
import com.hwf.fruitmall.model.request.AddProductReq;
import com.hwf.fruitmall.model.request.UpdateProductReq;
import com.hwf.fruitmall.service.ProductService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

/**
 * 后台商品管理controller
 */
@RestController//这里如果是restController,则代表该类下的所有url都自动添加了responsbody注解
public class ProductAdminController {

    @Autowired
    private ProductService productService;

    @PostMapping("admin/product/add")
    @ApiOperation("添加商品接口")
    public ApiRestResponse addProduct(@Valid @RequestBody AddProductReq addProductReq){
        productService.add(addProductReq);
        return ApiRestResponse.success();
    }

    @PostMapping("admin/upload/file")
    @ApiOperation("文件上传接口")
    public ApiRestResponse upload(HttpServletRequest httpServletRequest, @RequestParam("file")MultipartFile file){
        //获取后缀
        String fileName = file.getOriginalFilename(); //文件名
        String suffixName = fileName.substring(fileName.lastIndexOf("."));  //拿到.后缀
        //生成文件名UUID,保证唯一
        UUID uuid = UUID.randomUUID();
        String newFileName = uuid.toString()+suffixName;

        //创建文件
        File fileDirectory = new File(Constant.FILE_UPLOAD_DIR);
        File destFile = new File(Constant.FILE_UPLOAD_DIR+newFileName);

        //文件架不存在
        if(!fileDirectory.exists()){
            if(!fileDirectory.mkdir()){
                throw new FruitMallException(FruitMallExceptionEnum.MKDIR_FAILED);
            }
        }
        try {
            file.transferTo(destFile);//将传入的file写入
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            return ApiRestResponse.success(getHost(new URI(httpServletRequest.getRequestURL()+""))+ "/images/" +newFileName);
        } catch (URISyntaxException e) {
            return ApiRestResponse.error(FruitMallExceptionEnum.UPLOAD_FAILED);
        }
    }

    private URI getHost(URI uri){
        URI effectiveURI;
        try {
            effectiveURI=new URI(uri.getScheme(),uri.getUserInfo(),uri.getHost(),uri.getPort(),null,null,null);
        } catch (URISyntaxException e) {
            effectiveURI=null;
            e.printStackTrace();
        }
        return effectiveURI;
    }

    @PostMapping("/admin/product/update")
    @ApiOperation("后台更新商品")
    public ApiRestResponse updateProduct(@Valid @RequestBody UpdateProductReq updateProductReq){
        productService.update(updateProductReq);
        return ApiRestResponse.success();
    }

    @ApiOperation("后台删除商品")
    @PostMapping("/admin/product/delete")
    public ApiRestResponse deleteProduct(@RequestParam Integer id){

        productService.delete(id);
        return ApiRestResponse.success();
    }

    @ApiOperation("后台批量上下架接口")
    @PostMapping("/admin/product/batchUpdateSellStatus")
    public ApiRestResponse batchUpdateSellStatus(@RequestParam Integer[] ids,@RequestParam Integer sellStatus){
        productService.batchUpdateSellStatus(ids,sellStatus);
        return ApiRestResponse.success();
    }

    @ApiOperation("后台商品列表接口")
    @GetMapping("/admin/product/list")
    public ApiRestResponse list(@RequestParam Integer pageNum,@RequestParam Integer pageSize){
        PageInfo pageInfo = productService.listForAdmin(pageNum,pageSize);
        return ApiRestResponse.success(pageInfo);
    }

}
