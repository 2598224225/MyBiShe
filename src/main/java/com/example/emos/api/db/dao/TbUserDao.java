package com.example.emos.api.db.dao;

import com.example.emos.api.db.pojo.TbUser;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Mapper
public interface TbUserDao {
    public Set<String> searchUserPermissions(int userId);

    public HashMap searchById(int userId);

    public Integer searchIdByOpenId(String openId);

    public HashMap searchUserSummary(int userId);

    public HashMap searchUserInfo(int userId);

    public Integer searchDeptManagerId(int id);

    public Integer searchGmId();

    public ArrayList<HashMap> searchAllUser();

    Integer login(HashMap param);

    int updatePasswordByid(HashMap map);

    ArrayList<HashMap> selectAllByPage(HashMap map);

    long searchUserCount(HashMap map);

    int insertUser(TbUser user);

    public int update(HashMap param);

    public int deleteUserByIds(Integer[] ids);

    public ArrayList<String> searchUserRoles(int userId);

    public HashMap searchNameAndDept(int userId);


    TbUser searchUserInfoByUsername(String s);

    String searchUsernameByRole(String role);
}