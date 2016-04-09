package com.zhuhuibao.business.oms.system;

import com.zhuhuibao.common.JsonResult;
import com.zhuhuibao.common.ResultBean;
import com.zhuhuibao.mybatis.oms.entity.Category;
import com.zhuhuibao.mybatis.oms.mapper.CategoryMapper;
import com.zhuhuibao.mybatis.oms.service.CategoryService;
import com.zhuhuibao.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 运营中心类目管理
 * Created by cxx on 2016/3/4 0004.
 */
@RestController()
public class SystemController {
    private static final Logger log = LoggerFactory
            .getLogger(SystemController.class);

    @Autowired
    private CategoryService categoryService;
    /**
     * 查询大系统类目
     * @param req
     * @return
     * @throws IOException
     */

    @RequestMapping(value = "/rest/systemSearch", method = RequestMethod.GET)
    public void systemSearch(HttpServletRequest req, HttpServletResponse response) throws IOException {
        JsonResult jsonResult = new JsonResult();
        List<ResultBean> systemList = categoryService.findSystemList();
        jsonResult.setCode(200);
        jsonResult.setData(systemList);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(JsonUtils.getJsonStringFromObj(jsonResult));
    }

    /**
     * 查询大系统下所有子系统类目
     * @param req
     * @return
     * @throws IOException
     */

    @RequestMapping(value = "/rest/subSystemSearch", method = RequestMethod.GET)
    public void subSystemSearch(HttpServletRequest req, HttpServletResponse response) throws IOException {
        JsonResult jsonResult = new JsonResult();
        String parentId = req.getParameter("parentId");
        List<ResultBean> subSystemList = categoryService.findSubSystemList(parentId);
        jsonResult.setCode(200);
        jsonResult.setData(subSystemList);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(JsonUtils.getJsonStringFromObj(jsonResult));
    }

    /**
     * 添加类目
     * @param req
     * @return
     * @throws IOException
     */

    @RequestMapping(value = "/rest/addSystem", method = RequestMethod.POST)
    public void addSystem(HttpServletRequest req, HttpServletResponse response,Category category) throws IOException {
        JsonResult result = new JsonResult();
        int isAdd = categoryService.addSystem(category);
        if(isAdd==0){
            result.setCode(400);
            result.setMessage("添加失败");
        }else{
            result.setCode(200);
        }
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(JsonUtils.getJsonStringFromObj(result));
    }

    /**
     * 编辑类目
     * @param req
     * @return
     * @throws IOException
     */

    @RequestMapping(value = "/rest/updateSystem", method = RequestMethod.POST)
    public void updateSystem(HttpServletRequest req, HttpServletResponse response,Category category) throws IOException {
        JsonResult result = new JsonResult();
        int isUpdate = categoryService.updateSystem(category);
        if(isUpdate==0){
            result.setCode(400);
            result.setMessage("更新失败");
        }else{
            result.setCode(200);
        }
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(JsonUtils.getJsonStringFromObj(result));
    }

    /**
     * 删除类目
     * @param req
     * @return
     * @throws IOException
     */

    @RequestMapping(value = "/rest/deleteSystem", method = RequestMethod.POST)
    public void deleteSystem(HttpServletRequest req, HttpServletResponse response,Category category) throws IOException {
        JsonResult result = new JsonResult();
        int isDelete = categoryService.deleteSystem(category);
        if(isDelete==0){
            result.setCode(400);
            result.setMessage("删除失败");
        }else{
            result.setCode(200);
        }
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(JsonUtils.getJsonStringFromObj(result));
    }
}
