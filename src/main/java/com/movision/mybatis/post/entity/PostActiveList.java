package com.movision.mybatis.post.entity;

import com.movision.mybatis.goods.entity.GoodsVo;

import java.util.Date;
import java.util.List;

/**
 * @Author zhanglei
 * @Date 2017/2/11 9:39
 */
public class PostActiveList {
    private Integer zansum;
    private Integer commentsum;
    private Integer forwardsum;
    private Integer collectsum;
    private String postcontent;
    private List<GoodsVo> goodss;

    public List<GoodsVo> getGoodss() {
        return goodss;
    }

    public void setGoodss(List<GoodsVo> goodss) {
        this.goodss = goodss;
    }

    public String getPostcontent() {
        return postcontent;
    }

    public void setPostcontent(String postcontent) {
        this.postcontent = postcontent;
    }

    public Integer getZansum() {
        return zansum;
    }

    public void setZansum(Integer zansum) {
        this.zansum = zansum;
    }

    public Integer getCommentsum() {
        return commentsum;
    }

    public void setCommentsum(Integer commentsum) {
        this.commentsum = commentsum;
    }

    public Integer getForwardsum() {
        return forwardsum;
    }

    public void setForwardsum(Integer forwardsum) {
        this.forwardsum = forwardsum;
    }

    public Integer getCollectsum() {
        return collectsum;
    }

    public void setCollectsum(Integer collectsum) {
        this.collectsum = collectsum;
    }

    private Integer id;//帖子id
    private Integer circleid;
    private Integer userid;//用户id

    public Integer getUserid() {
        return userid;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    private Integer payStatue;//付款方式

    public Integer getPayStatue() {
        return payStatue;
    }

    public void setPayStatue(Integer payStatue) {
        this.payStatue = payStatue;
    }

    private Double moneypay;//实付金额
    private Double moneyying;//应付金额

    public Double getMoneyying() {
        return moneyying;
    }

    public void setMoneyying(Double moneyying) {
        this.moneyying = moneyying;
    }

    public Double getMoneypay() {
        return moneypay;
    }

    public void setMoneypay(Double moneypay) {
        this.moneypay = moneypay;
    }

    private  String phone;//联系方式

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getCircleid() {
        return circleid;
    }

    public void setCircleid(Integer circleid) {
        this.circleid = circleid;
    }

    private Double sumfree;//总价格

    public Double getSumfree() {
        return sumfree;
    }

    public void setSumfree(Double sumfree) {
        this.sumfree = sumfree;
    }

    public Integer persum;//报名人数

    public Integer getPersum() {
        return persum;
    }

    public void setPersum(Integer persum) {
        this.persum = persum;
    }

    private String title;//标题

    private String nickname;//发帖人

    private Integer orderid;//排序id

    private  Date intime;//参与时间

    public Date getIntime() {
        return intime;
    }

    public void setIntime(Date intime) {
        this.intime = intime;
    }

    private  Date begintime;

    private Date endtime;

    private Integer isactive;

    private Integer activetype;

    private Double activefee;

    private  String activestatue;//活动状态

    private Date essencedate;//精选日期

    public Integer getId() {
        return id;
    }

    public String getActivestatue() {
        return activestatue;
    }

    public void setActivestatue(String activestatue) {
        this.activestatue = activestatue;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Date getEssencedate() {
        return essencedate;
    }

    public void setEssencedate(Date essencedate) {
        this.essencedate = essencedate;
    }

    public Double getActivefee() {

        return activefee;
    }

    public void setActivefee(Double activefee) {
        this.activefee = activefee;
    }

    public Integer getActivetype() {

        return activetype;
    }

    public void setActivetype(Integer activetype) {
        this.activetype = activetype;
    }

    public Integer getIsactive() {

        return isactive;
    }

    public void setIsactive(Integer isactive) {
        this.isactive = isactive;
    }

    public Date getEndtime() {

        return endtime;
    }

    public void setEndtime(Date endtime) {
        this.endtime = endtime;
    }

    public Date getBegintime() {

        return begintime;
    }

    private String coverimg;

    public String getCoverimg() {
        return coverimg;
    }

    public void setCoverimg(String coverimg) {
        this.coverimg = coverimg;
    }

    public void setBegintime(Date begintime) {
        this.begintime = begintime;
    }

    public Integer getOrderid() {

        return orderid;
    }

    public void setOrderid(Integer orderid) {
        this.orderid = orderid;
    }

    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private Integer isessence;
    private String subtitle;

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public Integer getIsessence() {
        return isessence;
    }

    public void setIsessence(Integer isessence) {
        this.isessence = isessence;
    }
}
