package com.movision.mybatis.activeH5.entity;

import java.util.Date;

public class ActiveH5 {
    private Integer id;

    private String name;

    private String photo;

    private Date begintime;

    private Date endtime;

    private String activitydescription;

    private Integer isdel;

    private Date intime;

    private Integer pageview;

    private Integer isApply;//是否投稿 0否 1是

    private String awardsSetting;//奖项设置

    private String awardsRules;//评奖规则
    private Integer howvote;//0:1天1次 1:一个账号一次',

    public Integer getHowvote() {
        return howvote;
    }

    public void setHowvote(Integer howvote) {
        this.howvote = howvote;
    }

    public String getAwardsSetting() {
        return awardsSetting;
    }

    public void setAwardsSetting(String awardsSetting) {
        this.awardsSetting = awardsSetting;
    }

    public String getAwardsRules() {
        return awardsRules;
    }

    public void setAwardsRules(String awardsRules) {
        this.awardsRules = awardsRules;
    }

    public Integer getIsApply() {
        return isApply;
    }

    public void setIsApply(Integer isApply) {
        this.isApply = isApply;
    }

    public Integer getPageview() {
        return pageview;
    }

    public void setPageview(Integer pageview) {
        this.pageview = pageview;
    }

    public Date getIntime() {
        return intime;
    }

    public void setIntime(Date intime) {
        this.intime = intime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo == null ? null : photo.trim();
    }

    public Date getBegintime() {
        return begintime;
    }

    public void setBegintime(Date begintime) {
        this.begintime = begintime;
    }

    public Date getEndtime() {
        return endtime;
    }

    public void setEndtime(Date endtime) {
        this.endtime = endtime;
    }

    public String getActivitydescription() {
        return activitydescription;
    }

    public void setActivitydescription(String activitydescription) {
        this.activitydescription = activitydescription == null ? null : activitydescription.trim();
    }

    public Integer getIsdel() {
        return isdel;
    }

    public void setIsdel(Integer isdel) {
        this.isdel = isdel;
    }
}