package com.example.scoringsystem.service;

import com.example.scoringsystem.bean.*;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface UserService {
    List<User> insUserBatch(List<User> userList, User user);

    Boolean savePair(List<Pair> pairList,String ClassRoomId);

    String generateJwtToken(User user);

    User getJwtTokenInfo(User user);

    Integer deleteLoginInfo(User user);

    User getUserByAccountWithoutPrivacy(User user);

    PageInfo<User> getUserByRoleWithStudent(PageRequest pageRequest);

    List<User> getUserListByTeamId(Team team);

    UserVO getUserAndClassRoomByUserId(User user);

    ResponseData insClassRomm(ClassRoom classRoom);
}