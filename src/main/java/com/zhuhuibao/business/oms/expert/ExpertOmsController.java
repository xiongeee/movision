package com.zhuhuibao.business.oms.expert;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.zhuhuibao.common.Response;
import com.zhuhuibao.mybatis.memCenter.entity.Achievement;
import com.zhuhuibao.mybatis.memCenter.entity.Dynamic;
import com.zhuhuibao.mybatis.memCenter.entity.Expert;
import com.zhuhuibao.mybatis.memCenter.service.ExpertService;
import com.zhuhuibao.utils.pagination.model.Paging;
import com.zhuhuibao.utils.pagination.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cxx on 2016/5/19 0019.
 */
@RestController
@RequestMapping("/rest/oms")
@Api(value="ExportOms")
public class ExpertOmsController {
    private static final Logger log = LoggerFactory.getLogger(ExpertOmsController.class);

    @Autowired
    private ExpertService expertService;

    @ApiOperation(value="技术成果列表(运营分页)",notes="技术成果列表(运营分页)",response = Response.class)
    @RequestMapping(value = "achievementListOms", method = RequestMethod.GET)
    public Response achievementListOms(@ApiParam(value = "标题")@RequestParam(required = false) String title,
                                       @ApiParam(value = "状态")@RequestParam(required = false)String status,
                                       @ApiParam(value = "系统分类")@RequestParam(required = false) String systemType,
                                       @ApiParam(value = "应用领域")@RequestParam(required = false)String useArea,
                                       @RequestParam(required = false)String pageNo,
                                       @RequestParam(required = false)String pageSize)  {
        Response response = new Response();
        //设定默认分页pageSize
        if (StringUtils.isEmpty(pageNo)) {
            pageNo = "1";
        }
        if (StringUtils.isEmpty(pageSize)) {
            pageSize = "10";
        }
        Paging<Achievement> pager = new Paging<Achievement>(Integer.valueOf(pageNo), Integer.valueOf(pageSize));
        Map<String,Object> map = new HashMap<>();
        //查询传参+
        map.put("systemType",systemType);
        map.put("useArea",useArea);
        map.put("title",title);
        map.put("status",status);
        List<Achievement> achievementList = expertService.findAllAchievementList(pager,map);
        pager.result(achievementList);
        response.setData(pager);
        return response;
    }

    @ApiOperation(value="协会动态列表(运营分页)",notes="协会动态列表(运营分页)",response = Response.class)
    @RequestMapping(value = "dynamicListOms", method = RequestMethod.GET)
    public Response dynamicListOms(@RequestParam(required = false) String title,
                                   @RequestParam(required = false)String status,
                                   @RequestParam(required = false)String pageNo,
                                   @RequestParam(required = false)String pageSize)  {
        Response response = new Response();
        //设定默认分页pageSize
        if (StringUtils.isEmpty(pageNo)) {
            pageNo = "1";
        }
        if (StringUtils.isEmpty(pageSize)) {
            pageSize = "10";
        }
        Paging<Dynamic> pager = new Paging<Dynamic>(Integer.valueOf(pageNo), Integer.valueOf(pageSize));
        Map<String,Object> map = new HashMap<>();
        //查询传参
        map.put("title",title);
        map.put("status",status);
        List<Dynamic> dynamicList = expertService.findAllDynamicList(pager,map);
        pager.result(dynamicList);
        response.setData(pager);
        return response;
    }

    @ApiOperation(value="专家列表(运营分页)",notes="专家列表(运营分页)",response = Response.class)
    @RequestMapping(value = "expertListOms", method = RequestMethod.GET)
    public Response expertListOms(@ApiParam(value = "姓名")@RequestParam(required = false) String name,
                                  @ApiParam(value = "专家类型")@RequestParam(required = false) String expertType,
                                  @ApiParam(value = "状态")@RequestParam(required = false) String status,
                                  @RequestParam(required = false)String pageNo,
                                  @RequestParam(required = false)String pageSize) {
        Response response = new Response();
        //设定默认分页pageSize
        if (StringUtils.isEmpty(pageNo)) {
            pageNo = "1";
        }
        if (StringUtils.isEmpty(pageSize)) {
            pageSize = "10";
        }
        Paging<Expert> pager = new Paging<Expert>(Integer.valueOf(pageNo), Integer.valueOf(pageSize));
        Map<String,Object> map = new HashMap<>();
        //查询传参
        map.put("name",name);
        map.put("expertType",expertType);
        map.put("status",status);
        List<Expert> expertList = expertService.findAllExpertList(pager,map);
        pager.result(expertList);
        response.setData(pager);
        return response;
    }

    @ApiOperation(value="专家回答列表",notes="专家回答列表",response = Response.class)
    @RequestMapping(value = "expertAnswerListOms", method = RequestMethod.GET)
    public Response expertAnswerListOms(@RequestParam(required = false)String pageNo,
                                  @RequestParam(required = false)String pageSize) {
        Response response = new Response();
        //设定默认分页pageSize
        if (StringUtils.isEmpty(pageNo)) {
            pageNo = "1";
        }
        if (StringUtils.isEmpty(pageSize)) {
            pageSize = "10";
        }
        Paging<Map<String,String>> pager = new Paging<Map<String,String>>(Integer.valueOf(pageNo), Integer.valueOf(pageSize));
        List<Map<String,String>> expertAnswerList = expertService.findAllExpertAnswerListOms(pager);
        pager.result(expertAnswerList);
        response.setData(pager);
        return response;
    }
}
