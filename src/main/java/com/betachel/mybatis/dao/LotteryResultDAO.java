package com.betachel.mybatis.dao;

import java.util.List;

import com.betachel.model.LotteryResultModel;

/**
 * @author shiqiu
 * @date 2021/04/18
 */
public interface LotteryResultDAO {
    List<LotteryResultModel> findAll();
    int insert(LotteryResultModel model);
    int batchInsert(List<LotteryResultModel> models);
}
