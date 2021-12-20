package com.heqing.search.segment;

import com.heqing.search.analyzer.Separators;
import com.heqing.search.segment.analyzer.IAnalyzer;
import com.heqing.search.segment.analyzer.impl.BirdAnalyzer;
import com.heqing.search.segment.analyzer.impl.BirdExtendAnalyzer;
import com.heqing.search.segment.analyzer.impl.NatureAnalyzer;
import com.heqing.search.segment.common.Token;
import org.junit.Test;

import java.util.List;

/**
 * @author heqing
 * @date 2021/12/17 11:24
 */
public class TestBird {

    @Test
    public void testBirdSegment() {
        String ret;
        String sentence = "helloworld 毛泽东主席对小学生说 今天天气真好啊";
        System.out.println("--> " + sentence);

        IAnalyzer natureAnalyzer = new NatureAnalyzer();
        ret = natureAnalyzer.completeSegment(sentence).toString();
        System.out.println("Nature(空格) --> " + ret);

        String dataPath = this.getClass().getProtectionDomain().getClassLoader().getResource("segment").getPath();

        String coreDataPath = dataPath + "/core";
        IAnalyzer birdAnalyzer = new BirdAnalyzer(coreDataPath);
        ret = constructSegResult(birdAnalyzer.completeSegment(sentence));
        System.out.println("Bird complete --> " + ret);
        ret = constructSegResult(birdAnalyzer.probabilitySegment(sentence));
        System.out.println("Bird probability --> " + ret);

        IAnalyzer birdExtendAnalyzer = new BirdExtendAnalyzer(dataPath);
        ret = constructSegResult(birdExtendAnalyzer.completeSegment(sentence));
        System.out.println("BirdExtend complete --> " + ret);
        ret = constructSegResult(birdExtendAnalyzer.probabilitySegment(sentence));
        System.out.println("BirdExtend probability --> " + ret);
    }

    private String constructSegResult(List<Token> tokenList) {
        StringBuilder sb = new StringBuilder();
        for (Token token : tokenList) {
            sb.append(token.getTerm()).append(Separators.SPACE);
        }
        return sb.toString().trim();
    }


}
