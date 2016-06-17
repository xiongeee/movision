package com.zhuhuibao.business.witkey.mc;

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.zhuhuibao.common.Response;
import com.zhuhuibao.common.util.ShiroUtil;
import com.zhuhuibao.mybatis.constants.service.ConstantService;
import com.zhuhuibao.mybatis.witkey.entity.Cooperation;
import com.zhuhuibao.mybatis.witkey.service.CooperationService;
import com.zhuhuibao.utils.pagination.model.Paging;
import com.zhuhuibao.utils.pagination.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 威客接口
 * Created by cxx on 2016/5/4 0004.
 */
@RestController
@RequestMapping("/rest/witkey/mc/")
public class WitkeyController {
    private static final Logger log = LoggerFactory.getLogger(WitkeyController.class);

    @Autowired
    private CooperationService cooperationService;

    @Autowired
    private ConstantService constantService;

    /**
     * 合作类型(大类，子类)
     */
    @ApiOperation(value="合作类型(大类，子类)",notes="合作类型(大类，子类)",response = Response.class)
    @RequestMapping(value = "sel_witkeyTypeList", method = RequestMethod.GET)
    public Response cooperationType()  {
        Response response = new Response();
        List list = cooperationService.cooperationType();
        response.setData(list);
        return response;
    }

    /**
     * 合作类型(子类)
     */
    @ApiOperation(value="合作类型(子类)",notes="合作类型(子类)",response = Response.class)
    @RequestMapping(value = "sel_subWitkeyTypeList", method = RequestMethod.GET)
    public Response subCooperationType()  {
        Response response = new Response();
        List list = cooperationService.subCooperationType();
        response.setData(list);
        return response;
    }

    /**
     * 项目类别
     */
    @ApiOperation(value="项目类别",notes="项目类别",response = Response.class)
    @RequestMapping(value = "sel_witkeyCategoryList", method = RequestMethod.GET)
    public Response cooperationCategory()  {
        Response response = new Response();
        //项目类别type为9
        String type = "9";
        List<Map<String,String>> list = constantService.findByType(type);
        response.setData(list);
        return response;
    }

    /**
     * 编辑任务
     */
    @ApiOperation(value="编辑任务",notes="编辑任务",response = Response.class)
    @RequestMapping(value = "upd_witkey", method = RequestMethod.POST)
    public Response updateCooperation(Cooperation cooperation)  {
        Response response = new Response();
        if(cooperation.getEndTime()!=null){
            cooperation.setEndTime(cooperation.getEndTime()+" 23:59:59");
        }
        if(cooperation.getPrice()==null){
            cooperation.setPrice(Double.parseDouble("-1"));
        }
        cooperationService.updateCooperation(cooperation);
        return response;
    }

    /**
     * 批量删除任务
     */
    @ApiOperation(value="批量删除任务",notes="批量删除任务",response = Response.class)
    @RequestMapping(value = "del_witkey", method = RequestMethod.POST)
    public Response deleteCooperation(@RequestParam String ids[])  {
        Response response = new Response();
        cooperationService.deleteCooperation(ids);
        return response;
    }

    /**
     * 查询一条任务的信息
     */
    @ApiOperation(value="查询一条任务的信息",notes="查询一条任务的信息",response = Cooperation.class)
    @RequestMapping(value = "sel_witkey", method = RequestMethod.GET)
    public Response queryCooperationInfo(@RequestParam String id)  {
        Response response = new Response();
        Cooperation cooperation = cooperationService.queryCooperationInfoById(id);
        response.setData(cooperation);
        return response;
    }

    /**
     * 查询我发布的任务（分页）
     */
    @ApiOperation(value="查询我发布的任务（后台分页）",notes="查询我发布的任务（后台分页）",response = Response.class)
    @RequestMapping(value = "sel_myWitkeyList", method = RequestMethod.GET)
    public Response findAllMyCooperationByPager(
            @RequestParam(required = false) String pageNo,@RequestParam(required = false) String pageSize,
            @ApiParam(value = "合作标题")@RequestParam(required = false) String title,
            @ApiParam(value = "合作类型")@RequestParam(required = false) String type,
            @ApiParam(value = "审核状态")@RequestParam(required = false) String status
    )  {
        Long createId = ShiroUtil.getCreateID();
        if (StringUtils.isEmpty(pageNo)) {
            pageNo = "1";
        }
        if (StringUtils.isEmpty(pageSize)) {
            pageSize = "10";
        }
        Response response = new Response();
        Paging<Map<String,String>> pager = new Paging<Map<String,String>>(Integer.valueOf(pageNo),Integer.valueOf(pageSize));
        Cooperation cooperation = new Cooperation();
        cooperation.setCreateId(String.valueOf(createId));
        cooperation.setType(type);
        cooperation.setTitle(title);
        cooperation.setStatus(status);
        List<Map<String,String>> cooperationList = cooperationService.findAllCooperationByPager(pager, cooperation);
        pager.result(cooperationList);
        response.setData(pager);
        return response;
    }
}
