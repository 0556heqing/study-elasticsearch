package com.heqing.elasticsearch.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.completion.Completion;

import java.util.ArrayList;
import java.util.List;

/**
 * 对索引字段最小粒度分词，同义词、扩展词都加入到索引中
 * @author heqing
 */
public class IndexAnalysisUtil {

    private final static Logger logger = LoggerFactory.getLogger(IndexAnalysisUtil.class);

    public static final String SPLITER = "|||";

    public static List<Completion> buildCompletions(String word, int addWeight){
        List<Completion> targetCompetions = new ArrayList<>();
        try {
            // 原始词
            Completion originalWordCompetion = new Completion(new String[]{word});
            originalWordCompetion.setWeight(100+addWeight);
            targetCompetions.add(originalWordCompetion);

            // 全拼
            Completion pinYinCompletion = new Completion(new String[]{HanlpTool.convertToPinyinString(word)});
            pinYinCompletion.setWeight(80+addWeight);
            targetCompetions.add(pinYinCompletion);

            // 首拼
            Completion pinYinFirstCharCompletion = new Completion(new String[]{HanlpTool.convertToPinyinFirstCharString(word)});
            pinYinFirstCharCompletion.setWeight(50+addWeight);
            targetCompetions.add(pinYinFirstCharCompletion);

        } catch (Exception e) {
            logger.error("----buildCompletions error word:" + word,e);
            return targetCompetions;
        }
        return targetCompetions;
    }

}
