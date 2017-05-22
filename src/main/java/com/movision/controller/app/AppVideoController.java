package com.movision.controller.app;

import com.google.gson.Gson;
import com.movision.common.Response;
import com.movision.common.constant.AliVideoConstant;
import com.movision.facade.apsaraVideo.AliVideoFacade;
import com.movision.utils.CommonUtils;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @Author zhuangyuhao
 * @Date 2017/5/19 16:41
 */
@RestController
@RequestMapping("app/video")
public class AppVideoController {

    @Autowired
    private AliVideoFacade aliVideoFacade;


    @ApiOperation(value = "获取视频播放凭证", notes = "获取视频播放凭证", response = Response.class)
    @RequestMapping(value = "get_video_play_auth", method = RequestMethod.POST)
    public Response getVideoPlayAuth(@ApiParam("vid, ali视频id") @RequestParam String vid) throws Exception {
        Response response = new Response();
        String url = aliVideoFacade.generateRequestUrl(AliVideoConstant.GetVideoPlayAuth, vid);
        Map<String, String> reMap = aliVideoFacade.doGet(url);
        Map result = new HashedMap();
        if (!reMap.isEmpty()) {
            if ("200".equals(reMap.get("status"))) {
                Gson gson = new Gson();
                result = gson.fromJson(reMap.get("result"), Map.class);
            }
        }
        response.setData(result);
        return response;
    }

}