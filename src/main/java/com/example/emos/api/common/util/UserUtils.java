package com.example.emos.api.common.util;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Map;

public class UserUtils {

    private static Map<String, Object> map = new HashMap();

    /**
     * 获取登录用户名
     *
     * @return
     */
    public static String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        return authentication.getName();
    }

    public static Integer getUserId() {
        Integer userId = StpUtil.getLoginIdAsInt();
       return  userId;

    }

}
