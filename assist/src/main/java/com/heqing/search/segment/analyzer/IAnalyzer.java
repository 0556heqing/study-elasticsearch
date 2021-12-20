package com.heqing.search.segment.analyzer;

import com.heqing.search.segment.common.Token;

import java.util.List;

/**
 * @author heqing
 * @date 2021/11/19 15:30
 */
public interface IAnalyzer {

    /**
     * 全切分接口
     *
     * @param str 输入字符串
     * @return
     */
    List<Token> completeSegment(String str);

    /**
     * 概率切分结构
     *
     * @param str 输入字符串
     * @return
     */
    List<Token> probabilitySegment(String str);

}
