package com.heqing.search.segment.analyzer.impl;

import com.google.common.base.Preconditions;
import com.heqing.search.analyzer.StringUtil;
import com.heqing.search.segment.common.TermVec;
import com.heqing.search.segment.common.Token;
import com.heqing.search.segment.probability.ProbabilityModel;
import com.heqing.search.segment.analyzer.IAnalyzer;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @author heqing
 * @date 2021/11/19 15:44
 */
public class BirdAnalyzer implements IAnalyzer {

    private ProbabilityModel probabilityModel = null;

    public BirdAnalyzer(String dataDir) {
        init(dataDir, 3, 2);
    }

    public BirdAnalyzer(String dataDir, int nPath, int nGram) {
        init(dataDir, nPath, nGram);
    }

    public void init(String dataDir, int nPath, int nGram) {
        Preconditions.checkArgument(nPath >= 2 && nPath <= 5, "nPath must be between 2 and 5, nPath:" + nPath);
        Preconditions.checkArgument(nGram == 2 || nGram == 3, "nGram must be 2 or 3, nGram:" + nGram);

        probabilityModel = new ProbabilityModel();
        probabilityModel.init(dataDir, nPath, nGram);
    }

    @Override
    public List<Token> completeSegment(String str) {
        str = StringUtil.normalServerString(str);
        Map<String, TermVec> termMap = probabilityModel.productTitleSegmentPos(str);
        List<Token> tokens = collectAndSortSegmentTokens(termMap);
        return tokens;
    }

    @Override
    public List<Token> probabilitySegment(String str) {
        if (StringUtils.isEmpty(str)) {
            return new ArrayList<>();
        }

        //繁体转简体
        str = StringUtil.normalServerString(str);
        HashMap<String, TermVec> termMap = probabilityModel.querySegmentQuick(str);
        List<Token> tokens = collectAndSortSegmentTokens(termMap);
        return tokens;
    }

    private List<Token> collectAndSortSegmentTokens(Map<String, TermVec> termMap) {
        List<Token> tokens = new ArrayList<Token>();
        Set<Map.Entry<String, TermVec>> termList = termMap.entrySet();
        for (Map.Entry<String, TermVec> entry : termList) {
            String term = entry.getKey();
            TermVec tv = entry.getValue();
            for (int pos : tv.startPosSet) {
                Token token = new Token();
                token.offset = (short) pos;
                token.term = term;
                token.len = tv.len;
                tokens.add(token);
            }
        }
        tokens.sort(Comparator.comparing(Token::getOffset).reversed()
                .thenComparing(Token::getTerm).reversed());
        return tokens;
    }
}
