package com.movision.facade.user;

import com.movision.aop.UserSaveCache;
import com.movision.common.constant.ImConstant;
import com.movision.common.constant.MsgCodeConstant;
import com.movision.common.constant.PointConstant;
import com.movision.common.util.ShiroUtil;
import com.movision.exception.AuthException;
import com.movision.exception.BusinessException;
import com.movision.facade.im.ImFacade;
import com.movision.facade.index.FacadeHeatValue;
import com.movision.facade.pointRecord.PointRecordFacade;
import com.movision.fsearch.utils.StringUtil;
import com.movision.mybatis.PostZanRecord.service.PostZanRecordService;
import com.movision.mybatis.collection.service.CollectionService;
import com.movision.mybatis.homepageManage.service.HomepageManageService;
import com.movision.mybatis.imDevice.entity.ImDevice;
import com.movision.mybatis.imDevice.service.ImDeviceService;
import com.movision.mybatis.imuser.entity.ImUser;
import com.movision.mybatis.imuser.entity.ImdeviceAppuser;
import com.movision.mybatis.imuser.entity.ImuserAppuser;
import com.movision.mybatis.imuser.service.ImUserService;
import com.movision.mybatis.pointRecord.entity.DailyTask;
import com.movision.mybatis.pointRecord.entity.PointRecord;
import com.movision.mybatis.pointRecord.service.PointRecordService;
import com.movision.mybatis.post.entity.ActiveVo;
import com.movision.mybatis.post.entity.Post;
import com.movision.mybatis.post.entity.PostVo;
import com.movision.mybatis.post.service.PostService;
import com.movision.mybatis.user.entity.*;
import com.movision.mybatis.user.service.UserService;
import com.movision.mybatis.userRefreshRecord.service.UserRefreshRecordService;
import com.movision.utils.DateUtils;
import com.movision.utils.ListUtil;
import com.movision.utils.UserBadgeUtil;
import com.movision.utils.VideoCoverURL;
import com.movision.utils.im.CheckSumBuilder;
import com.movision.utils.pagination.model.Paging;
import com.movision.utils.propertiesLoader.PropertiesLoader;
import net.sf.json.JSONArray;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.*;

/**
 * APP用户 facade
 *
 * @Author zhuangyuhao
 * @Date 2017/1/24 20:20
 */
@Service
public class UserFacade {

    private static Logger log = LoggerFactory.getLogger(UserFacade.class);

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @Autowired
    private AppRegisterFacade appRegisterFacade;

    @Autowired
    private PointRecordFacade pointRecordFacade;

    @Autowired
    private PointRecordService pointRecordService;

    @Autowired
    private FacadeHeatValue facadeHeatValue;

    @Autowired
    private CollectionService collectionService;
    @Autowired
    private PostZanRecordService postZanRecordService;

    @Autowired
    private HomepageManageService homepageManageService;

    @Autowired
    private UserBadgeUtil userBadgeUtil;

    @Autowired
    private UserRefreshRecordService userRefreshRecordService;

    @Autowired
    private ImUserService imUserService;

    @Autowired
    private ImFacade imFacade;

    @Autowired
    private ImDeviceService imDeviceService;

    @Autowired
    private VideoCoverURL videoCoverURL;

    /**
     * 判断是否存在该手机号的app用户
     *
     * @param phone
     * @return
     */
    public Boolean isExistAccount(String phone) {
        return userService.isExistAccount(phone);
    }

    /**
     * 修改app用户token和设备号
     *
     * @param registerUser
     * @return
     */
    public int updateAccount(RegisterUser registerUser) {
        return userService.updateRegisterUser(registerUser);
    }

    public UserVo queryUserInfo(String userid) {
        return userService.queryUserInfo(Integer.parseInt(userid));
    }

    public List<PostVo> personPost(Paging<PostVo> pager, String userid) {
        return userService.personPost(pager, Integer.parseInt(userid));
    }

    public List<ActiveVo> personActive(Paging<ActiveVo> pager, String userid) throws ParseException {

        List<ActiveVo> activeVoList = userService.personActive(pager, Integer.parseInt(userid));

        for (int i = 0; i < activeVoList.size(); i++) {
            //遍历计算活动累计参与总人数
            int postid = activeVoList.get(i).getId();//取出活动id
            int partsum = postService.queryActivePartSum(postid);
            activeVoList.get(i).setPartsum(partsum);
            //遍历计算活动距结束天数
            Date begin = activeVoList.get(i).getBegintime();//活动开始时间
            Date end = activeVoList.get(i).getEndtime();//活动结束时间
            Date now = new Date();//当前时间
            int enddays = DateUtils.activeEndDays(now, begin, end);
            activeVoList.get(i).setEnddays(enddays);
        }
        return activeVoList;
    }

    public void commetAPP(String userid) {
        //查询当前用户之前是否获得过评价APP的新手任务积分
        int count = pointRecordService.queryIsComment(Integer.parseInt(userid));

        if (count == 0) {
            pointRecordFacade.addPointRecord(PointConstant.POINT_TYPE.comment_app.getCode(), ShiroUtil.getAppUserID());//根据不同积分类型赠送积分的公共方法（包括总分和流水）
        }
    }

    public void shareSucNotice(String type, String userid, String channel, String postid, String goodsid, String beshareuserid) {

        if (StringUtil.isNotEmpty(userid)) {
            pointRecordFacade.addPointRecord(PointConstant.POINT_TYPE.share.getCode(), Integer.valueOf(userid));//根据不同积分类型赠送积分的公共方法（包括总分和流水）
        }

        Map<String, Object> parammap = new HashMap<>();
        if (StringUtil.isNotEmpty(userid)) {
            parammap.put("userid", Integer.parseInt(userid));
        }
        parammap.put("intime", new Date());
        log.debug("测试分享渠道值>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>!!!!!!!!!!>>>>>>>>>>>>>>>>>" + channel);
        if (channel.equals("0")) {
            parammap.put("channel", "QQ");
        } else if (channel.equals("1")) {
            parammap.put("channel", "QQ空间");
        } else if (channel.equals("2")) {
            parammap.put("channel", "微信");
        } else if (channel.equals("3")) {
            parammap.put("channel", "朋友圈");
        } else if (channel.equals("4")) {
            parammap.put("channel", "新浪微博");
        }

        if (type.equals("0")) {
            //分享帖子或活动
            parammap.put("postid", Integer.parseInt(postid));
            if (StringUtil.isNotEmpty(userid)) {
                userService.insertPostShare(parammap);//插入帖子分享记录
            }
            postService.updatePostShareNum(parammap);//增加帖子分享次数
            //增加热度
            facadeHeatValue.addHeatValue(Integer.parseInt(postid), 5, Integer.valueOf(userid));
        } else if (type.equals("1")) {
            //分享商品
            parammap.put("goodsid", Integer.parseInt(goodsid));
            if (StringUtil.isNotEmpty(userid)) {
                userService.insertGoodsShare(parammap);//插入商品分享记录(目前商品表不记录被分享的总次数)
            }
        } else if (type.equals("2")) {
            //个人主页（自己或他人的）
            //------------------------------暂时预留，后期可以存主页分享次数
        }
    }

    public User queryUserByPhone(String phone) {
        return userService.queryUserByPhone(phone);
    }

    /**
     * 根据手机获取该用户信息，以及判断是什么角色
     *
     * @param phone
     * @return
     */
    public LoginUser getLoginUserByPhone(String phone) {

        LoginUser loginUser = userService.queryLoginUserByPhone(phone);
        if (null == loginUser) {
            throw new AuthException(MsgCodeConstant.app_user_not_exist, "该手机号的用户不存在");
        }
        return loginUser;

    }

    /**
     * 根据token获取app登录用户信息
     *
     * @param token
     * @return
     */
    public LoginUser getLoginUserByToken(String token) {

        LoginUser loginUser = userService.selectLoginUserByToken(token);
        if (null == loginUser) {
            throw new AuthException(MsgCodeConstant.app_user_not_exist_with_this_token, "该token的app用户不存在");
        }
        return loginUser;
    }

    /**
     * 根据用户id获取Loginuser
     *
     * @param userid
     * @return
     */
    public LoginUser getLoginuserByUserid(Integer userid) {
        return userService.selectLoginuserByUserid(userid);
    }

    /**
     * 根据openid获取Loginuser
     */
    public LoginUser getLoginuserByOpenid(String openid) {
        return userService.getLoginuserByOpenid(openid);
    }

    /**
     * 测试使用
     *
     * @param user
     * @return
     */
    //@CachePut(value = "user", key = "'id_'+#user.getId()")
    /*@Caching(put = {
            @CachePut(value = "user", key = "'user_id_'+#user.id"),
            @CachePut(value = "user", key = "'user_username_'+#user.username"),
            @CachePut(value = "user", key = "'user_phone_'+#user.phone")
    })*/
    @UserSaveCache  //自定义缓存注解
    public User addUser(User user) {
        System.out.println("addUser,@CachePut注解方法被调用啦。。。。。");
        return user;
    }

    /**
     * 测试使用
     *
     * @param id
     * @return
     */
    @Cacheable(value = "user", key = "'id_'+#id") //,key="'user_id_'+#id"
    public User getUserByID(Integer id) {
        System.out.println("@Cacheable注解方法被调用啦。。。。。");
        User user = new User();
        user.setId(123);
        user.setPhone("18051989558");
        user.setNickname("zyh");
        return user;
    }

    /**
     * 测试使用
     *
     * @param user
     * @return
     */
    @CachePut(value = "user", key = "'users_'+#user.getId()")
    public List<User> getUsers(User user) {
        System.out.println("@CachePut注解方法被调用啦。。。。。");
        List<User> users = new ArrayList<User>();
        for (int i = 0; i < 10; i++) {
            User temp = new User();
            temp.setNickname("username" + i);
            users.add(temp);
        }
        return users;
    }

    /**
     * 完善个人资料过程
     *
     * @param personInfo
     */
    public void finishPersonDataProcess(PersonInfo personInfo) throws IOException {

        validateNicknameIsExist(personInfo);

        updatePersonInfo(personInfo);

        addPointProcess();

    }

    /**
     * 修改昵称之前，先做重复校验
     *
     * @param personInfo
     */
    private void validateNicknameIsExist(PersonInfo personInfo) {
        String nickname = personInfo.getNickname();
        if (StringUtils.isNotBlank(nickname)) {
            Boolean isExistNickname = userService.isExistSameNickname(nickname, personInfo.getId());
            if (isExistNickname) {

                throw new BusinessException(MsgCodeConstant.app_nickname_already_exist, "该昵称已经存在，请换昵称");
            }
        }
    }

    /**
     * 判断当前用户是否需要添加【完善个人资料】的积分记录
     */
    private void addPointProcess() {
        //查询是否存在该用户的完善个人资料的积分记录
        PointRecord pointRecord = pointRecordService.selectFinishPersonDataPointRecord(ShiroUtil.getAppUserID());

        //获取当前用户信息
        User user = userService.selectByPrimaryKey(ShiroUtil.getAppUserID());
        //检查个人资料是否全部完善：生日，头像，性别，签名
        if (StringUtils.isNotBlank(String.valueOf(user.getBirthday()))
                && StringUtils.isNotBlank(user.getPhoto())
                && StringUtils.isNotBlank(String.valueOf(user.getSex()))
                && StringUtils.isNotBlank(user.getSign())
                && null == pointRecord) {

            log.debug("需要进行【完善个人资料】积分操作");
            pointRecordFacade.addPointRecord(PointConstant.POINT_TYPE.finish_personal_data.getCode(), ShiroUtil.getAppUserID());
        } else {
            log.debug("不需要进行【完善个人资料】积分操作");
        }

    }

    /**
     * 处理yw_im_user表中不存在的yw_user用户，并重新注册云信im用户，使这两个表的数据对应。
     *
     * @throws IOException
     */
    public void registeNewImUser() throws IOException {
        List<User> list = imUserService.queryNotExistImUser();
        if (ListUtil.isNotEmpty(list)) {
            //循环重新注册云信IM用户
            for (User user : list) {
                ImUser imUser = new ImUser();
                imUser.setName(user.getNickname());
                imUser.setIcon(user.getPhoto());
                imUser.setSign(user.getSign());
                imUser.setUserid(user.getId());
                imUser.setAccid(CheckSumBuilder.getAccid(String.valueOf(user.getId())));  //根据userid生成accid

                imFacade.registerImUserAndSave(imUser, imUser.getUserid(), ImConstant.TYPE_APP);
            }
        }
    }

    /**
     * 1 同步app用户信息到yw_im_user表中
     * 2 同步修改IM服务器用户信息
     *
     * @throws IOException
     */
    public void completeImuserInfo() throws IOException {
        List<ImuserAppuser> imuserAppuserList = imUserService.selectRelatedAppuserAndImuser();
        if (ListUtil.isNotEmpty(imuserAppuserList)) {
            //同步app用户信息到yw_im_user表中
            for (ImuserAppuser u : imuserAppuserList) {
                //1 修改db中的im信息
                ImUser imUser = new ImUser();
                imUser.setUserid(u.getUserid());
                imUser.setSign(u.getSign());
                imUser.setIcon(u.getPhoto());
                imUser.setName(u.getNickname());

                imUserService.updateImUserByUserid(imUser);


                //2修改云信服务端的userid对应的im信息
                Map map = new HashMap();
                map.put("accid", u.getAccid());
                map.put("name", u.getNickname());
                map.put("icon", u.getPhoto());
                map.put("sign", u.getSign());
                imFacade.updateImUserInfo(map);

            }
        }
    }

    public void completeImdeviceInfo() {
        List<ImdeviceAppuser> imdeviceAppusers = imDeviceService.selectRelatedAppuserAndImdevice();
        if (ListUtil.isNotEmpty(imdeviceAppusers)) {
            //同步app用户信息到yw_im_device表中
            for (ImdeviceAppuser d : imdeviceAppusers) {

                ImDevice imDevice = new ImDevice();
                imDevice.setDeviceid(d.getDeviceno());
                imDevice.setName(d.getNickname());
                imDevice.setIcon(d.getPhoto());
                imDevice.setSign(d.getSign());

                imDeviceService.updateImDevice(imDevice);
            }
        }
    }

    /**
     * 更新个人资料
     * 1 修改yw_use信息
     * 2 修改yw_im_user信息
     * 3 修改yw_im_device信息
     * 4 修改session信息
     *
     * @param personInfo
     */
    private void updatePersonInfo(PersonInfo personInfo) throws IOException {
        //1 修改数据库中个人信息
        updateAppUserInfo(personInfo);
        //2 查出数据库中最新的登录用户信息
        LoginUser loginUser = getLoginuserByUserid(personInfo.getId());
        //3 修改session 中的个人信息
        ShiroUtil.updateAppuser(loginUser);
        //4 修改yw_im_user表信息
        updateImUserWhenUpdateAppUser(loginUser);

        //5 修改云信服务端的userid对应的im信息
        updateUinfoByLoginUser(loginUser);

        //6 修改yw_im_device信息
        String deviceno = loginUser.getDeviceno();
        log.debug("当前设备号是：" + deviceno);
        ImDevice imDevice = imDeviceService.selectByDevice(deviceno);
        imDevice.setDeviceid(deviceno);
        imDevice.setIcon(loginUser.getPhoto());
        imDevice.setName(loginUser.getNickname());
        imDevice.setSign(loginUser.getSign());
        imDeviceService.updateImDevice(imDevice);

        //7 修改云信服务端的设备号对应的im信息
        updateUinfo(loginUser, imDevice);
    }

    private void updateUinfo(LoginUser loginUser, ImDevice imDevice) throws IOException {
        Map map = new HashMap();
        map.put("accid", imDevice.getAccid());
        map.put("name", loginUser.getNickname());
        map.put("icon", loginUser.getPhoto());
        map.put("sign", loginUser.getSign());
        imFacade.updateImUserInfo(map);
    }

    private void updateUinfoByLoginUser(LoginUser loginUser) throws IOException {
        Map map = new HashMap();
        map.put("accid", loginUser.getAccid());
        map.put("name", loginUser.getNickname());
        map.put("icon", loginUser.getPhoto());
        map.put("sign", loginUser.getSign());
        imFacade.updateImUserInfo(map);
    }

    private void updateImUserWhenUpdateAppUser(LoginUser loginUser) {
        ImUser imUser = new ImUser();
        imUser.setUserid(loginUser.getId());
        imUser.setAccid(loginUser.getAccid());
        imUser.setName(loginUser.getNickname());
        imUser.setIcon(loginUser.getPhoto());
        imUser.setSign(loginUser.getSign());
        imUserService.updateImUserByUserid(imUser);
    }

    private void updateAppUserInfo(PersonInfo personInfo) {
        User user = new User();
        user.setId(personInfo.getId());
        if (StringUtils.isNotBlank(personInfo.getNickname())) {
            user.setNickname(personInfo.getNickname().trim());  //去除首尾空格
        }
        if (StringUtils.isNotBlank(personInfo.getBirthday())) {
            user.setBirthday(DateUtils.str2Date(personInfo.getBirthday()));
        }
        user.setPhoto(personInfo.getPhoto());
        user.setSex(personInfo.getSex());
        user.setSign(personInfo.getSign());

        userService.updateByPrimaryKeySelective(user);
    }


    /**
     * 记录申请vip的时间
     */
    public void applyVip() {

        User user = new User();
        user.setId(ShiroUtil.getAppUserID());
        user.setApplydate(new Date());

        userService.updateByPrimaryKeySelective(user);
    }


    /**
     * 查询出当前用户积分剩余
     *
     * @param userid
     * @return
     */
    public int queryUserByRewarde(int userid) {
        return userService.queryUserByRewarde(userid);
    }


    /**
     * 操作用户积分
     *
     * @param integral
     */
    public int updateUserPoint(int userid, int integral, int type) {
        Map map = new HashedMap();
        map.put("userid", userid);
        map.put("integral", integral);
        map.put("type", type);
        return userService.updateUserPoint(map);
    }


    /**
     * 绑定第三方账号
     *
     * @param flag
     * @param account
     */
    public void bindThirdAccount(Integer flag, String account) {
        User appUser = userService.selectByPrimaryKey(ShiroUtil.getAppUserID());

        appRegisterFacade.setUserThirdAccount(flag, account, appUser);

        userService.updateByPrimaryKeySelective(appUser);
    }

    public User selectByPrimaryKey(Integer userid) {
        return userService.selectByPrimaryKey(userid);
    }

    /**
     * 根据当前登录的用户查询推荐的作者列表
     *
     * @param pager
     * @param userid
     * @return
     */
    public List<Author> findAllHotAuthor(Paging<Author> pager, String userid) {
        //首先查询推荐的作者列表（返回作者的phone字段，用于app端比对是不是通讯录好友的提示）
        Map<String, Object> map = new HashMap<>();
        if (StringUtil.isNotEmpty(userid)) {
            map.put("userid", Integer.parseInt(userid));
        } else {
            map.put("userid", "");
        }
        List<Author> authorList = userService.findAllHotAuthor(pager, map);

        //遍历查询作者发布的最新的三个帖子
        if (authorList.size() > 0) {
            authorList = queryNewThreePost(authorList);
        }
        return authorList;
    }

    /**
     * 根据当前登录的用户查询用户感兴趣的作者列表
     */
    public List<Author> findAllInterestAuthor(Paging<Author> pager, String userid) {
        //首先查询用户感兴趣的圈子里的作者和感兴趣的标签里的作者，根据热度值倒叙
        Map<String, Object> map = new HashMap<>();
        map.put("userid", Integer.parseInt(userid));
        List<Author> authorList = userService.findAllInterestAuthor(pager, map);

        //遍历查询作者发布的最新的三个帖子
        if (authorList.size() > 0) {
            authorList = queryNewThreePost(authorList);
        }
        return authorList;
    }

    /**
     * 根据当前登录的用户查询附近的作者列表
     */
    public List<Author> findAllNearAuthor(Paging<Author> pager, String lng, String lat, String userid) {
        //根据传入的经纬度查询距离当前经纬度30公里的所有作者（发过帖子的）
        Map<String, Object> map = new HashMap<>();
        map.put("lng", Double.parseDouble(lng));
        map.put("lat", Double.parseDouble(lat));
        if (StringUtil.isNotEmpty(userid)) {
            map.put("userid", Integer.parseInt(userid));
        } else {
            map.put("userid", "");
        }
        List<Author> authorList = userService.findAllNearAuthor(pager, map);

        //遍历查询作者发布的最新的三个帖子
        if (authorList.size() > 0) {
            authorList = queryNewThreePost(authorList);
        }
        return authorList;
    }

    /**
     * 根据传入的作者列表遍历查询作者发布的最新的三个帖子
     */
    public List<Author> queryNewThreePost(List<Author> authorList) {
        for (int i = 0; i < authorList.size(); i++) {
            Author ao = authorList.get(i);
            int id = ao.getId();//作者的用户ID
            List<Post> postList = postService.querPostListByUser(id);
            ao.setPostListByAuthor(postList);
            authorList.set(i, ao);
        }
        return authorList;
    }

    /**
     * 美番2.0我的接口上半部分(*)
     *
     * @param userid 展示的用户信息
     * @return
     */
    public UserVo queryPersonalHomepage(String userid) {
        UserVo user = wrapPersonHomepageTopPart(userid);
        return user;
    }

    /**
     * 展示用户信息
     *
     * @param userid
     * @return
     */
    private UserVo wrapPersonHomepageTopPart(String userid) {
        //查询用户信息
        UserVo user = userService.queryUserInfoHompage(Integer.parseInt(userid));
        //查询发的帖子总数
        int postcount = postService.queryUserPostCount(Integer.parseInt(userid));
        user.setPostsum(postcount);
        //查询用户参与的活动总数
        int activecount = postService.queryUserActiveCount(Integer.parseInt(userid));
        user.setActivecount(activecount);
        //收藏帖子数
        int collectioncount = collectionService.queryCollectionCount(Integer.parseInt(userid));
        user.setCollectioncount(collectioncount);
        //被赞数
        int zansum = postZanRecordService.userPostZan(Integer.parseInt(userid));
        user.setZansum(zansum);
        //被收藏数
        int collectionsum = collectionService.userPostCollection(Integer.parseInt(userid));
        user.setBecollectsum(collectionsum);
        //获取个人主页中当前用户获取到的徽章总数
        UserBadge userBadge = userBadgeUtil.judgeBadge(userid, user.getIsdv());//调用公共工具方法获取用户徽章信息
        int badgenum = UserBadgeUtil.getBadgeNum(userBadge);
        user.setBadgenum(badgenum);

        return user;
    }

    /**
     * 查询别人的主页
     *
     * @param userid
     * @return
     */
    public UserVo queryOtherPersonHomepage(String userid) {
        //1 展示用户信息
        UserVo user = wrapPersonHomepageTopPart(userid);
        //2 查询isFollow
        Map<String, Object> paramap = new HashMap<>();
        int curLoginId = ShiroUtil.getAppUserID();
        if (curLoginId == 0) {
            //未登录
            user.setIsfollow(0);    //始终保持【关注】按钮显示，即未登录状态
        } else {
            //已登录
            paramap.put("userid", curLoginId); //关注动作的主体
            paramap.put("id", userid);  //被关注的id
            int sum = userService.queryIsFollowAuthor(paramap);
            user.setIsfollow(sum);
        }
        return user;
    }

    /**
     * 美番2.0我的下半部分
     *
     * @param type
     * @param userid
     * @param paging
     * @return
     */
    public List<PostVo> mineBottle(int type, String userid, Paging<PostVo> paging) throws ParseException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        try {
            List<PostVo> list = null;
            if (type == 0) {//帖子
                //查询用户发的帖子
                list = postService.findAllUserPostList(Integer.parseInt(userid), paging);//MYSQL数据库查询出来的帖子列表
                if (list.size() == 0){
                    list = null;
                } else {
                    //获取视频贴的有效封面
                    for (int i = 0; i < list.size(); i++) {
                        PostVo vo = list.get(i);
                        String str = vo.getPostcontent();
                        JSONArray jsonArray = JSONArray.fromObject(str);
                        jsonArray = videoCoverURL.getVideoCover(jsonArray);
                        vo.setPostcontent(jsonArray.toString());
                        list.set(i, vo);
                    }
                }
            } else if (type == 1) {//活动
                //活动帖子
                list = postService.findAllUserActive(Integer.parseInt(userid), paging);
                //遍历活动获取活动参与人数和活动剩余结束天数
                for (int i = 0; i < list.size(); i++) {
                    PostVo ao = list.get(i);
                    //计算距离结束时间
                    Date begin = ao.getBegintime();
                    Date end = ao.getEndtime();
                    Date now = new Date();
                    int enddays = DateUtils.activeEndDays(now, begin, end);
                    ao.setEnddays(enddays);
                    //查询活动参与总人数
                    int partsum = postService.queryActivePartSum(ao.getId());
                    ao.setPartsum(partsum);
                    list.set(i, ao);
                }
                if (list.size() == 0) {
                    list = null;
                }
            } else if (type == 2) {//收藏
                //查询用户收藏的帖子列表
                list = postService.findAllCollectPostByUser(Integer.parseInt(userid), paging);
                if (list.size() == 0) {
                    list = null;
                } else {
                    //获取视频贴的有效封面
                    for (int i = 0; i < list.size(); i++) {
                        PostVo vo = list.get(i);
                        String str = vo.getPostcontent();
                        JSONArray jsonArray = JSONArray.fromObject(str);
                        jsonArray = videoCoverURL.getVideoCover(jsonArray);
                        vo.setPostcontent(jsonArray.toString());
                        list.set(i, vo);
                    }
                }
            }
            return list;
        } catch (Exception e) {
            log.error("查询我的模块下半部分数据异常", e);
            throw e;
        }
    }

    /**
     * 他人主页中举报用户
     *
     * @param userid
     * @param beaccid
     */
    public void accusationUser(String userid, String beaccid) {
        Map<String, Object> map = new HashMap<>();
        map.put("userid", Integer.parseInt(userid));
        map.put("beaccid", Integer.parseInt(beaccid));
        map.put("intime", new Date());
        userService.accusationUser(map);
    }

    /**
     * 我的--关注--关注的作者，点击关注调用的关注的作者列表返回接口
     */
    public Paging<UserVo> getMineFollowAuthor(String userid, String pageNo, String pageSize) {
        Paging<UserVo> pager = new Paging<>(Integer.parseInt(pageNo), Integer.parseInt(pageSize));

        //首先查询当前用户关注的作者列表
        Map<String, Object> paramap = new HashMap<>();
        paramap.put("userid", Integer.parseInt(userid));
        List<UserVo> myFollowAuthorList = userService.findAllMineFollowAuthor(paramap, pager);

        pager.result(myFollowAuthorList);
        return pager;
    }

    /**
     * 我的模块——点击粉丝，进入用户被关注的粉丝用户列表接口
     */
    public Paging<UserVo> getMyfans(String userid, String pageNo, String pageSize) {
        Paging<UserVo> pager = new Paging<>(Integer.parseInt(pageNo), Integer.parseInt(pageSize));

        //首先查询当前用户的所有粉丝列表
        Map<String, Object> parammap = new HashMap<>();
        parammap.put("userid", Integer.parseInt(userid));
        List<UserVo> myFansList = userService.findAllMineFans(parammap, pager);

//        for (int i=0; i<myFansList.size(); i++){
//            //遍历获取粉丝的已发帖子数
//            UserVo vo = myFansList.get(i);
//            int id = vo.getId();//查询到的粉丝userid
//            parammap.put("id", id);
//            int count = userService.queryPostNumByAuthor(parammap);
//            vo.setPostsum(count);
//
//            //获取当前用户是否关注过该粉丝
//            int sum = userService.queryIsFollowAuthor(parammap);
//            vo.setIsfollow(sum);
//
//            myFansList.set(i, vo);
//        }
        pager.result(myFansList);
        return pager;

    }

    /**
     * 我的邀请页面--邀请送现金页面数据返回接口
     */
    public Map<String, Object> myinvite(String userid) {
        Map<String, Object> map = new HashMap<>();

        //邀请好友宣传图片送现金宣传图url获取
        //测试环境上的邀请好友页面的topictype为9，生产环境上待定
        int topictype = 9;
        String inviteimgurl = homepageManageService.myinvite(topictype);
        map.put("inviteimgurl", inviteimgurl);

        //获取当前用户的邀请链接
        String inviteurl = PropertiesLoader.getValue("inviteprefix") + userid;
        map.put("inviteurl", inviteurl);

        //查询当前登录的用户已邀请的好友数量
        int invitenum = userService.queryInviteNum(Integer.parseInt(userid));
        map.put("invitenum", invitenum);

        return map;
    }

    /**
     * 我的邀请页面--邀请送现金页面下半部分数据返回接口
     */
    public List<InviteUserVo> myInviteList(String userid, String type, Paging<InviteUserVo> pager) {
        List<InviteUserVo> inviteUserList = null;
        if (type.equals("0")) {
            //我邀请的用户列表
            inviteUserList = userService.findAllMyInviteList(Integer.parseInt(userid), pager);
        } else if (type.equals("1")) {
            //邀请好友排行榜
            inviteUserList = userService.findAllInviteRank(pager);
        }
        return inviteUserList;
    }

    /**
     * 我的模块--XX的达人之路页面数据返回
     */
    public Map<String, Object> myTalentInfo(String userid) {

        Map<String, Object> map = new HashMap<>();

        //首先查询用户头像昵称等级等内容
        User user = userService.selectByPrimaryKey(Integer.parseInt(userid));
        TalentUserVo talentUserVo = new TalentUserVo();
        talentUserVo.setId(user.getId());
        talentUserVo.setNickname(user.getNickname());
        talentUserVo.setPhoto(user.getPhoto());

        int point = user.getPoints();//用户当前积分，2.0版本中作为经验值来使用
        //判断用户等级
        Map<String, Integer> resmap = getLev(point);
        talentUserVo.setLevel(resmap.get("lev"));

        //获取上下级经验数值
        double upperlevpoint = (double) resmap.get("uppoint");
        double nextlevpoint = (double) resmap.get("nextpoint");
        //计算升至下一级缺少多少经验值
        talentUserVo.setLackxp(nextlevpoint - point);

        double rate = ((point - upperlevpoint) / (nextlevpoint - upperlevpoint)) * 100;
        BigDecimal b = new BigDecimal(rate);
        double f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
        talentUserVo.setRate(f1);

        map.put("talentUserVo", talentUserVo);//----------------------------------------------------->1.个人信息，昵称等级经验值等

        UserBadge userBadge = userBadgeUtil.judgeBadge(userid, user.getIsdv());//调用公共工具方法获取用户徽章信息
        map.put("userBadge", userBadge);//----------------------------------------------------->2.徽章信息

        //最后查询用户的每日任务的升级情况
        DailyTask dailyTask = pointRecordFacade.getDailyTask();
        map.put("dailyTask", dailyTask);//------------------------------------------------------>3.获取每日任务完成情况

        return map;
    }

    public Map<String, Object> getBadgeUnlock(String userid, String type) {
        Map<String, Object> map = new HashMap<>();
        if (type.equals("0")) {
            //旅行者
            int footprint = userService.getfootmap(Integer.parseInt(userid));//查询当前足迹数
            int footmap1 = Integer.parseInt(PropertiesLoader.getValue("footmap1"));
            int footmap2 = Integer.parseInt(PropertiesLoader.getValue("footmap2"));
            int footmap3 = Integer.parseInt(PropertiesLoader.getValue("footmap3"));
            map = packageMap(footprint, footmap1, footmap2, footmap3);

        } else if (type.equals("1")) {
            //引荐人
            int invitesum = userService.queryInviteNum(Integer.parseInt(userid));//查询邀请总人数
            int invite1 = Integer.parseInt(PropertiesLoader.getValue("invite1"));
            int invite2 = Integer.parseInt(PropertiesLoader.getValue("invite2"));
            int invite3 = Integer.parseInt(PropertiesLoader.getValue("invite3"));
            map = packageMap(invitesum, invite1, invite2, invite3);

        } else if (type.equals("2")) {
            //创作者
            int postsum = postService.queryPostNumByUserid(Integer.parseInt(userid));//查询当前用户发帖总数
            int post1 = Integer.parseInt(PropertiesLoader.getValue("post1"));
            int post2 = Integer.parseInt(PropertiesLoader.getValue("post2"));
            int post3 = Integer.parseInt(PropertiesLoader.getValue("post3"));
            map = packageMap(postsum, post1, post2, post3);

        } else if (type.equals("3")) {
            //人气之王
            int zansum = postService.queryZanSumByUserid(Integer.parseInt(userid));//查询用户所有帖子被点赞总数
            int zan1 = Integer.parseInt(PropertiesLoader.getValue("zan1"));
            int zan2 = Integer.parseInt(PropertiesLoader.getValue("zan2"));
            int zan3 = Integer.parseInt(PropertiesLoader.getValue("zan3"));
            map = packageMap(zansum, zan1, zan2, zan3);

        } else if (type.equals("4")) {
            //收藏家
            int collectsum = postService.queryCollectByUserid(Integer.parseInt(userid));//查询用户所有帖子被收藏总数
            int collect1 = Integer.parseInt(PropertiesLoader.getValue("collect1"));
            int collect2 = Integer.parseInt(PropertiesLoader.getValue("collect2"));
            int collect3 = Integer.parseInt(PropertiesLoader.getValue("collect3"));
            map = packageMap(collectsum, collect1, collect2, collect3);

        } else if (type.equals("5")) {
            //精选
            int essencesum = postService.queryEssencesumByUserid(Integer.parseInt(userid));//查询用户当前所有帖子被精选总数
            int essence1 = Integer.parseInt(PropertiesLoader.getValue("essence1"));
            int essence2 = Integer.parseInt(PropertiesLoader.getValue("essence2"));
            int essence3 = Integer.parseInt(PropertiesLoader.getValue("essence3"));
            map = packageMap(essencesum, essence1, essence2, essence3);

        }
        return map;
    }

    /**
     * 封装一个判断逻辑的公共方法（用于返回封装好的徽章解锁条件）
     *
     * @param num   当前数值
     * @param one   一级条件
     * @param two   二级条件
     * @param three 三级条件
     * @return
     */
    public Map<String, Object> packageMap(int num, int one, int two, int three) {
        Map<String, Object> map = new HashMap<>();
        if (num <= three) {
            map.put("current", num);
        } else {
            map.put("current", three);
        }
        if (num <= one) {
            map.put("target", one);
        } else if (num <= two) {
            map.put("target", two);
        } else {
            map.put("target", three);
        }
        return map;
    }

    /**
     * 根据经验值计算用户等级
     *
     * @param point
     * @return
     */
    public Map<String, Integer> getLev(int point) {
        Map<String, Integer> map = new HashMap<>();
        int lev = 0;
        int uppoint = 0;
        int nextpoint = 0;

        int lev10 = Integer.parseInt(PropertiesLoader.getValue("lev10"));
        int lev20 = Integer.parseInt(PropertiesLoader.getValue("lev20"));
        int lev30 = Integer.parseInt(PropertiesLoader.getValue("lev30"));
        int lev40 = Integer.parseInt(PropertiesLoader.getValue("lev40"));
        int lev50 = Integer.parseInt(PropertiesLoader.getValue("lev50"));
        int lev60 = Integer.parseInt(PropertiesLoader.getValue("lev60"));
        int lev70 = Integer.parseInt(PropertiesLoader.getValue("lev70"));
        int lev80 = Integer.parseInt(PropertiesLoader.getValue("lev80"));
        int lev90 = Integer.parseInt(PropertiesLoader.getValue("lev90"));

        if (point < lev10) {
            int lev1 = Integer.parseInt(PropertiesLoader.getValue("lev1"));
            int lev2 = Integer.parseInt(PropertiesLoader.getValue("lev2"));
            int lev3 = Integer.parseInt(PropertiesLoader.getValue("lev3"));
            int lev4 = Integer.parseInt(PropertiesLoader.getValue("lev4"));
            int lev5 = Integer.parseInt(PropertiesLoader.getValue("lev5"));
            int lev6 = Integer.parseInt(PropertiesLoader.getValue("lev6"));
            int lev7 = Integer.parseInt(PropertiesLoader.getValue("lev7"));
            int lev8 = Integer.parseInt(PropertiesLoader.getValue("lev8"));
            int lev9 = Integer.parseInt(PropertiesLoader.getValue("lev9"));
            if (point < lev1) {
                lev = 0;
                uppoint = 0;
                nextpoint = lev1;
            } else if (point < lev2) {
                lev = 1;
                uppoint = lev1;
                nextpoint = lev2;
            } else if (point < lev3) {
                lev = 2;
                uppoint = lev2;
                nextpoint = lev3;
            } else if (point < lev4) {
                lev = 3;
                uppoint = lev3;
                nextpoint = lev4;
            } else if (point < lev5) {
                lev = 4;
                uppoint = lev4;
                nextpoint = lev5;
            } else if (point < lev6) {
                lev = 5;
                uppoint = lev5;
                nextpoint = lev6;
            } else if (point < lev7) {
                lev = 6;
                uppoint = lev6;
                nextpoint = lev7;
            } else if (point < lev8) {
                lev = 7;
                uppoint = lev7;
                nextpoint = lev8;
            } else if (point < lev9) {
                lev = 8;
                uppoint = lev8;
                nextpoint = lev9;
            } else {
                lev = 9;
                uppoint = lev9;
                nextpoint = lev10;
            }
        } else if (point < lev20) {
            int lev11 = Integer.parseInt(PropertiesLoader.getValue("lev11"));
            int lev12 = Integer.parseInt(PropertiesLoader.getValue("lev12"));
            int lev13 = Integer.parseInt(PropertiesLoader.getValue("lev13"));
            int lev14 = Integer.parseInt(PropertiesLoader.getValue("lev14"));
            int lev15 = Integer.parseInt(PropertiesLoader.getValue("lev15"));
            int lev16 = Integer.parseInt(PropertiesLoader.getValue("lev16"));
            int lev17 = Integer.parseInt(PropertiesLoader.getValue("lev17"));
            int lev18 = Integer.parseInt(PropertiesLoader.getValue("lev18"));
            int lev19 = Integer.parseInt(PropertiesLoader.getValue("lev19"));
            if (point < lev11) {
                lev = 10;
                uppoint = lev10;
                nextpoint = lev11;
            } else if (point < lev12) {
                lev = 11;
                uppoint = lev11;
                nextpoint = lev12;
            } else if (point < lev13) {
                lev = 12;
                uppoint = lev12;
                nextpoint = lev13;
            } else if (point < lev14) {
                lev = 13;
                uppoint = lev13;
                nextpoint = lev14;
            } else if (point < lev15) {
                lev = 14;
                uppoint = lev14;
                nextpoint = lev15;
            } else if (point < lev16) {
                lev = 15;
                uppoint = lev15;
                nextpoint = lev16;
            } else if (point < lev17) {
                lev = 16;
                uppoint = lev16;
                nextpoint = lev17;
            } else if (point < lev18) {
                lev = 17;
                uppoint = lev17;
                nextpoint = lev18;
            } else if (point < lev19) {
                lev = 18;
                uppoint = lev18;
                nextpoint = lev19;
            } else {
                lev = 19;
                uppoint = lev19;
                nextpoint = lev20;
            }
        } else if (point < lev30) {
            int lev21 = Integer.parseInt(PropertiesLoader.getValue("lev21"));
            int lev22 = Integer.parseInt(PropertiesLoader.getValue("lev22"));
            int lev23 = Integer.parseInt(PropertiesLoader.getValue("lev23"));
            int lev24 = Integer.parseInt(PropertiesLoader.getValue("lev24"));
            int lev25 = Integer.parseInt(PropertiesLoader.getValue("lev25"));
            int lev26 = Integer.parseInt(PropertiesLoader.getValue("lev26"));
            int lev27 = Integer.parseInt(PropertiesLoader.getValue("lev27"));
            int lev28 = Integer.parseInt(PropertiesLoader.getValue("lev28"));
            int lev29 = Integer.parseInt(PropertiesLoader.getValue("lev29"));
            if (point < lev21) {
                lev = 20;
                uppoint = lev20;
                nextpoint = lev21;
            } else if (point < lev22) {
                lev = 21;
                uppoint = lev21;
                nextpoint = lev22;
            } else if (point < lev23) {
                lev = 22;
                uppoint = lev22;
                nextpoint = lev23;
            } else if (point < lev24) {
                lev = 23;
                uppoint = lev23;
                nextpoint = lev24;
            } else if (point < lev25) {
                lev = 24;
                uppoint = lev24;
                nextpoint = lev25;
            } else if (point < lev26) {
                lev = 25;
                uppoint = lev25;
                nextpoint = lev26;
            } else if (point < lev27) {
                lev = 26;
                uppoint = lev26;
                nextpoint = lev27;
            } else if (point < lev28) {
                lev = 27;
                uppoint = lev27;
                nextpoint = lev28;
            } else if (point < lev29) {
                lev = 28;
                uppoint = lev28;
                nextpoint = lev29;
            } else {
                lev = 29;
                uppoint = lev29;
                nextpoint = lev30;
            }
        } else if (point < lev40) {
            int lev31 = Integer.parseInt(PropertiesLoader.getValue("lev31"));
            int lev32 = Integer.parseInt(PropertiesLoader.getValue("lev32"));
            int lev33 = Integer.parseInt(PropertiesLoader.getValue("lev33"));
            int lev34 = Integer.parseInt(PropertiesLoader.getValue("lev34"));
            int lev35 = Integer.parseInt(PropertiesLoader.getValue("lev35"));
            int lev36 = Integer.parseInt(PropertiesLoader.getValue("lev36"));
            int lev37 = Integer.parseInt(PropertiesLoader.getValue("lev37"));
            int lev38 = Integer.parseInt(PropertiesLoader.getValue("lev38"));
            int lev39 = Integer.parseInt(PropertiesLoader.getValue("lev39"));
            if (point < lev31) {
                lev = 30;
                uppoint = lev30;
                nextpoint = lev31;
            } else if (point < lev32) {
                lev = 31;
                uppoint = lev31;
                nextpoint = lev32;
            } else if (point < lev33) {
                lev = 32;
                uppoint = lev32;
                nextpoint = lev33;
            } else if (point < lev34) {
                lev = 33;
                uppoint = lev33;
                nextpoint = lev34;
            } else if (point < lev35) {
                lev = 34;
                uppoint = lev34;
                nextpoint = lev35;
            } else if (point < lev36) {
                lev = 35;
                uppoint = lev35;
                nextpoint = lev36;
            } else if (point < lev37) {
                lev = 36;
                uppoint = lev36;
                nextpoint = lev37;
            } else if (point < lev38) {
                lev = 37;
                uppoint = lev37;
                nextpoint = lev38;
            } else if (point < lev39) {
                lev = 38;
                uppoint = lev38;
                nextpoint = lev39;
            } else {
                lev = 39;
                uppoint = lev39;
                nextpoint = lev40;
            }
        } else if (point < lev50) {
            int lev41 = Integer.parseInt(PropertiesLoader.getValue("lev41"));
            int lev42 = Integer.parseInt(PropertiesLoader.getValue("lev42"));
            int lev43 = Integer.parseInt(PropertiesLoader.getValue("lev43"));
            int lev44 = Integer.parseInt(PropertiesLoader.getValue("lev44"));
            int lev45 = Integer.parseInt(PropertiesLoader.getValue("lev45"));
            int lev46 = Integer.parseInt(PropertiesLoader.getValue("lev46"));
            int lev47 = Integer.parseInt(PropertiesLoader.getValue("lev47"));
            int lev48 = Integer.parseInt(PropertiesLoader.getValue("lev48"));
            int lev49 = Integer.parseInt(PropertiesLoader.getValue("lev49"));
            if (point < lev41) {
                lev = 40;
                uppoint = lev40;
                nextpoint = lev41;
            } else if (point < lev42) {
                lev = 41;
                uppoint = lev41;
                nextpoint = lev42;
            } else if (point < lev43) {
                lev = 42;
                uppoint = lev42;
                nextpoint = lev43;
            } else if (point < lev44) {
                lev = 43;
                uppoint = lev43;
                nextpoint = lev44;
            } else if (point < lev45) {
                lev = 44;
                uppoint = lev44;
                nextpoint = lev45;
            } else if (point < lev46) {
                lev = 45;
                uppoint = lev45;
                nextpoint = lev46;
            } else if (point < lev47) {
                lev = 46;
                uppoint = lev46;
                nextpoint = lev47;
            } else if (point < lev48) {
                lev = 47;
                uppoint = lev47;
                nextpoint = lev48;
            } else if (point < lev49) {
                lev = 48;
                uppoint = lev48;
                nextpoint = lev49;
            } else {
                lev = 49;
                uppoint = lev49;
                nextpoint = lev50;
            }
        } else if (point < lev60) {
            int lev51 = Integer.parseInt(PropertiesLoader.getValue("lev51"));
            int lev52 = Integer.parseInt(PropertiesLoader.getValue("lev52"));
            int lev53 = Integer.parseInt(PropertiesLoader.getValue("lev53"));
            int lev54 = Integer.parseInt(PropertiesLoader.getValue("lev54"));
            int lev55 = Integer.parseInt(PropertiesLoader.getValue("lev55"));
            int lev56 = Integer.parseInt(PropertiesLoader.getValue("lev56"));
            int lev57 = Integer.parseInt(PropertiesLoader.getValue("lev57"));
            int lev58 = Integer.parseInt(PropertiesLoader.getValue("lev58"));
            int lev59 = Integer.parseInt(PropertiesLoader.getValue("lev59"));
            if (point < lev51) {
                lev = 50;
                uppoint = lev50;
                nextpoint = lev51;
            } else if (point < lev52) {
                lev = 51;
                uppoint = lev51;
                nextpoint = lev52;
            } else if (point < lev53) {
                lev = 52;
                uppoint = lev52;
                nextpoint = lev53;
            } else if (point < lev54) {
                lev = 53;
                uppoint = lev53;
                nextpoint = lev54;
            } else if (point < lev55) {
                lev = 54;
                uppoint = lev54;
                nextpoint = lev55;
            } else if (point < lev56) {
                lev = 55;
                uppoint = lev55;
                nextpoint = lev56;
            } else if (point < lev57) {
                lev = 56;
                uppoint = lev56;
                nextpoint = lev57;
            } else if (point < lev58) {
                lev = 57;
                uppoint = lev57;
                nextpoint = lev58;
            } else if (point < lev59) {
                lev = 58;
                uppoint = lev58;
                nextpoint = lev59;
            } else {
                lev = 59;
                uppoint = lev59;
                nextpoint = lev60;
            }
        } else if (point < lev70) {
            int lev61 = Integer.parseInt(PropertiesLoader.getValue("lev61"));
            int lev62 = Integer.parseInt(PropertiesLoader.getValue("lev62"));
            int lev63 = Integer.parseInt(PropertiesLoader.getValue("lev63"));
            int lev64 = Integer.parseInt(PropertiesLoader.getValue("lev64"));
            int lev65 = Integer.parseInt(PropertiesLoader.getValue("lev65"));
            int lev66 = Integer.parseInt(PropertiesLoader.getValue("lev66"));
            int lev67 = Integer.parseInt(PropertiesLoader.getValue("lev67"));
            int lev68 = Integer.parseInt(PropertiesLoader.getValue("lev68"));
            int lev69 = Integer.parseInt(PropertiesLoader.getValue("lev69"));
            if (point < lev61) {
                lev = 60;
                uppoint = lev60;
                nextpoint = lev61;
            } else if (point < lev62) {
                lev = 61;
                uppoint = lev61;
                nextpoint = lev62;
            } else if (point < lev63) {
                lev = 62;
                uppoint = lev62;
                nextpoint = lev63;
            } else if (point < lev64) {
                lev = 63;
                uppoint = lev63;
                nextpoint = lev64;
            } else if (point < lev65) {
                lev = 64;
                uppoint = lev64;
                nextpoint = lev65;
            } else if (point < lev66) {
                lev = 65;
                uppoint = lev65;
                nextpoint = lev66;
            } else if (point < lev67) {
                lev = 66;
                uppoint = lev66;
                nextpoint = lev67;
            } else if (point < lev68) {
                lev = 67;
                uppoint = lev67;
                nextpoint = lev68;
            } else if (point < lev69) {
                lev = 68;
                uppoint = lev68;
                nextpoint = lev69;
            } else {
                lev = 69;
                uppoint = lev69;
                nextpoint = lev70;
            }
        } else if (point < lev80) {
            int lev71 = Integer.parseInt(PropertiesLoader.getValue("lev71"));
            int lev72 = Integer.parseInt(PropertiesLoader.getValue("lev72"));
            int lev73 = Integer.parseInt(PropertiesLoader.getValue("lev73"));
            int lev74 = Integer.parseInt(PropertiesLoader.getValue("lev74"));
            int lev75 = Integer.parseInt(PropertiesLoader.getValue("lev75"));
            int lev76 = Integer.parseInt(PropertiesLoader.getValue("lev76"));
            int lev77 = Integer.parseInt(PropertiesLoader.getValue("lev77"));
            int lev78 = Integer.parseInt(PropertiesLoader.getValue("lev78"));
            int lev79 = Integer.parseInt(PropertiesLoader.getValue("lev79"));
            if (point < lev71) {
                lev = 70;
                uppoint = lev70;
                nextpoint = lev71;
            } else if (point < lev72) {
                lev = 71;
                uppoint = lev71;
                nextpoint = lev72;
            } else if (point < lev73) {
                lev = 72;
                uppoint = lev72;
                nextpoint = lev73;
            } else if (point < lev74) {
                lev = 73;
                uppoint = lev73;
                nextpoint = lev74;
            } else if (point < lev75) {
                lev = 74;
                uppoint = lev74;
                nextpoint = lev75;
            } else if (point < lev76) {
                lev = 75;
                uppoint = lev75;
                nextpoint = lev76;
            } else if (point < lev77) {
                lev = 76;
                uppoint = lev76;
                nextpoint = lev77;
            } else if (point < lev78) {
                lev = 77;
                uppoint = lev77;
                nextpoint = lev78;
            } else if (point < lev79) {
                lev = 78;
                uppoint = lev78;
                nextpoint = lev79;
            } else {
                lev = 79;
                uppoint = lev79;
                nextpoint = lev80;
            }
        } else if (point < lev90) {
            int lev81 = Integer.parseInt(PropertiesLoader.getValue("lev81"));
            int lev82 = Integer.parseInt(PropertiesLoader.getValue("lev82"));
            int lev83 = Integer.parseInt(PropertiesLoader.getValue("lev83"));
            int lev84 = Integer.parseInt(PropertiesLoader.getValue("lev84"));
            int lev85 = Integer.parseInt(PropertiesLoader.getValue("lev85"));
            int lev86 = Integer.parseInt(PropertiesLoader.getValue("lev86"));
            int lev87 = Integer.parseInt(PropertiesLoader.getValue("lev87"));
            int lev88 = Integer.parseInt(PropertiesLoader.getValue("lev88"));
            int lev89 = Integer.parseInt(PropertiesLoader.getValue("lev89"));
            if (point < lev81) {
                lev = 80;
                uppoint = lev80;
                nextpoint = lev81;
            } else if (point < lev82) {
                lev = 81;
                uppoint = lev81;
                nextpoint = lev82;
            } else if (point < lev83) {
                lev = 82;
                uppoint = lev82;
                nextpoint = lev83;
            } else if (point < lev84) {
                lev = 83;
                uppoint = lev83;
                nextpoint = lev84;
            } else if (point < lev85) {
                lev = 84;
                uppoint = lev84;
                nextpoint = lev85;
            } else if (point < lev86) {
                lev = 85;
                uppoint = lev85;
                nextpoint = lev86;
            } else if (point < lev87) {
                lev = 86;
                uppoint = lev86;
                nextpoint = lev87;
            } else if (point < lev88) {
                lev = 87;
                uppoint = lev87;
                nextpoint = lev88;
            } else if (point < lev89) {
                lev = 88;
                uppoint = lev88;
                nextpoint = lev89;
            } else {
                lev = 89;
                uppoint = lev89;
                nextpoint = lev90;
            }
        } else {
            int lev91 = Integer.parseInt(PropertiesLoader.getValue("lev91"));
            int lev92 = Integer.parseInt(PropertiesLoader.getValue("lev92"));
            int lev93 = Integer.parseInt(PropertiesLoader.getValue("lev93"));
            int lev94 = Integer.parseInt(PropertiesLoader.getValue("lev94"));
            int lev95 = Integer.parseInt(PropertiesLoader.getValue("lev95"));
            int lev96 = Integer.parseInt(PropertiesLoader.getValue("lev96"));
            int lev97 = Integer.parseInt(PropertiesLoader.getValue("lev97"));
            int lev98 = Integer.parseInt(PropertiesLoader.getValue("lev98"));
            int lev99 = Integer.parseInt(PropertiesLoader.getValue("lev99"));
            int lev100 = Integer.parseInt(PropertiesLoader.getValue("lev100"));
            if (point < lev91) {
                lev = 90;
                uppoint = lev90;
                nextpoint = lev91;
            } else if (point < lev92) {
                lev = 91;
                uppoint = lev91;
                nextpoint = lev92;
            } else if (point < lev93) {
                lev = 92;
                uppoint = lev92;
                nextpoint = lev93;
            } else if (point < lev94) {
                lev = 93;
                uppoint = lev93;
                nextpoint = lev94;
            } else if (point < lev95) {
                lev = 94;
                uppoint = lev94;
                nextpoint = lev95;
            } else if (point < lev96) {
                lev = 95;
                uppoint = lev95;
                nextpoint = lev96;
            } else if (point < lev97) {
                lev = 96;
                uppoint = lev96;
                nextpoint = lev97;
            } else if (point < lev98) {
                lev = 97;
                uppoint = lev97;
                nextpoint = lev98;
            } else if (point < lev99) {
                lev = 98;
                uppoint = lev98;
                nextpoint = lev99;
            } else {
                lev = 99;
                uppoint = lev99;
                nextpoint = lev100;
            }
        }

        map.put("lev", lev);
        map.put("uppoint", uppoint);
        map.put("nextpoint", nextpoint);

        return map;
    }


}