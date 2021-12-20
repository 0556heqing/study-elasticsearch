package com.heqing.search.segment;

import org.ansj.app.keyword.KeyWordComputer;
import org.ansj.app.keyword.Keyword;
import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.library.AmbiguityLibrary;
import org.ansj.library.DicLibrary;
import org.ansj.library.StopLibrary;
import org.ansj.library.SynonymsLibrary;
import org.ansj.recognition.impl.BookRecognition;
import org.ansj.recognition.impl.StopRecognition;
import org.ansj.splitWord.analysis.*;
import org.ansj.util.MyStaticValue;
import org.junit.Test;
import org.nlpcn.commons.lang.tire.domain.Forest;
import org.nlpcn.commons.lang.tire.domain.Value;
import org.nlpcn.commons.lang.tire.library.Library;

import java.util.Collection;
import java.util.List;

/**
 * @author heqing
 * @date 2021/12/17 11:25
 */
public class TestAnsj {

    /**
     * BaseAnalysis : 最小颗粒度的分词
     *      基本就是保证了最基本的分词.词语颗粒度最非常小的..所涉及到的词大约是10万左右.
     *      基本分词速度非常快.在macAir上.能到每秒300w字每秒.同时准确率也很高.但是对于新词他的功能十分有限.
     * ToAnalysis : 精准分词 (推荐)
     *      它在易用性,稳定性.准确性.以及分词效率上.都取得了一个不错的平衡.如果你初次尝试Ansj如果你想开箱即用.那么就用这个分词方式是不会错的.
     * DicAnalysis : 用户自定义词典优先策略的分词
     *      用户自定义词典优先策略的分词,如果你的用户自定义词典足够好,或者你的需求对用户自定义词典的要求比较高,那么强烈建议你使用DicAnalysis的分词方式.
     * IndexAnalysis : 面向索引的分词
     *       面向索引的分词。顾名思义就是适合在lucene等文本检索中用到的分词。 主要考虑以下两点
     *       召回率 * 召回率是对分词结果尽可能的涵盖。
     *       准确率 * 其实这和召回本身是具有一定矛盾性的Ansj的强大之处是很巧妙的避开了这两个的冲突
     * NlpAnalysis : 带有新词发现功能的分词
     *       适用方式.1.语法实体名抽取.未登录词整理.主要是对文本进行发现分析等工作
     */
    @Test
    public void testAnsjSegmenter() {
        String sentence = "helloworld 毛泽东主席对小学生说 今天天气真好啊！";
        System.out.println("BaseAnalysis  : " + BaseAnalysis.parse(sentence));
        System.out.println("ToAnalysis    : " + ToAnalysis.parse(sentence));
        System.out.println("DicAnalysis   : " + DicAnalysis.parse(sentence));
        System.out.println("IndexAnalysis : " + IndexAnalysis.parse(sentence));
        System.out.println("NlpAnalysis   : " + NlpAnalysis.parse(sentence));
    }

    /**
     * 词典各字段之间使用tab(\t)分割，而不是空格。
     * DicLibrary : 用户自定义词典,格式 词语 词性 词频
     * StopLibrary : 停用词词典,格式 词语 停用词类型[可以为空]
     * CrfLibrary : crf模型,格式 二进制格式
     * AmbiguityLibrary : 歧义词典, 格式:'词语0 词性0 词语1 词性1.....'
     * SynonymsLibrary : 同义词词典,格式'词语0 词语1 词语2 词语3',采用幂等策略,w1=w2 w2=w3 w1=w3
     */
    @Test
    public void testLoadUserDict() {
        // 分词调用前重新设定字典路径
        MyStaticValue.ENV.put(DicLibrary.DEFAULT, "src/test/resources/library/dic.dic");
        // 添加单个字
        Value value = new Value("毛泽东主席", "n", "11111111");
        Library.insertWord(DicLibrary.get(), value);

        String sentence = "毛泽东主席对小学生说,今天天气真好啊！";
        System.out.println(IndexAnalysis.parse(sentence));
    }

    @Test
    public void testStop() {
        StopRecognition filter = new StopRecognition();
        filter.insertStopNatures("y"); //过滤词性
        filter.insertStopWords("毛泽东"); //过滤单词
        filter.insertStopRegexes("小.*?"); //支持正则表达式

        String sentence = "毛泽东主席对小学生说,今天天气真好啊！";
        System.out.println(IndexAnalysis.parse(sentence).recognition(filter));
    }

    @Test
    public void testFind() {
        DicLibrary.insert(DicLibrary.DEFAULT, "由浅入深学JAVA", "n", 1000000);

        String sentence = "我第一本JAVA书籍是《由浅入深学JAVA》";
        System.out.println(IndexAnalysis.parse(sentence));
        System.out.println(IndexAnalysis.parse(sentence).recognition(new BookRecognition()).getTerms().toString());

        KeyWordComputer kwc = new KeyWordComputer(5);
        String title = "JAVA书籍";
        Collection<Keyword> result = kwc.computeArticleTfidf(title, sentence);
        System.out.println(result);
    }
}
