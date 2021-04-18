package com.betachel.mybatis.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.betachel.model.LotteryResultModel;
import com.betachel.mybatis.dao.LotteryResultDAO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

/**
 * @author shiqiu
 * @date 2021/04/18
 */
public class LotteryResultService {
    private static InputStream is;
    private static SqlSessionFactoryBuilder builder;
    private static SqlSessionFactory factory;
    public static final int MAX_BATCH_SIZE = 50;

    static {
        try {
            is = LotteryResultService.class.getResourceAsStream("/SqlMapConfig.xml");
            builder = new SqlSessionFactoryBuilder();
            factory = builder.build(is);
            if ( Objects.nonNull(is) ) {
                is.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int save(List<LotteryResultModel> models) {
        if ( CollectionUtils.isEmpty(models) ) {
            return 0;
        }
        int size = Math.min(models.size(), MAX_BATCH_SIZE);
        List<LotteryResultModel> saveModels = new ArrayList<>(size);
        int count = 0, saveCount = 0;
        SqlSession sqlSession = factory.openSession(true);
        LotteryResultDAO lotteryResultDAO = sqlSession.getMapper(LotteryResultDAO.class);
        for ( LotteryResultModel model : models ) {
            count++;
            if ( Objects.isNull(model) ) {
                continue;
            }
            saveModels.add(model);
            if ( saveModels.size() == size || ( count == models.size() ) ) {
                saveCount += doSave(lotteryResultDAO, saveModels);
            }
        }

        System.out.println("save count : " + saveCount);

        return saveCount;
    }

    private int doSave(LotteryResultDAO lotteryResultDAO, List<LotteryResultModel> models) {
        if ( Objects.isNull(lotteryResultDAO) || CollectionUtils.isEmpty(models) ) {
            return 0;
        }
        int count = lotteryResultDAO.batchInsert(models);
        return count;
    }
}
