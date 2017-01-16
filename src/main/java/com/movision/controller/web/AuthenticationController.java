package com.movision.controller.web;

import java.io.IOException;

import com.movision.common.pojo.RefundItem;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import com.movision.common.Response;


/**
 * 登录
 *
 * @author zhuangyuhao@20160303
 */
@RestController
public class AuthenticationController {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);

    @ApiOperation(value = "111", notes = "111", response = Response.class)
    @RequestMapping(value = "/rest/web/authc", method = RequestMethod.GET)
    public Response isLogin(@ApiParam(value = "手机号码", required = true) @RequestParam String id) throws IOException {
        Response response = new Response();
        response.setData(id);
        return response;
    }


    @ApiOperation(value = "222", notes = "222", response = Response.class)
    @RequestMapping(value = "/rest/web/authc222", method = RequestMethod.GET)
    public Response isLogin222(@ApiParam @ModelAttribute RefundItem refundItem) throws IOException {

        Response response = new Response();
        refundItem.setOrderNo("lalalalal");

        response.setData(refundItem);
        return response;
    }


}
