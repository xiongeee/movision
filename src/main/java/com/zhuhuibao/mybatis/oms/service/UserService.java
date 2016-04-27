package com.zhuhuibao.mybatis.oms.service;

import com.zhuhuibao.common.JsonResult;
import com.zhuhuibao.common.MsgCodeConstant;
import com.zhuhuibao.mybatis.oms.entity.User;
import com.zhuhuibao.mybatis.oms.mapper.UserMapper;
import com.zhuhuibao.utils.MsgPropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by Administrator on 2016/4/27 0027.
 */
@Service
@Transactional
public class UserService {

    private final static Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    UserMapper userMapper;

    public User selectUserByAccount(String userName)
    {
        User account = new User();
        try
        {
            account = userMapper.selectUserByAccount(userName);
        }
        catch(Exception e)
        {
            log.error("query user by account error!",e);
        }
        return account;
    }

    public JsonResult selectByPrimaryKey(Integer id)
    {
        JsonResult jsonResult = new JsonResult();
        try
        {
            User account = userMapper.selectByPrimaryKey(id);
            jsonResult.setData(account);
        }
        catch(Exception e)
        {
            log.error("query user by account error!",e);
            jsonResult.setCode(MsgCodeConstant.response_status_400);
            jsonResult.setMsgCode(MsgCodeConstant.mcode_common_failure);
            jsonResult.setMessage((MsgPropertiesUtils.getValue(String.valueOf(MsgCodeConstant.mcode_common_failure))));
        }
        return jsonResult;
    }
}
