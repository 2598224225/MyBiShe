package com.example.emos.api.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.db.dao.TbAmectDao;
import com.example.emos.api.db.pojo.TbAmect;
import com.example.emos.api.db.properties.Alipay;
import com.example.emos.api.exception.EmosException;
import com.example.emos.api.service.AmectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AmectServiceImpl implements AmectService {
    @Autowired
    private TbAmectDao amectDao;

    // 支付宝网关：沙箱环境 (真实环境的话改外：https://openapi.alipay.com/gateway.do)
    @Value("${alipay.url}")
    private String url;
    // APPID (请自行填写，真实环境请做对应修改)
    @Value("${alipay.appId}")
    private String appId;
    // 应用私钥 (请自行填写，真实环境请做对应修改)
    @Value("${alipay.appPrivateKey}")
    private String alipayPrivateKey;
    // 支付宝公钥 (请自行填写，真实环境请做对应修改)
    @Value("${alipay.alipayPublicKey}")
    private String alipayPublicKey;

    @Value("${alipay.notifyUrl}")
    private String notifyUrl;

    @Override
    public PageUtils searchAmectByPage(HashMap param) {
        ArrayList<HashMap> list = amectDao.searchAmectByPage(param);
        long count = amectDao.searchAmectCount(param);
        int start = (Integer) param.get("start");
        int length = (Integer) param.get("length");
        PageUtils pageUtils = new PageUtils(list, count, start, length);
        return pageUtils;
    }

    @Override
    @Transactional
    public int insert(ArrayList<TbAmect> list) {
        list.forEach(one -> {
            amectDao.insert(one);
        });
        return list.size();
    }

    @Override
    public HashMap searchById(int id) {
        HashMap map = amectDao.searchById(id);
        return map;
    }

    @Override
    public int update(HashMap param) {
        int rows = amectDao.update(param);
        return rows;
    }

    @Override
    public int deleteAmectByIds(Integer[] ids) {
        int rows = amectDao.deleteAmectByIds(ids);
        return rows;
    }

    @Override
    public String createNativeAmectPayOrder(HashMap param) {
        HashMap map = amectDao.searchAmectByCondition(param);
        if (map != null && map.size() > 0) {
            try {
                String amount = new BigDecimal(MapUtil.getStr(map, "amount")).intValue() + "";
                AlipayClient alipayClient =
                        new DefaultAlipayClient(url, appId, alipayPrivateKey,
                                "json", "UTF-8", alipayPublicKey, "RSA2");
                AlipayTradePrecreateRequest alipayRequest = new AlipayTradePrecreateRequest();
                // 设置支付宝异步通知回调地址 (注意：这个网址必须是可以通过外网访问的网址)
                AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
                // 订单描述
                model.setBody("缴纳罚款");
                // 订单标题
                model.setSubject(MapUtil.getStr(map, "reason"));
                // 商户订单号 就是商户后台生成的订单号
                model.setOutTradeNo(MapUtil.getStr(map, "uuid"));
                // 该笔订单允许的最晚付款时间，逾期将关闭交易。取值范围：1m～15d。m-分钟，h-小时，d-天，1c-当天 (屁股后面的字母一定要带，不然报错)
                model.setTimeoutExpress("30m");
                // 订单总金额 ，默认单位为元，精确到小数点后两位，取值范围[0.01,100000000]
                model.setTotalAmount(amount);
                model.setStoreId("wyu");
                //设置支付成功回调地址，需要进行内网穿透
                alipayRequest.setNotifyUrl(notifyUrl);
                alipayRequest.setBizModel(model);    // 将订单信息设置到alipay请求中
                //客户端执行请求
                AlipayTradePrecreateResponse response =
                        alipayClient.execute(alipayRequest);

                //成功请求到阿里服务器
                if (response.isSuccess()) {
                    JSONObject respJson = JSON.parseObject(response.getBody());
                    JSONObject rsj = (JSONObject) respJson.get("alipay_trade_precreate_response");
                    String qr_code = (String) rsj.get("qr_code");
                    //生成二维码
                    QrConfig qrConfig = new QrConfig();
                    qrConfig.setWidth(255);
                    qrConfig.setHeight(255);
                    qrConfig.setMargin(2);
                    return QrCodeUtil.generateAsBase64(qr_code, qrConfig, "jpg");
                } else {
                    log.error("创建支付订单失败");
                    throw new EmosException("创建支付订单失败");
                }
            } catch (Exception e) {
                log.error("创建支付订单失败", e);
                throw new EmosException("创建支付订单失败");
            }
        } else {
            throw new EmosException("没有找到罚款单");
        }

    }

    @Override
    public int updateStatus(HashMap param) {
        int rows = amectDao.updateStatus(param);
        return rows;
    }

    @Override
    public int searchUserIdByUUID(String uuid) {
        int userId = amectDao.searchUserIdByUUID(uuid);
        return userId;
    }

    @Override
    public void searchNativeAmectPayResult(HashMap param) {
        HashMap map = amectDao.searchAmectByCondition(param);
        if (MapUtil.isNotEmpty(map)) {
            String uuid = MapUtil.getStr(map, "uuid");
            Boolean payResult = getPayResult(uuid);
            System.out.println(payResult);
        }
    }


    public Boolean getPayResult(String orderId) {
        AlipayTradeQueryRequest queryRequest = new AlipayTradeQueryRequest();

        queryRequest.setBizModel(generateOrderInfo(orderId));

        AlipayTradeQueryResponse response = null;
        try {
            AlipayClient alipayClient =
                    new DefaultAlipayClient(url, appId, alipayPrivateKey,
                            "json", "UTF-8", alipayPublicKey, "RSA2");
            response = alipayClient.execute(queryRequest);
        } catch (AlipayApiException e) {
            log.error("支付宝查询订单" + orderId + "失败！", e);
        }

        if (response == null) {
            log.error("支付宝未获取订单" + orderId + "详情！");
            return false;
        }

        if (response.isSuccess()) {
            if (response.getTradeStatus().equals("TRADE_SUCCESS") || response.getTradeStatus().equals("finished")) {
                // 更新订单状态
                amectDao.updateStatus(new HashMap() {{
                    put("uuid", orderId);
                    put("status", 2);
                }});
                return true;
            }

            log.error("支付宝订单" + orderId + "交易失败，交易状态：" + response.getTradeStatus());
            return false;
        } else {
            log.error("支付宝订单" + orderId + "查询失败！");
            return false;
        }
    }

    private AlipayTradeQueryModel generateOrderInfo(String orderId) {
//        HashMap order = amectDao.searchById(orderId);

        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        model.setOutTradeNo(orderId);

        return model;
    }


    @Override
    public HashMap searchChart(HashMap param) {
        ArrayList<HashMap> chart_1 = amectDao.searchChart_1(param);
        ArrayList<HashMap> chart_2 = amectDao.searchChart_2(param);
        ArrayList<HashMap> chart_3 = amectDao.searchChart_3(param);
        param.clear();
        int year = DateUtil.year(new Date());
        param.put("year", year);
        param.put("status", 1);
        ArrayList<HashMap> list_1 = amectDao.searchChart_4(param);
        param.replace("status", 2);
        ArrayList<HashMap> list_2 = amectDao.searchChart_4(param);

        ArrayList<HashMap> chart_4_1 = new ArrayList<>();
        ArrayList<HashMap> chart_4_2 = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            HashMap map = new HashMap();
            map.put("month", i);
            map.put("ct", 0);
            chart_4_1.add(map);
            chart_4_2.add((HashMap) map.clone());
        }
        list_1.forEach(one -> {
            chart_4_1.forEach(temp -> {
                if (MapUtil.getInt(one, "month") == MapUtil.getInt(temp, "month")) {
                    temp.replace("ct", MapUtil.getInt(one, "ct"));
                }
            });
        });

        list_2.forEach(one -> {
            chart_4_2.forEach(temp -> {
                if (MapUtil.getInt(one, "month") == MapUtil.getInt(temp, "month")) {
                    temp.replace("ct", MapUtil.getInt(one, "ct"));
                }
            });
        });


        HashMap map = new HashMap() {{
            put("chart_1", chart_1);
            put("chart_2", chart_2);
            put("chart_3", chart_3);
            put("chart_4_1", chart_4_1);
            put("chart_4_2", chart_4_2);
        }};
        return map;
    }
}
