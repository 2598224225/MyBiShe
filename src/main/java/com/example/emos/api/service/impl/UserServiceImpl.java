package com.example.emos.api.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.db.dao.TbUserDao;
import com.example.emos.api.db.pojo.TbUser;
import com.example.emos.api.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.servlet.http.PushBuilder;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service("userService")
public class UserServiceImpl implements UserService, UserDetailsService {
/*
    @Value("${wx.app-id}")
    private String appId;

    @Value("${wx.app-secret}")
    private String appSecret;
*/


    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TbUserDao userDao;

    @Override
    public HashMap createQrCode() {
        String uuid = IdUtil.simpleUUID();
        redisTemplate.opsForValue().set(uuid, false, 5, TimeUnit.MINUTES);
        QrConfig config = new QrConfig();
        config.setHeight(160);
        config.setWidth(160);
        config.setMargin(1);
        String base64 = QrCodeUtil.generateAsBase64("login@@@" + uuid, config, ImgUtil.IMAGE_TYPE_JPG);
        HashMap map = new HashMap() {{
            put("uuid", uuid);
            put("pic", base64);
        }};
        return map;
    }

    @Override
    public boolean checkQrCode(String code, String uuid) {
        boolean bool = redisTemplate.hasKey(uuid);
        if (bool) {
            String openId = getOpenId(code);
            long userId = userDao.searchIdByOpenId(openId);
            redisTemplate.opsForValue().set(uuid, userId);
        }
        return bool;
    }

    @Override
    public HashMap wechatLogin(String uuid) {
        HashMap map = new HashMap();
        boolean result = false;
        if (redisTemplate.hasKey(uuid)) {
            String value = redisTemplate.opsForValue().get(uuid).toString();
            if (!"false".equals(value)) {
                result = true;
                redisTemplate.delete(uuid);
                int userId = Integer.parseInt(value);
                map.put("userId", userId);
            }
        }
        map.put("result", result);
        return map;
    }

    @Override
    public Set<String> searchUserPermissions(int userId) {
        Set<String> permissions = userDao.searchUserPermissions(userId);
        return permissions;
    }

    @Override
    public HashMap searchById(int userId) {
        HashMap map = userDao.searchById(userId);
        return map;
    }

    @Override
    public HashMap searchUserSummary(int userId) {
        System.out.println(userId);
        HashMap map = userDao.searchUserSummary(userId);
        return map;
    }

    @Override
    public ArrayList<HashMap> searchAllUser() {
        ArrayList<HashMap> list = userDao.searchAllUser();
        return list;
    }

    @Override
    public Integer login(HashMap param) {
        Integer id = userDao.login(param);
        return id;
    }

    @Override
    public Integer updatePasswordByid(HashMap map) {
        return userDao.updatePasswordByid(map);
    }

    @Override
    public PageUtils SearchUserByPage(HashMap map) {
        List<HashMap> users = userDao.selectAllByPage(map);
        long count = userDao.searchUserCount(map);
        int start = ((Integer) map.get("start"));
        int size = ((Integer) map.get("length"));
        PageUtils pageUtils = new PageUtils(users, count, start, size);
        return pageUtils;

    }

    //     添加用户
    @Override
    public int insert(TbUser param) {
        return userDao.insertUser(param);

    }

    @Override
    public int update(HashMap param) {
        int rows = userDao.update(param);
        return rows;
    }

    @Override
    public int deleteUserByIds(Integer[] ids) {
        int rows = userDao.deleteUserByIds(ids);
        return rows;
    }

    private String getOpenId(String code) {
        String url = "https://api.weixin.qq.com/sns/jscode2session";
        HashMap map = new HashMap();
      /*  map.put("appid", appId);
        map.put("secret", appSecret);*/
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");
        String response = HttpUtil.post(url, map);
        JSONObject json = JSONUtil.parseObj(response);
        String openId = json.getStr("openid");
        if (openId == null || openId.length() == 0) {
            throw new RuntimeException("临时登陆凭证错误");
        }
        return openId;
    }

    @Override
    public ArrayList<String> searchUserRoles(int userId) {
        ArrayList<String> list = userDao.searchUserRoles(userId);
        return list;
    }

    /**
     * 判断是否是总经理角色
     *
     * @param userId
     * @return
     */
    @Override
    public boolean isGmRole(Integer userId) {
        ArrayList<String> roles = searchUserRoles(userId);
        return CollectionUtil.contains(roles, "总经理");
    }

    @Override
    public HashMap searchNameAndDept(int userId) {
        HashMap map = userDao.searchNameAndDept(userId);
        return map;
    }

    /**
     * 根据用户id查询一个部门经理的信息
     *
     * @param userId
     * @return
     */
    @Override
    public String searchDeptManagerByid(int userId) {

        Integer managerId = userDao.searchDeptManagerId(userId);
        if (managerId != null) {
            HashMap managerInfo = userDao.searchUserInfo(managerId);
            String username = (String) managerInfo.get("username");

            return username;
        }
        return null;
    }

    /**
     * 获取总经理信息
     *
     * @return
     */
    @Override
    public String searchGmName() {
        Integer gmId = userDao.searchGmId();
        HashMap info = userDao.searchUserInfo(gmId);
        return (String) info.get("username");
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        TbUser user = userDao.searchUserInfoByUsername(s);
        if (user == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        Set<GrantedAuthority> authorities = new HashSet<>();
        // 添加用户拥有角色 ACTIVITI_USER，才可以使用 ProcessRuntime/TaskRuntime
        // 候选组 MANAGER_TEAM
        authorities.add(new SimpleGrantedAuthority("ROLE_ACTIVITI_USER"));
        authorities.add(new SimpleGrantedAuthority("GROUP_MANAGER_TEAM"));
        user.setAuthorities(authorities);
        return user;
    }

    //通过行政角色获取用户名
    public String getUsernameByAdminstrationRole() {
        String role = "行政";
        return userDao.searchUsernameByRole(role);
    }
    //通过总监角色获取用户名
    public String getUsernameByChiefRole() {
        String role = "总监";
        return userDao.searchUsernameByRole(role);
    }
    //通过HR角色获取用户名进行请假归档
    public String getUsernameByHRRole() {
        String role = "HR";
        return userDao.searchUsernameByRole(role);
    }
    //通过财务角色获取用户名进行请假归档
    public String getUsernameByFinanceRole() {
        String role = "财务";
        return userDao.searchUsernameByRole(role);
    }

}
