package com.heqing.search.segment;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.WordDictionary;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;

/**
 * @author heqing
 * @date 2021/12/17 11:23
 */
public class TestJeiba {

    @Test
    public void testJiebaSegmenter() {
        JiebaSegmenter segmenter = new JiebaSegmenter();
        String sentence = "helloworld 毛泽东主席对小学生说 今天天气真好啊";
        System.out.println(segmenter.process(sentence, JiebaSegmenter.SegMode.INDEX).toString());
    }

    @Test
    public void testLoadUserDict() {
        // 词典路径
        Path path = new File("src/test/resources/testjieba.txt").toPath();
        WordDictionary.getInstance().loadUserDict(path);

        String[] sentences = new String[] {
                "可乐必妥 左氧氟沙星片",
                "西帕 强身萝菠甫赛河里蜜膏",
                "美罗 参茸鞭丸",
                "肤阴洁 复方黄松洗液",
                "hello world",
                "今天天气真好啊",
                "金戈 枸橼酸西地那非片"};

        JiebaSegmenter segmenter = new JiebaSegmenter();
        for (String sentence : sentences) {
            System.out.println("--------------------");
            System.out.println(segmenter.process(sentence, JiebaSegmenter.SegMode.INDEX).toString());
        }
    }
}
