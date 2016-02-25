package com.zhuhuibao.mybatis.memCenter.mapper;

import com.zhuhuibao.mybatis.memCenter.entity.Member;

public interface MemberMapper {
    //根据会员ID找到会员信息
    Member findMemById(String memberId);

    //更新个人基本信息
    int updateMemBasicInfo(Member member);
}