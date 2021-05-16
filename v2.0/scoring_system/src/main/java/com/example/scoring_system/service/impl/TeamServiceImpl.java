package com.example.scoring_system.service.impl;

import com.example.scoring_system.bean.*;
import com.example.scoring_system.mapper.TeamMapper;
import com.example.scoring_system.mapper.UserMapper;
import com.example.scoring_system.service.TeamService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class TeamServiceImpl implements TeamService {

    final int PAGE_NUMBER = 5;

    @Autowired
    TeamMapper teamMapper;

    @Autowired
    UserMapper userMapper;

    @Override
    public PageInfo<Team> selTeamByPage(PageRequest pageRequest) {
        int pageNum = pageRequest.getPageNum();
        int pageSize = pageRequest.getPageSize();
        log.error(String.valueOf(pageNum));
        log.error(String.valueOf(pageSize));
        PageHelper.startPage(pageNum, pageSize);
        List<Team> teamList = teamMapper.selAllTeam();
        return new PageInfo<Team>(teamList);
    }

    @Override
    public List<User> selAllTeamMember(Team team) {
        List<User> studentList = teamMapper.selAllTeamMember(team);
        for (User student : studentList) {
            String newAccount = student.getAccount().substring(1);
            student.setAccount(newAccount);
        }

        return studentList;
    }

    @Override
    public boolean delTeam(Team team) {
        Integer result = teamMapper.delTeam(team);
        return result == 1;
    }

    @Override
    @Transactional
    public ResponseData insTeamBatch(List<TeamForImport> teamList) {
        Integer count = 0;
        Integer result;
        for (int i = 0; i < teamList.size(); i++) {
            log.info(teamList.get(i).toString());

            Team team = new Team(teamList.get(i).getTeamName(), teamList.get(i).getTeamSlogan(), teamList.get(i).getClassId());
            result = teamMapper.addSingleTeam(team);
            if (result == 1) count++;
            List<String> idList=dealTeamList(teamList,i);
            for (int j=0;j<idList.size();j++)
            {
                if (idList.get(j)!=null)
                {
                    User user=new User();
                    user.setAccount(idList.get(j));
                    User user1=userMapper.selUserByAccount(user);
                    if (user1==null||user1.getId()==null)
                    {
                        return new ResponseData("新增失败，存在成员账户不存在："+idList.get(j),"1501","");
                    }
                }
            }
            for (int j=0;j<idList.size();j++)
            {
                teamMapper.updStuTeamId(team.getId(), idList.get(j));
            }
            String teamId = teamMapper.selectLastInsertId();
            log.error("最近插入的team的id是" + teamId);
        }
        return new ResponseData("新增成功：","200","");
    }

    private List<String> dealTeamList(List<TeamForImport> teamList,int i)
    {
        List<String> idList=new ArrayList<>();
        idList.add(teamList.get(i).getStudent1());
        idList.add(teamList.get(i).getStudent2());
        idList.add(teamList.get(i).getStudent3());
        idList.add(teamList.get(i).getStudent5());
        idList.add(teamList.get(i).getStudent6());
        idList.add(teamList.get(i).getStudent7());
        idList.add(teamList.get(i).getStudent8());
        idList.add(teamList.get(i).getStudent9());
        idList.add(teamList.get(i).getStudent10());
        return idList;
    }

    @Override
    public boolean addSingleTeam(TeamForImport team) {
        Team team1 = new Team(team.getTeamName(), team.getTeamSlogan(), team.getClassId());
        Integer result = teamMapper.addSingleTeam(team1);

        teamMapper.updStuTeamId(team.getId(), "s" + team.getStudent1());
        teamMapper.updStuTeamId(team.getId(), "s" + team.getStudent2());
        teamMapper.updStuTeamId(team.getId(), "s" + team.getStudent3());
        teamMapper.updStuTeamId(team.getId(), "s" + team.getStudent4());
        teamMapper.updStuTeamId(team.getId(), "s" + team.getStudent5());
        teamMapper.updStuTeamId(team.getId(), "s" + team.getStudent6());
        teamMapper.updStuTeamId(team.getId(), "s" + team.getStudent7());
        teamMapper.updStuTeamId(team.getId(), "s" + team.getStudent8());
        teamMapper.updStuTeamId(team.getId(), "s" + team.getStudent9());
        teamMapper.updStuTeamId(team.getId(), "s" + team.getStudent10());

        return result == 1;
    }

    @Override
    public boolean updTeam(Team team) {
        Integer result = teamMapper.updTeam(team);
        return result == 1;
    }
}