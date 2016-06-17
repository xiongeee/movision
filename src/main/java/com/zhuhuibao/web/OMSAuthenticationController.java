package com.zhuhuibao.web;

import com.zhuhuibao.common.Response;
import com.zhuhuibao.mybatis.oms.service.UserService;
import com.zhuhuibao.security.resubmit.TokenHelper;
import com.zhuhuibao.shiro.realm.OMSRealm;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录
 * @author caijl@20160303
 */
@RestController
public class OMSAuthenticationController {
    private static final Logger log = LoggerFactory.getLogger(OMSAuthenticationController.class);

    @Autowired
	private UserService userService;
    
    @RequestMapping(value = "/rest/oms/authc", method = RequestMethod.GET)
    public Response isLogin() throws IOException{
        Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession(false);
        Response response = new Response();
        response.setCode(200);
		Map<String, Object> map = new HashMap<String, Object>();

        if(null == session){
        	response.setMsgCode(0);
            response.setMessage("you are rejected!");
            map.put("authorized", false);
        }else{
            OMSRealm.ShiroOmsUser principal = (OMSRealm.ShiroOmsUser)session.getAttribute("oms");
        	if(null == principal){
            	response.setMsgCode(0);
                response.setMessage("you are rejected!");
                map.put("authorized", false);
        	}else{
                LoginUser user = new LoginUser();
                user.setAccount(principal.getAccount());
                user.setId(principal.getId());
            	response.setMsgCode(1);
                response.setMessage("welcome you!");
                map.put("authorized", true);
                map.put("user", user);
        	}
        }
        
        response.setData(map);

        log.debug("caijl:/rest/oms/authc is called,msgcode=["+ response.getMsgCode()+"],Message=["+ response.getMessage()+"].");
        return response;

    }


    @RequestMapping(value="/rest/oms/getToken",method = RequestMethod.GET)
    public Response getToken(HttpServletRequest req) {
        Response result = new Response();
        String  token = TokenHelper.setToken(req);
        result.setData(token);
        return result;
    }
    
    @RequestMapping(value="/rest/oms/findMemberInfoById",method = RequestMethod.GET)
	public Response findMemberInfoById() throws IOException
	{
		Response response = new Response();
		Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession(false);
        if(null == session){
            response.setMessage("you are not login!");
        }
        else {
            OMSRealm.ShiroOmsUser principal = (OMSRealm.ShiroOmsUser) session.getAttribute("oms");
            if (null != principal) {
                response = userService.selectByPrimaryKey(principal.getId());
            }
        }

        return response;
	}
    
    public static class LoginUser {
        private String account;
        private Integer id;

        public Integer getId() {
            return id;
        }
        public void setId(Integer id) {
            this.id = id;
        }
        public String getAccount() {
			return account;
		}
		public void setAccount(String account) {
			this.account = account;
		}

        /**
         * 重载equals,只计算account;
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            LoginUser other = (LoginUser) obj;

            if (account == null) {
                return false;
            } else if (account.equals(other.account))
                return true;
            return false;
        }
    }
}
