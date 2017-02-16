package com.movision.mybatis.bossOrders.servic;

import com.movision.mybatis.area.entity.Area;
import com.movision.mybatis.bossOrders.entity.BossOrdersVo;
import com.movision.mybatis.bossOrders.mapper.BossOrdersMapper;
import com.movision.mybatis.city.entity.City;
import com.movision.mybatis.post.entity.Post;
import com.movision.mybatis.province.entity.Province;
import com.movision.utils.pagination.model.Paging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @Author zhurui
 * @Date 2017/2/6 10:55
 */
@Service
@Transactional
public class BossOrderService {

    @Autowired
    BossOrdersMapper bossOrdersMapper;
    Logger loger = LoggerFactory.getLogger(BossOrderService.class);

    /**
     * 查询订单列表
     *
     * @param pager
     * @return
     */
    public List<BossOrdersVo> queryOrderList(Paging<Post> pager) {
        try {
            if (loger.isDebugEnabled()) {
                loger.debug("查询订单列表");
            }
            return bossOrdersMapper.findAllOrdersByList(pager.getRowBounds());
        } catch (Exception e) {
            loger.error("查询订单列表异常");
            throw e;
        }
    }

    /**
     * 根据条件查询订单
     *
     * @param map
     * @return
     */
    public List<BossOrdersVo> queryOrderByCondition(Map map) {
        try {
            if (loger.isDebugEnabled()) {
                loger.debug("根据条件查询订单");
            }
            return bossOrdersMapper.queryOrderByCondition(map);
        } catch (Exception e) {
            loger.error("根据条件查询订单");
            throw e;
        }
    }

    /**
     * 精确查询订单
     *
     * @param map
     * @return
     */
    public List<BossOrdersVo> queryAccuracyConditionByOrder(Map map) {
        try {
            if (loger.isDebugEnabled()) {
                loger.debug("精确查找订单");
            }
            return bossOrdersMapper.findAllAccuracyConditionByOrder(map);
        } catch (Exception e) {
            loger.error("精确查询订单异常");
            throw e;
        }
    }

    /**
     * 后台管理--三级联动--省市区
     * @return
     */
    public  List<Integer> queryPostProvince(){
        try{

            loger.info("查询省");
            return bossOrdersMapper.findAllProvince();
        }catch(Exception e){
            loger.error("查询省失败");
            throw  e;
        }
    }
    /**
     * 后台管理--三级联动--省市区
     * @return
     */
    public  List<Integer> queryPostCity(){
        try{

            loger.info("查询市");
            return bossOrdersMapper.findAllCity();
        }catch(Exception e){
            loger.error("查询市失败");
            throw  e;
        }
    }   /**
     * 后台管理--三级联动--省市区
     * @return
     */
    public  List<Integer> queryPostArea(){
        try{

            loger.info("查询区");
            return bossOrdersMapper.findAllArea();
        }catch(Exception e){
            loger.error("查询区失败");
            throw  e;
        }
    }
    /**
     * 后台管理-查询市名
     * @param id
     * @return
     */
    public List<City> queryPostCityName(Integer id){
        try{
            loger.error("查询市名成功");
            return  bossOrdersMapper.findAllCityName(id);
        }catch (Exception e){

            loger.error("查询市名失败");
            throw e;
        }


    }
    /**
     * 后台管理-查询区名
     * @param id
     * @return
     */
    public List<Area> queryPostAreaName(Integer id){
        try{
            loger.error("查询区名成功");
            return  bossOrdersMapper.findAllAreaName(id);
        }catch (Exception e){

            loger.error("查询区名失败");
            throw e;
        }


    }
    /**
     * 后台管理-查询省名
     * @param id
     * @return
     */
    public List<Province> queryPostProvinceName(Integer id){
        try{
            loger.error("查询省名成功");
            return  bossOrdersMapper.findAllProvinceName(id);
        }catch (Exception e){

            loger.error("查询省名失败");
            throw e;
        }


    }
    /**
     * 后台管理-查询省数量
     * @return
     */
    public Integer queryPostProvinceNum(){
        try{
            loger.error("查询省数量");
            return  bossOrdersMapper.findAllProvinceByNum();
        }catch (Exception e){

            loger.error("查询省数量失败");
            throw e;
        }
    }
    /**
     * 后台管理-查询市数量
     * @return
     */
    public Integer queryPostCityNum(){
        try{
            loger.error("查询省数量");
            return  bossOrdersMapper.findAllCityByNum();
        }catch (Exception e){

            loger.error("查询省数量失败");
            throw e;
        }
    }
    /**
     * 后台管理-查询区数量
     * @return
     */
    public Integer queryPostAreaNum(){
        try{
            loger.error("查询省数量");
            return  bossOrdersMapper.findAllAreaByNum();
        }catch (Exception e){

            loger.error("查询省数量失败");
            throw e;
        }
    }
    public BossOrdersVo queryOrderParticulars(Integer ordernumber) {
        try {
            if (loger.isDebugEnabled()) {
                loger.debug("查询订单基本信息");
            }
            return bossOrdersMapper.queryOrderParticulars(ordernumber);
        } catch (Exception e) {
            loger.error("订单基本信息查询异常");
            throw e;
        }
    }
}
