package com.movision.aop;

import com.movision.mybatis.accessLog.entity.AccessLog;
import com.movision.mybatis.accessLog.service.AccessLogService;
import com.movision.shiro.realm.BossRealm;
import com.movision.utils.propertiesLoader.PropertiesLoader;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * 平台访问日志
 *
 * @author zhuangyuhao@20160413
 */

@Component
@Aspect
public class AspectLog {

    @Autowired
    private AccessLogService accessLogService;

    private static final Logger log = LoggerFactory.getLogger(AspectLog.class);

    ThreadLocal<Long> time = new ThreadLocal<>();

//    @Pointcut("!execution(* com.movision.controller.boss.*.query*(..))")
//    public void pointCut(){}

//    @Around(value="!execution(* com.movision.controller.boss.*.query*(..))")
    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping)")//注解方式配置规则和这行冲突，所以只能在代码中逻辑过滤query前缀的方法名
    public Object aroundLog(ProceedingJoinPoint pjp) throws Throwable {
        time.set(System.currentTimeMillis());
        Object result = pjp.proceed();
        Long execTime = System.currentTimeMillis() - time.get();

        long memberId = 0;
        Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession(false);
        if (null != session) {
            //BOSS端session就ShiroBossUser类型；app端session类型就ShiroUser。
            BossRealm.ShiroBossUser principal = (BossRealm.ShiroBossUser) session.getAttribute("bossuser");//app端：appuser;boss端：bossuser//暂时这边只记录BOSS端的所有请求记录--------------------shuxf 2018.01.15
            if (null != principal) {
                memberId = principal.getId();
            }
        }
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String clientIP = request.getHeader("X-Real-IP");
        clientIP = clientIP == null ? "127.0.0.1" : clientIP;
        String httpMethod = request.getMethod();
        String requestURL = request.getRequestURL().toString();
        String queryString = request.getQueryString();
        String userAgent = request.getHeader("User-Agent");
        String[] aa = requestURL.split("/");
        /**
         *  正式环境 http://www.mofo.shop/movision/boss/menu/query_sidebar
         *  测试环境 http://www.51mofo.com/movision/boss/menu/query_sidebar
         *  本地环境 http://localhost:8080/app/waterfall/labelPage
         *          http://localhost:8080/api-docs
         */
        String busitype;    //获取其中的boss, app。用于鉴别请求的端。
        if (clientIP.equals("127.0.0.1")) {
            //本地环境
            busitype = aa[3];
        } else {
            busitype = aa[4];    //第五个
        }

        int id = Integer.parseInt(String.valueOf(memberId));//后台操作的admin_userid
        int index = requestURL.indexOf("query");//访问链接中包含的query所在的索引位置
        int index1 = requestURL.indexOf("search");//访问链接中包含的search所在的索引位置

        //1.是BOSS端操作请求； 2.后台用户id!=1和0即除了admin账号和未知账号(session为空)之外的用户操作； 3.不是query类接口  （同时满足这三点才做记录）
        if (busitype.equals("boss") && id != 1 && id != 0 && index == -1 && index1 == -1) {//暂时这边只记录BOSS端的所有请求记录--------------------shuxf 2018.01.15
            String logMode = PropertiesLoader.getValue("busi.log.mode");
            switch (logMode) {
                case "db":
                    AccessLog accessLog = new AccessLog();
                    accessLog.setMemberid(Integer.parseInt(String.valueOf(memberId)));    //用户id
                    accessLog.setClientip(clientIP);    //客户端ip
                    accessLog.setHttpmethod(httpMethod);    //http请求方法
                    accessLog.setRequesturl(requestURL);    //请求url
                    accessLog.setQuerystring(queryString);  //请求参数
                    accessLog.setUseragent(userAgent);  //用户代理
                    accessLog.setExectime(Integer.parseInt(String.valueOf(execTime)));    //执行日期
                    accessLog.setBusitype(busitype);//业务类型
                    accessLog.setIntime(new Date());
                    int isAdd = accessLogService.insertSelective(accessLog);
                    log.debug("AspectLog增加访问日志->isAdd=" + isAdd);
                    break;
                case "file":
                    log.trace("memberId:[{}]&clientIP:[{}]&httpMethod:[{}]&requestURL:[{}]&queryString[{}]&userAgent[{}]&execTime[{}]&busitype[{}]",
                            new Object[]{memberId, clientIP, httpMethod, requestURL, queryString, userAgent, execTime, busitype});
                    break;
                default:
                    log.error("业务日志配置不正确");
                    break;
            }
        }
        return result;
    }

}