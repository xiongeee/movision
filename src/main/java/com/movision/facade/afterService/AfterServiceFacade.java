package com.movision.facade.afterService;

import com.movision.fsearch.utils.StringUtil;
import com.movision.mybatis.afterServiceImg.entity.AfterServiceImg;
import com.movision.mybatis.afterservice.entity.AfterServiceVo;
import com.movision.mybatis.afterservice.entity.Afterservice;
import com.movision.mybatis.afterservice.service.AfterServcieServcie;
import com.movision.mybatis.logisticsCompany.entity.LogisticsCompany;
import com.movision.mybatis.orders.entity.Orders;
import com.movision.mybatis.orders.service.OrderService;
import com.movision.mybatis.shop.entity.Shop;
import com.movision.mybatis.shop.service.ShopService;
import com.movision.utils.file.FileUtil;
import com.movision.utils.oss.ImageUtil;
import com.movision.utils.oss.MovisionOssClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author shuxf
 * @Date 2017/4/6 11:16
 */
@Service
public class AfterServiceFacade {

    @Autowired
    private AfterServcieServcie afterServcieServcie;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MovisionOssClient movisionOssClient;

    @Autowired
    private ShopService shopService;

    @Transactional
    public Map<String, Object> commitAfterService(String userid, String orderid, String addressid, String goodsid, String afterstatue,
                                                  String amountdue, String remark, MultipartFile imgfile1, MultipartFile imgfile2, MultipartFile imgfile3) {
        Map<String, Object> map = new HashMap<>();

        //这里首先需要根据这个订单号查询，当前是否存在未被删除的售后单，如果存在则不可重复申请，如果不存在才可以申请
        int count = afterServcieServcie.queryAfterServiceCount(Integer.parseInt(orderid));

        if (count == 0) {
            //查询订单配送方式
            Orders orders = orderService.getOrderById(Integer.parseInt(orderid));
            int takeway = orders.getTakeway();

            //先保存基本信息
            Afterservice afterservice = new Afterservice();
            afterservice.setOrderid(Integer.parseInt(orderid));
            afterservice.setAddressid(Integer.parseInt(addressid));
            afterservice.setGoodsid(Integer.parseInt(goodsid));
            afterservice.setAfterstatue(Integer.parseInt(afterstatue));
            afterservice.setAftersalestatus(1);//1 已申请
            afterservice.setProcessingstatus(2);//2 未处理
            afterservice.setIsdel(1);//0 已删除 1 未删除
            afterservice.setUserid(Integer.parseInt(userid));
            afterservice.setProposertime(new Date());
            if (!StringUtil.isEmpty(amountdue)) {
                afterservice.setAmountdue(Double.parseDouble(amountdue));
            }
            afterservice.setRemark(remark);
            afterservice.setTakeway(takeway);

            afterServcieServcie.insertAfterInformation(afterservice);

            int afterserviceid = afterservice.getId();//售后单id
            //如果有上传图片，那就上传图片
            if (imgfile1 != null) {
                Map m = movisionOssClient.uploadObject(imgfile1, "img", "afterservice");
                String url1 = String.valueOf(m.get("url"));

                AfterServiceImg afterServiceImg = new AfterServiceImg();
                afterServiceImg.setAfterserviceid(afterserviceid);
                afterServiceImg.setImgurl(url1);
                afterServiceImg.setWidth(String.valueOf(m.get("width")));
                afterServiceImg.setHeight(String.valueOf(m.get("height")));
                afterServcieServcie.insertAfterServiceImg(afterServiceImg);
            }
            if (imgfile2 != null) {
                Map m = movisionOssClient.uploadObject(imgfile2, "img", "afterservice");
                String url2 = String.valueOf(m.get("url"));

                AfterServiceImg afterServiceImg = new AfterServiceImg();
                afterServiceImg.setAfterserviceid(afterserviceid);
                afterServiceImg.setImgurl(url2);
                afterServiceImg.setWidth(String.valueOf(m.get("width")));
                afterServiceImg.setHeight(String.valueOf(m.get("height")));
                afterServcieServcie.insertAfterServiceImg(afterServiceImg);
            }
            if (imgfile3 != null) {
                Map m = movisionOssClient.uploadObject(imgfile3, "img", "afterservice");
                String url3 = String.valueOf(m.get("url"));

                AfterServiceImg afterServiceImg = new AfterServiceImg();
                afterServiceImg.setAfterserviceid(afterserviceid);
                afterServiceImg.setImgurl(url3);
                afterServiceImg.setWidth(String.valueOf(m.get("width")));
                afterServiceImg.setHeight(String.valueOf(m.get("height")));
                afterServcieServcie.insertAfterServiceImg(afterServiceImg);
            }
            map.put("afterserviceid", afterserviceid);
            map.put("applytime", new Date());
            if (afterstatue.equals("1")) {
                map.put("type", "退货");
            } else if (afterstatue.equals("2")) {
                map.put("type", "退款");
            } else if (afterstatue.equals("3")) {
                map.put("type", "换货");
            }
            map.put("code", 200);//申请成功

        } else if (count >= 1) {
            map.put("code", 300);//重复申请

        }
        return map;
    }

    public int cancelAfterService(String userid, String afterserviceid) {
        Map<String, Object> parammap = new HashMap<>();
        parammap.put("userid", Integer.parseInt(userid));
        parammap.put("afterserviceid", Integer.parseInt(afterserviceid));

        return afterServcieServcie.cancelAfterService(parammap);
    }

    public Map<String, Object> queryAfterServiceDetail(String afterserviceid) {

        Map<String, Object> map = new HashMap<>();

        //查询售后基本信息
        AfterServiceVo vo = afterServcieServcie.queryAfterServiceDetail(Integer.parseInt(afterserviceid));
        //根据物流公司id查询物流公司名称
        String logisticsname = afterServcieServcie.queryLogisticName(vo.getLogisticsway());
        vo.setLogisticsname(logisticsname);
        map.put("afterServiceVo", vo);

        //查询售后上传的图片
        List<AfterServiceImg> afterServiceImgList = afterServcieServcie.queryAfterServiceImgList(Integer.parseInt(afterserviceid));
        map.put("afterServiceImgList", afterServiceImgList);

        //退换货说明
        if (vo.getProcessingstatus() == 1) {//处理状态：1 已处理 2 未处理
            Shop shop = shopService.queryShopInfo(Integer.parseInt(afterserviceid));
            map.put("infovo", shop);
        }

        return map;
    }

    public List<LogisticsCompany> queryLogisticType() {
        return afterServcieServcie.queryLogisticType();
    }

    public void commitReturnLogisticInfo(String id, String logisticid, String returnnumber) {
        Map<String, Object> parammap = new HashMap<>();
        parammap.put("id", id);
        parammap.put("logisticid", logisticid);
        parammap.put("returnnumber", returnnumber);
        afterServcieServcie.commitReturnLogisticInfo(parammap);
    }
}
