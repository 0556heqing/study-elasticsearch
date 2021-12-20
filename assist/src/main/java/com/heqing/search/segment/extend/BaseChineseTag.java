package com.heqing.search.segment.extend;

import com.heqing.search.analyzer.Separators;
import com.heqing.search.segment.common.Dictionary;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author suke
 * @version 1.0
 * @className ChineseTag
 * @description
 * @date 2020/7/20 11:12
 **/
public abstract class BaseChineseTag {
    /**
     * 去除尺码、容积、重量等信息的匹配
     */
    protected final static String REGEX_PATTERN = "[0-9]*[.]?[0-9]+(kg|g|l|ml|a|ma|cm|mm|m)";
    protected final static Pattern PATTERN = Pattern.compile(REGEX_PATTERN);

    /**
     * 词汇对应关系
     */
    protected Map<String, Set<String>> word2tags;
    /**
     * 词典
     */
    protected Dictionary dict;

    protected BaseChineseTag() {
    }

    /**
     * 数据加载
     *
     * @param dataDir 数据目录
     * @param nPath   切分超参数 保留最优路径数
     */
    public BaseChineseTag(String dataDir, int nPath) {
        //步骤一： 参数整理
        dataDir = dataDir.trim();
        if (!dataDir.endsWith(Separators.SLASH)) {
            dataDir += Separators.SLASH;
        }

        //步骤二： 加载数据
        Map<String, Set<String>> word2tags = new HashMap<>();
        Set<String> words = new HashSet<>();

        loadData(dataDir, words, word2tags);
        this.word2tags = word2tags;

        //步骤三： 词典构建
        List<String> wordList = new ArrayList<>(words);
        this.dict = new Dictionary(nPath, wordList);
    }


    /**
     * 获取标签
     *
     * @param sentence
     * @return
     */
    public String getTags(String sentence) {
        //步骤一： 参数规范化
        if (StringUtils.isEmpty(sentence)) {
            return "";
        }

        Matcher matcher = PATTERN.matcher(sentence);
        if (matcher.find()) {
            sentence = matcher.replaceAll(Separators.SPACE);
        }

        //步骤二： 获取句子中需要扩展的词汇
        List<String> matchWords = dict.getTagsByContent(sentence);
        if (matchWords.isEmpty()) {
            return "";
        }

        //步骤三： 获取扩展词汇
        Set<String> tags = new HashSet<>();

        for (String matchWord : matchWords) {
            Set<String> tmpTags = word2tags.getOrDefault(matchWord, null);
            if (null != tmpTags) {
                tags.addAll(tmpTags);
            }
        }

        //步骤四： 去除远传中已经有的词汇
        //        扩展的目的是为了召回，没有必要重复增加词汇
        tags.removeAll(matchWords);

        return StringUtils.join(tags, Separators.SPACE);
    }


    /**
     * 加载词汇对应关系数据
     *
     * @param dataDir   数据目录
     * @param words     词汇列表
     * @param word2tags 词汇对应标签数据
     */
    protected abstract void loadData(String dataDir, Set<String> words, Map<String, Set<String>> word2tags);


    /**
     * 热更新数据
     *
     * @param word2tags
     * @return
     */
    public boolean updateWords(Dictionary dict, Map<String, Set<String>> word2tags) {
        this.dict = dict;
        this.word2tags = word2tags;

        return true;
    }


    public static void main(String[] args) {
        String sentence = "我爱中华@#￥%……&*（）9.8888kg 60g 20l 100ml 2a 1ma 15cm 10mm 20m";
        Matcher matcher = PATTERN.matcher(sentence);
        sentence = matcher.replaceAll(Separators.SPACE);
        System.out.println(sentence);
    }
}
