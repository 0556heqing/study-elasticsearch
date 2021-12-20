package com.heqing.search.segment.analyzer.impl;

import com.heqing.search.analyzer.Separators;
import com.heqing.search.analyzer.StringUtil;
import com.heqing.search.segment.extend.BaseChineseTag;
import com.heqing.search.segment.extend.ExtendType;
import com.heqing.search.segment.extend.cascade.Phrase;
import com.heqing.search.segment.extend.hypernym.Hypernym;
import com.heqing.search.segment.extend.stop.StopWords;
import com.heqing.search.segment.extend.synonym.Synonym;
import com.heqing.search.segment.analyzer.IAnalyzer;
import com.heqing.search.segment.common.Token;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author heqing
 * @date 2021/11/19 17:30
 */
public class BirdExtendAnalyzer implements IAnalyzer {

    BirdAnalyzer birdAnalyzer;

    private Set<ExtendType> compExtend;

    /**
     * 停用词功能
     */
    private StopWords stopWords;

    /**
     * 拆分和合并
     */
    private Phrase phrase;

    /**
     * 同义词
     */
    private BaseChineseTag synonym;

    /**
     * 上位词关系
     */
    private BaseChineseTag upWords;

    public BirdExtendAnalyzer(String dataDir) {
        Set<ExtendType> compExt = new HashSet<>();

        /**
         * 默认加载
         * 1、停用词-STOP
         * 2、分词干预-CASCADE
         * 3、同义词-SYNONYM
         * 4、上下位词-HYPERNYM
         */
        compExt.add(ExtendType.STOP);
        compExt.add(ExtendType.CASCADE);
        compExt.add(ExtendType.SYNONYM);
        compExt.add(ExtendType.HYPERNYM);
        init(dataDir, compExt);
    }

    public BirdExtendAnalyzer(String dataDir, Set<ExtendType> compExtend) {
        init(dataDir, compExtend);
    }

    public void init(String dataDir, Set<ExtendType> compExtend) {
        dataDir = dataDir.trim();
        if (!dataDir.endsWith(Separators.SLASH)) {
            dataDir += "/";
        }

        String segmentDir = dataDir + "core";
        if (null==birdAnalyzer){
            // 加载核心分词
            birdAnalyzer = new BirdAnalyzer(segmentDir);
        }

        String extendDir = dataDir + "extend";
        //停用词
        stopWords = new StopWords(extendDir);
        //拆分合并规则
        phrase = new Phrase(extendDir);
        //同义词
        synonym = new Synonym(extendDir, 2);
        //上位词关系
        upWords = new Hypernym(extendDir,2);

        this.compExtend = compExtend;
    }

    @Override
    public List<Token> completeSegment(String str) {
        return segment(str, 1);
    }

    @Override
    public List<Token> probabilitySegment(String str) {
        return segment(str, 2);
    }

    private List<Token> segment(String str, int segmentType) {
        str = StringUtil.normalServerString(str);

        //添加同义词
        if (compExtend.contains(ExtendType.SYNONYM)) {
            String suppStr = synonym.getTags(str);
            str = str + Separators.SPACE + suppStr;
        }
        //补充上位词  概率切分慎用扩展补充
        if (compExtend.contains(ExtendType.HYPERNYM)) {
            String suppStr = upWords.getTags(str);
            str = str + Separators.SPACE + suppStr;
        }

        List<Token> tokens = new ArrayList<>();
        if(segmentType == 1) {
            //全切分
            tokens = birdAnalyzer.completeSegment(str);
        } else if(segmentType == 2) {
            // 概率切分
            tokens = birdAnalyzer.probabilitySegment(str);
        }

        //去除停用词
        if (compExtend.contains(ExtendType.STOP)) {
            tokens = stopWords.removeStopWords(tokens);
        }
        //强制合并或拆分
        if (compExtend.contains(ExtendType.CASCADE)) {
            tokens = phrase.probSegDelMergeSplitTerm(tokens);
        }

        return tokens;
    }
}
