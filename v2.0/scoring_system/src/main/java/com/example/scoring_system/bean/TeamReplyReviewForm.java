package com.example.scoring_system.bean;

import lombok.Data;

@Data
public class TeamReplyReviewForm {
    String id;
    String teamId;
    String teamName;
    String replyReviewForm;
    String replyReviewFormScore;
    String userId;
    String score;
    String advice;
    String detailsId;
    String taskId;
    String reviewPeopleNum;
    //0未评分 1 已评分
    Integer finnishCount;
}
