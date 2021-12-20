package com.heqing.search.segment.analyzer.impl;

import com.heqing.search.analyzer.Separators;
import com.heqing.search.segment.common.Token;
import com.heqing.search.segment.analyzer.IAnalyzer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author heqing
 * @date 2021/11/19 15:35
 */
public class NatureAnalyzer implements IAnalyzer {

    @Override
    public List<Token> completeSegment(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        str = str.toLowerCase().trim();
        String[] termArr = str.split(Separators.SPACE);

        ArrayList<Token> tokenArr = new ArrayList<>();
        short pos = 0;
        for (short i = 0; i < termArr.length; i++) {
            if (termArr[i].length() == 0) {
                continue;
            }
            Token t = new Token();
            t.offset = pos;
            t.term = termArr[i];
            tokenArr.add(t);
            pos += t.term.length() + 1;
        }

        return tokenArr;
    }

    @Override
    public List<Token> probabilitySegment(String str) {
        return completeSegment(str);
    }
}
