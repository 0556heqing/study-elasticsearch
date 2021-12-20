package com.heqing.search;

import com.heqing.search.analyzer.StringUtil;
import org.junit.Test;

/**
 * @author heqing
 * @date 2021/11/18 19:31
 *
 * 1.搜索词规范化
 * 2.分词
 */
public class StudySearch {

    /**
     * 进入搜索模块的query的最大长度，如果超过该长度，则进行截断
     */
    private static final int MAXLENGTH_QUERY = 50;

    @Test
    public void normalizeQuery() {
        String query = "HeQing，你還欠我50.2元錢!";

        //繁体转简体，全角转半角，大写转小写，出去符号
        String normalized = StringUtil.normalize(query, true);

        //截断query，最长10个单词，一个中文字符为一个单词，一个数字串为一个单词，一个英文字符串为一个单词
        normalized = StringUtil.cutoff(normalized, MAXLENGTH_QUERY, true);

        System.out.println("-->" + normalized);
    }
}
