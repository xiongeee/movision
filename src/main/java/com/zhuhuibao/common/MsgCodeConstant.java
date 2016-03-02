package com.zhuhuibao.common;

/**
 * 消息定义接口常量
 * @author penglong
 *
 */
public interface MsgCodeConstant {
	
	/**
	 * 统一定义成功返回1000
	 */
	int mcode_common_success = 1000;
	
	/**
	 * 会员错误信息码：100开头
	 * 账户名已经存在
	 */
	int member_mcode_account_exist = 10001;
	
	/**
	 * 手机验证码不正确
	 */
	int member_mcode_mobile_validate_error = 10002;
	
	/**
	 * 邮箱验证码不正确
	 */
	int member_mcode_mail_validate_error = 10003;
	
	/**
	 * 激活码已过期
	 */
	int member_mcode_active_code_expire = 10004;
	
	/**
	 * 邮箱已激活请登录
	 */
	int member_mcode_mail_actived = 10005;
	
	/**
	 * 邮箱已被注册
	 */
	int member_mcode_mail_registered = 10006;
	
	/**
	 * 该邮箱未注册
	 */
	int member_mcode_mail_unregister = 10007;
	
	/**
	 * 验证身份已过期
	 */
	int member_mcode_mail_validate_expire = 10008;
	
	/**
	 * 密码找回链接已经失效
	 */
	int member_mcode_mail_url_invalid = 10009;
	
	
}
