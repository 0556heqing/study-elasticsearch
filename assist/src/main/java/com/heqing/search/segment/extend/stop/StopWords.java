package com.heqing.search.segment.extend.stop;

import com.heqing.search.analyzer.Separators;
import com.heqing.search.segment.common.Token;
import com.heqing.search.segment.utils.LoadFileDataUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author liuxiangqian
 * @version 1.0
 * @className SegmentDemo
 * @description
 * @date 2020/9/12 20:37
 **/
public class StopWords {

    private Set<String> stopWords = new HashSet<>();

    public StopWords(String dataDir) {
        dataDir = dataDir.trim();
        if (!dataDir.endsWith(Separators.SLASH)) {
            dataDir += Separators.SLASH;
        }

        dataDir += "stop/";
        String stopWordsPath = dataDir + "stopwords.data";

        //加载停用词
        LoadFileDataUtil.loadSingleWordToSet(stopWordsPath, stopWords, 1, 5);
    }

    public List<Token> removeStopWords(List<Token> tokenList) {
        if (null == tokenList || tokenList.size() == 0) {
            return tokenList;
        }

        List<Token> newTokens = new ArrayList<>();
        int len = tokenList.size();
        for (int i = 0; i < len; i++) {
            Token token = tokenList.get(i);
            String term = token.getTerm();
            if (!beStopWord(term)) {
                newTokens.add(token);
            }
        }

        return newTokens;
    }

    private boolean beStopWord(String word) {
        return stopWords.contains(word);
    }
}
