package com.movision.mybatis.homepageManage.mapper;

import com.movision.mybatis.homepageManage.entity.HomepageManage;
import com.movision.mybatis.homepageManage.entity.HomepageManageVo;
import com.movision.mybatis.manageType.entity.ManageType;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public interface HomepageManageMapper {
    int deleteByPrimaryKey(Integer id);

    String myinvite(int topictype);

    String getOpenAppImg();

    int insert(HomepageManage record);

    int insertSelective(HomepageManage record);

    HomepageManage selectByPrimaryKey(Integer id);

    HomepageManage queryBanner(int type);

    List<HomepageManageVo> queryBannerList(int topictype);

    int updateByPrimaryKeySelective(HomepageManage record);

    int updateByPrimaryKey(HomepageManage record);

    HomepageManageVo queryAvertisementById(String id);

    int addAdvertisement(HomepageManage manage);

    int queryIsAdd(HomepageManage manage);

    int updateAdvertisement(HomepageManage manage);

    List<HomepageManageVo> findAllQueryAdvertisementLike(ManageType map, RowBounds rowBounds);

    List<Integer> queryAdvertisementLocation(String type);

    int deleteAdvertisementOrderid(Map map);

    int updateAtionAdvertisementOrderid(Map map);

    List<HomepageManage> queryIndexPic();

    List<HomepageManage> queryGetAppPeacockFigure();

}