package com.movision.controller.app;

import com.google.gson.Gson;
import com.movision.common.Response;
import com.movision.common.constant.Constants;
import com.movision.common.constant.ImConstant;
import com.movision.common.constant.MsgCodeConstant;
import com.movision.common.constant.SessionConstant;
import com.movision.common.util.ShiroUtil;
import com.movision.exception.BusinessException;
import com.movision.facade.im.ImFacade;
import com.movision.facade.user.AppRegisterFacade;
import com.movision.facade.user.UserFacade;
import com.movision.mybatis.deviceAccid.entity.DeviceAccid;
import com.movision.mybatis.imuser.entity.ImUser;
import com.movision.mybatis.user.entity.RegisterUser;
import com.movision.mybatis.user.entity.User;
import com.movision.mybatis.user.entity.Validateinfo;
import com.movision.shiro.realm.ShiroRealm;
import com.movision.utils.DateUtils;
import com.movision.utils.ValidateUtils;
import com.movision.utils.propertiesLoader.MsgPropertiesLoader;
import com.movision.utils.VerifyCodeUtils;
import com.movision.utils.im.CheckSumBuilder;
import com.movision.utils.propertiesLoader.PropertiesLoader;
import com.movision.utils.sms.SDKSendSms;
import com.taobao.api.ApiException;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

/**
 *
 * @Author zhuangyuhao
 * @Date 2017/2/4 13:58
 */
@RestController
@RequestMapping("app/login")
public class AppLoginController {

    private static Logger log = LoggerFactory.getLogger(AppLoginController.class);

    @Autowired
    private AppRegisterFacade appRegisterFacade;

    @Autowired
    private UserFacade userFacade;

    /**
     * 手机注册账号时发送的验证码
     *
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonGenerationException
     * @throws ApiException
     */
    @ApiOperation(value = "手机注册账号时发送的验证码", notes = "手机注册账号时发送的验证码", response = Response.class)
    @RequestMapping(value = {"/get_mobile_code"}, method = RequestMethod.GET)
    public Response getMobileCode(@ApiParam(value = "验证的手机号") @RequestParam String mobile) throws IOException, ApiException {
        log.debug("获得手机验证码  mobile==" + mobile);
        Response response = new Response();
        if (ValidateUtils.isMobile(mobile)) {
            // 生成随机字串
            String verifyCode = VerifyCodeUtils.generateVerifyCode(Constants.CHECK_MOBILE_CODE_SIZE, VerifyCodeUtils.VERIFY_CODES_DIGIT);
            log.debug("verifyCode == " + verifyCode);

            appRegisterFacade.sendSms(mobile, verifyCode);

            appRegisterFacade.putValidationInfoToSession(mobile, verifyCode, "r");

            response.setCode(200);
            response.setData(verifyCode);
        } else {
            log.error("手机号不正确！");
            response.setCode(400);
            response.setMessage("请输入正确的手机号！");
        }

        return response;
    }


    /**
     * 验证码校验之后：
     * 1 返回生成的token，保存在app端
     * 2 同时把token放入缓存和数据库
     *
     * @param user
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "手机短信验证码注册", notes = "手机短信验证码注册", response = Response.class)
    @RequestMapping(value = {"/registe_by_phone_sms_code"}, method = RequestMethod.POST)
    public Response registeByPhoneSMS(@ApiParam(value = "会员信息") @ModelAttribute RegisterUser user) throws Exception {

        log.debug("登录信息  mobile==" + user.getPhone() + "mobileCheckCode = " + user.getMobileCheckCode());
        Response response = new Response();
        try {

            Subject currentUser = SecurityUtils.getSubject();
            Session session = currentUser.getSession(true);
            String session_phone = (String) session.getAttribute("phone");
            String param_phone = user.getPhone();

            //验证输入错误的手机号正确的验证码登录
            if (!session_phone.equals(param_phone)) {
                response.setCode(400);
                response.setMessage("请输入正确的手机号码");
                return response;
            }

            //校验验证码是否正确
            if (user.getMobileCheckCode() != null) {
                //获取缓存中的登录的用户信息
                Validateinfo validateinfo = (Validateinfo) session.getAttribute("r" + param_phone);
                log.info("【短信验证码登录】获取缓存中的登录的用户信息:" + validateinfo.toString());
                if (null == validateinfo) {
                    response.setCode(400);
                    response.setMessage("session中无当前用户");
                }
                //业务操作
                Map result = appRegisterFacade.validateLoginUser(user, validateinfo, session);
                response.setData(result);
            } else {
                response.setCode(400);
                response.setMessage("请输入手机验证码");
            }
        } catch (Exception e) {
            log.error("注册操作失败>>>", e);
            throw e;
        }

        return response;
    }

    @ApiOperation(value = "注册QQ/微信/微博账号", notes = "注册QQ/微信/微博账号", response = Response.class)
    @RequestMapping(value = {"/registe_by_third_account"}, method = RequestMethod.POST)
    public Response registeByQQ(@ApiParam(value = "第三方登录方式标示。1:QQ， 2:微信， 3:微博 ") @RequestParam Integer flag,
                                @ApiParam(value = "QQ号/微信号/微博号(填对应的openid)") @RequestParam String account,
                                @ApiParam(value = "唯一标示：openid") @RequestParam String openid,
                                @ApiParam(value = "当前设备号") @RequestParam String deviceno,
                                @ApiParam(value = "第三方账号的头像url") @RequestParam String url,
                                @ApiParam(value = "第三方账号的昵称") @RequestParam String nickname,
                                @ApiParam(value = "第三方账号的性别,1男 0女") @RequestParam String sex) throws Exception {
        if (flag == 1) {
            log.debug("【QQ注册】");
        } else if (flag == 2) {
            log.debug("【微信注册】");
        } else {
            log.debug("【微博注册】");
        }
        log.debug("注册qq账号信息：  account==" + account + ", deviceno = " + deviceno + ", openid = " + openid);
        Response response = new Response();
        try {
            Map result = appRegisterFacade.registerQQAccount(flag, account, openid, deviceno, url, nickname, sex);
            response.setData(result);
        } catch (Exception e) {
            log.error("注册操作失败>>>", e);
            throw e;
        }
        return response;
    }

    @ApiOperation(value = "第三方平台账号登录（QQ/Weixin/Weibo）", notes = "第三方平台账号登录（QQ/Weixin/Weibo）", response = Response.class)
    @RequestMapping(value = {"/login_by_third_account"}, method = RequestMethod.POST)
    public Response loginByThirdAccont(@ApiParam(value = "第三方登录方式标示。1:QQ， 2:微信， 3:微博 ") @RequestParam Integer flag,
                                       @ApiParam(value = "QQ/weixin/weibo（填对应的openid）") @RequestParam String account,
                                       @ApiParam(value = "token") @RequestParam String appToken) throws Exception {

        Response response = new Response();
        try {
            User originUser = appRegisterFacade.queryExistThirdAccountAppUser(flag, account);
            if (null == originUser) {
                log.warn("qq账号不存在,请先注册");

                response.setCode(400);
                response.setMessage("qq账号不存在,请先注册");
                response.setMsgCode(MsgCodeConstant.app_account_by_qq_not_exist);
            } else {
                handleLoginProcess(appToken, response, originUser);
            }

        } catch (Exception e) {
            log.error("登录操作失败>>>", e);
            throw e;
        }
        return response;
    }


    /**
     * 该接口用于app端的自动登录，打开app第一个调的接口就是这个
     * 1 若app用户未注册，则会报异常：请发送短信验证码登录
     * 2 若app用户已注册，即已存在，则开始比较appToken和serverToken,若相同，则进入shiro的认证流程,
     * 若认证成功， 则在session中缓存当前app用户，并且删除session中的boss用户，
     * <p>
     * PS：（这里逻辑有点问题，如果一个用户既登录app，又登录boss，怎么办？
     * --其实他们的session不同，所以逻辑没有问题，
     * 因为同一个session当前登录人只能是app用户或者是boss用户的一个。）
     *
     * @param phone
     * @param appToken
     * @return 返回给前台IM注册的信息
     * @throws Exception
     */
    @ApiOperation(value = "APP登录", notes = "APP登录", response = Response.class)
    @RequestMapping(value = {"/login_by_phone"}, method = RequestMethod.POST)
    public Response loginByPhone(@ApiParam(value = "手机号") @RequestParam String phone,
                             @ApiParam(value = "token") @RequestParam String appToken) throws Exception {
        Response response = new Response();
        try {
            //1 校验手机号是否存在
            User user = userFacade.queryUserByPhone(phone);
            if (null == user) {
                //库中无该用户，需要发送短信验证码
                log.warn("手机号不存在,请发送短信验证码登录");

                response.setCode(400);
                response.setMessage("手机号不存在,请发送短信验证码登录");
                response.setMsgCode(MsgCodeConstant.app_user_not_exist);
            } else {
                handleLoginProcess(appToken, response, user);
            }

        } catch (Exception e) {
            log.error("登录操作失败>>>", e);
            throw e;
        }
        return response;
    }

    /**
     * 处理登录过程
     *
     * @param appToken
     * @param response
     * @param user
     */
    private void handleLoginProcess(String appToken, Response response, User user) {
        //2 校验appToken和serverToken非空
        String serverToken = this.validateAppTokenAndServerToken(appToken, response, user);

        //3 appToken和serverToken比较
        if (serverToken.equalsIgnoreCase(appToken)) {

            Subject currentUser = SecurityUtils.getSubject();
            Gson gson = new Gson();
            UsernamePasswordToken token = gson.fromJson(appToken, UsernamePasswordToken.class);

            Map returnMap = new HashedMap();
            //4 开始进入shiro的认证流程
            this.shiroLogin(response, currentUser, token);

            //若shiro获取身份验证信息通过，则进行下面操作
            if (currentUser.isAuthenticated()) {

                //5 验证通过则在session中缓存登录用户信息
                Session session = currentUser.getSession();
                //6 清除session中的boss用户信息
                session.removeAttribute(SessionConstant.BOSS_USER);
                session.setAttribute(SessionConstant.APP_USER, currentUser.getPrincipal());

                log.debug("验证登录接口是否在session中缓存用户id：" + ShiroUtil.getAppUserID());
                log.debug("验证登录接口是否在session中缓存用户信息：" + ShiroUtil.getAppUser());

                //7 返回登录人的信息
                ShiroRealm.ShiroUser appuser = (ShiroRealm.ShiroUser) currentUser.getPrincipal();
                if (null == appuser) {
                    response.setMsgCode(0);
                    response.setMessage("登录失败");
                    returnMap.put("authorized", false);
                } else {
                    response.setMsgCode(1);
                    response.setMessage("登录成功");
                    returnMap.put("authorized", true);
                    returnMap.put("user", appuser);
                }
                response.setData(returnMap);
            } else {
                token.clear();
            }
        } else {
            log.warn("appToken和serverToken不相等");
            response.setCode(400);
            response.setMessage("appToken和serverToken不相等");
            response.setMsgCode(MsgCodeConstant.app_token_not_equal_server_token);
        }
    }

    @ApiOperation(value = "判断app用户是否已经登录", notes = "判断app用户是否已经登录", response = Response.class)
    @RequestMapping(value = "/authc", method = RequestMethod.GET)
    public Response isLogin() throws IOException {
        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession(false);
        Response response = new Response();
        response.setCode(200);
        Map<String, Object> map = new HashMap<String, Object>();

        if (null == session) {
            response.setMsgCode(0);
            response.setMessage("you are rejected!");
            map.put("authorized", false);
        } else {
            ShiroRealm.ShiroUser appuser = ShiroUtil.getAppUser();
            if (null == appuser) {
                response.setMsgCode(0);
                response.setMessage("you are rejected!");
                map.put("authorized", false);
            } else {
                response.setMsgCode(1);
                response.setMessage("welcome you!");
                map.put("authorized", true);
                map.put("user", appuser);
            }
        }
        response.setData(map);
        log.debug("/app/login/authc is called,msgcode=[" + response.getMsgCode() + "],Message=[" + response.getMessage() + "].");
        return response;
    }

    private void shiroLogin(Response response, Subject currentUser, UsernamePasswordToken token) {
        try {
            //登录，即身份验证 , 开始进入shiro的认证流程
            currentUser.login(token);

        } catch (UnknownAccountException e) {
            log.warn("用户名不存在");
            response.setCode(400);
            response.setMessage(MsgPropertiesLoader.getValue(String.valueOf(MsgCodeConstant.app_user_not_exist)));
            response.setMsgCode(MsgCodeConstant.app_user_not_exist);
        } catch (LockedAccountException e) {
            log.warn("帐户状态异常");
            response.setCode(400);
            response.setMessage(MsgPropertiesLoader.getValue(String.valueOf(MsgCodeConstant.app_account_status_error)));
            response.setMsgCode(MsgCodeConstant.app_account_status_error);
        } catch (AuthenticationException e) {
            log.warn("用户名或验证码错误");
            response.setCode(400);
            response.setMessage(MsgPropertiesLoader.getValue(String.valueOf(MsgCodeConstant.app_account_name_error)));
            response.setMsgCode(MsgCodeConstant.app_account_name_error);
        }
    }

    /**
     * 校验app_token和Server_token是否都存在
     *
     * @param appToken
     * @param response
     * @param user
     * @return
     */
    private String validateAppTokenAndServerToken(String appToken, Response response, User user) {
        if (StringUtils.isEmpty(appToken)) {
            log.warn("app本地的token丢失");
            response.setCode(400);
            response.setMessage(MsgPropertiesLoader.getValue(String.valueOf(MsgCodeConstant.app_token_missing)));
            response.setMsgCode(MsgCodeConstant.app_token_missing);
        }

        String serverToken = user.getToken();
        if (StringUtils.isEmpty(serverToken)) {
            log.warn("服务器存储的token丢失");
            response.setCode(400);
            response.setMessage(MsgPropertiesLoader.getValue(String.valueOf(MsgCodeConstant.server_token_missing)));
            response.setMsgCode(MsgCodeConstant.server_token_missing);
        }
        return serverToken;
    }

    @ApiOperation(value = "退出登录", notes = "退出登录", response = Response.class)
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public Response logout() throws IOException {
        SecurityUtils.getSubject().logout();
        return new Response();
    }

    @ApiOperation(value = "新设备打开APP绑定ACCID", notes = "新设备打开APP绑定ACCID", response = Response.class)
    @RequestMapping(value = "/new_device_binding_accid", method = RequestMethod.POST)
    public Response newDeviceBindingAccid(@ApiParam(value = "设备号") @RequestParam String deviceid) throws IOException {
        Response response = new Response();
        //查询是否存在该设备号，如果存在，则不新增了
        /*DeviceAccid deviceAccid = appRegisterFacade.selectByDeviceno(deviceid);
        if (null == deviceAccid) {
            appRegisterFacade.addDeviceAccid(deviceid);
            response.setMessage("绑定设备号成功");
        } else {
            response.setMessage("已经存在该设备号的注册记录");
        }*/

        response.setData(appRegisterFacade.registerImDevice(deviceid, response));

        return response;
    }


}
