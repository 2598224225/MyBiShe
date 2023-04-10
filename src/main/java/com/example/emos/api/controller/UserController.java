package com.example.emos.api.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONUtil;
import com.example.emos.api.common.util.PageUtils;
import com.example.emos.api.common.util.R;
import com.example.emos.api.controller.form.*;
import com.example.emos.api.db.pojo.TbUser;
import com.example.emos.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

@RestController
@RequestMapping("/user")
@Tag(name = "UserController", description = "用户Web接口")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 生成登陆二维码的字符串
     */
    @GetMapping("/createQrCode")
    @Operation(summary = "生成二维码Base64格式的字符串")
    public R createQrCode() {
        HashMap map = userService.createQrCode();
        return R.ok(map);
    }

    /**
     * 检测登陆验证码
     *
     * @param form
     * @return
     */
    @PostMapping("/checkQrCode")
    @Operation(summary = "检测登陆验证码")
    public R checkQrCode(@Valid @RequestBody CheckQrCodeForm form) {
        boolean bool = userService.checkQrCode(form.getCode(), form.getUuid());
        return R.ok().put("result", bool);
    }

    @PostMapping("/wechatLogin")
    @Operation(summary = "微信小程序登陆")
    public R wechatLogin(@Valid @RequestBody WechatLoginForm form) {
        HashMap map = userService.wechatLogin(form.getUuid());
        boolean result = (boolean) map.get("result");
        if (result) {
            int userId = (int) map.get("userId");
            StpUtil.setLoginId(userId);
            Set<String> permissions = userService.searchUserPermissions(userId);
            map.remove("userId");
            map.put("permissions", permissions);
        }
        return R.ok(map);
    }

    /**
     * 登陆成功后加载用户的基本信息
     */
    @GetMapping("/loadUserInfo")
    @Operation(summary = "登陆成功后加载用户的基本信息")
    @SaCheckLogin
    public R loadUserInfo() {
        int userId = StpUtil.getLoginIdAsInt();
        System.out.println(userId);
        HashMap summary = userService.searchUserSummary(userId);
        return R.ok(summary);
    }

    @PostMapping("/searchById")
    @Operation(summary = "根据ID查找用户")
    @SaCheckPermission(value = {"ROOT", "USER:SELECT"}, mode = SaMode.OR)
    public R searchById(@Valid @RequestBody SearchUserByIdForm form) {
        HashMap map = userService.searchById(form.getUserId());
        return R.ok(map);
    }

    @GetMapping("/searchAllUser")
    @Operation(summary = "查询所有用户")
    @SaCheckLogin
    public R searchAllUser() {
        ArrayList<HashMap> list = userService.searchAllUser();
        return R.ok().put("list", list);
    }

    @PostMapping("/searchUserByPage")
    @Operation(summary = "查询用户分页记录")
    @SaCheckPermission(value = {"ROOT", "USER:SELECT"}, mode = SaMode.OR)
    public R searchUserByPage(@Valid @RequestBody SearchUserByPageForm form) {

        Integer page = form.getPage();
        Integer length = form.getLength();
        Integer start = (page - 1) * length;

        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        param.put("start", start);
        PageUtils pageUtils = userService.SearchUserByPage(param);
        return R.ok().put("page", pageUtils);
    }


    @Operation(summary = "登录系统")
    @PostMapping("/login")
    public R login(@Valid @RequestBody LoginForm loginForm) {
        HashMap map = JSONUtil.parse(loginForm).toBean(HashMap.class);
        Integer userId = userService.login(map);
        R result = R.ok().put("result", userId != null ? true : false);

        if (userId != null) {
          StpUtil.login(userId);//保存token字符串保存到redis，返回给浏览器
            Set<String> permissions = userService.searchUserPermissions(userId);
            result.put("permissions", permissions);

        }
        return result;
    }

    @Operation(summary = "修改密码")
    @SaCheckLogin//判断用户是否登录
    @PostMapping("/updatePassword")
    public R updatePassword(@Valid @RequestBody UpdatePasswordForm updatePasswordForm) {
        int userId = StpUtil.getLoginIdAsInt();///cookie的token转换成userid
        HashMap map = new HashMap() {{
            put("userId", userId);
            put("password", updatePasswordForm.getPassword());
        }};
        int rows = userService.updatePasswordByid(map);

        return R.ok().put("rows", rows);
    }

    @GetMapping("/logout")
    @Operation(summary = "退出系统")
    public R logout() {
        StpUtil.logout();//缓存清除，cookie过期
        return R.ok();
    }

    @PostMapping("/insert")
    @SaCheckPermission(value = {"ROOT", "USER:INSERT"}, mode = SaMode.OR)
    @Operation(summary = "添加用户")
    public R inserUser(@Valid @RequestBody InsertUserForm userForm) {
        TbUser param = JSONUtil.parse(userForm).toBean(TbUser.class);
        param.setStatus((byte) 1);
        param.setRole(JSONUtil.parseArray(param.getRole()).toString());
        param.setCreateTime(new Date(System.currentTimeMillis()));
        int rows = userService.insert(param);
        return R.ok().put("rows", rows);
    }

    @PostMapping("/update")
    @SaCheckPermission(value = {"ROOT", "USER:UPDATE"}, mode = SaMode.OR)
    @Operation(summary = "修改用户")
    public R update(@Valid @RequestBody UpdateUserForm form){
        HashMap param=JSONUtil.parse(form).toBean(HashMap.class);
        param.replace("role",JSONUtil.parseArray(form.getRole()).toString());
        int rows=userService.update(param);
        if(rows==1){
            StpUtil.logoutByLoginId(form.getUserId());
        }
        return R.ok().put("rows",rows);
    }

    @PostMapping("/deleteUserByIds")
    @SaCheckPermission(value = {"ROOT", "USER:DELETE"}, mode = SaMode.OR)
    @Operation(summary = "删除用户")
        public R deleteUserByIds(@Valid @RequestBody DeleteUserByIdsForm form){
        Integer userId=StpUtil.getLoginIdAsInt();
        if(ArrayUtil.contains(form.getIds(),userId)){
            return R.error("您不能删除自己的帐户");
        }
        int rows=userService.deleteUserByIds(form.getIds());
        if(rows>0){
            for (Integer id:form.getIds()){
                StpUtil.logoutByLoginId(id);
            }
        }
        return R.ok().put("rows",rows);
    }

    @PostMapping("/searchNameAndDept")
    @Operation(summary = "查找员工姓名和部门")
    @SaCheckLogin
    public R searchNameAndDept(@Valid @RequestBody SearchNameAndDeptForm form){
        HashMap map=userService.searchNameAndDept(form.getId());
        return R.ok(map);
    }



}
