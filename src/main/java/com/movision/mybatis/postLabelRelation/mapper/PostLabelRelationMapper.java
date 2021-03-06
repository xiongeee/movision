package com.movision.mybatis.postLabelRelation.mapper;

import com.movision.mybatis.post.entity.PostVo;
import com.movision.mybatis.postLabel.entity.PostLabel;
import com.movision.mybatis.postLabelRelation.entity.PostLabelRelation;
import org.apache.ibatis.session.RowBounds;

import java.util.List;
import java.util.Map;

public interface PostLabelRelationMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(PostLabelRelation record);

    int insertSelective(PostLabelRelation record);

    int insertPostLabelRelation(PostLabelRelation relation);

    PostLabelRelation selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(PostLabelRelation record);

    int updateByPrimaryKey(PostLabelRelation record);

    int labelPost(int labelid);

    List<Integer> postList(int labelid);

    List<PostVo> findAllPost(List postid, RowBounds rowBounds);

    List<PostVo> findAllPostHeatValue(List postid, RowBounds rowBounds);

    List<PostVo> findAllPostIseecen(List postid, RowBounds rowBounds);

    void deletePostLabelRelaton(Integer postid);

    int batchInsert(Map map);

    List<PostLabel> queryPostLabelByPostid(PostLabelRelation relation);

    int followlabel(int labelid);

    int isFollowLabel(Map map);
}