package com.zhuhuibao.web;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.zhuhuibao.utils.ueditor.ActionEnter;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * UEditor上传
 */
@RestController
@RequestMapping("/rest/ueditor")
@Api(value = "ueditor", description = "百度上传")
public class UEditorController {
    private static final Logger log = LoggerFactory.getLogger(UEditorController.class);


    @RequestMapping(value="uploadimage")
    @ApiOperation(value = "百度上传", notes = "百度上传")
    public void uploadimage(HttpServletRequest request, HttpServletResponse response,MultipartFile upfile) throws UnsupportedEncodingException {

        request.setCharacterEncoding("utf-8");
        response.setHeader("Content-Type" , "text/html");
        response.setContentType("text/html;charset=utf-8");

        String rootPath = request.getSession().getServletContext().getRealPath("/");
        PrintWriter out = null;
        try {
            out = response.getWriter();
            out.write(new ActionEnter(request, rootPath,upfile).exec());

        } catch (IOException e) {
            e.printStackTrace();
            log.error("获取输出流异常:" + e.getMessage());
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.flush();
                out.close();
            }
        }
    }
}
