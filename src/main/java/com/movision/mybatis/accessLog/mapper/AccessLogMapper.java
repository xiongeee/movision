package com.movision.mybatis.accessLog.mapper;

import com.movision.mybatis.accessLog.entity.AccessLog;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

public interface AccessLogMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(AccessLog record);

    int insertSelective(AccessLog record);

    AccessLog selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(AccessLog record);

    int updateByPrimaryKey(AccessLog record);

    List<AccessLog> findAllqueryPlatformAccess(AccessLog accessLog, RowBounds rowBounds);
}