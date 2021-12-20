package com.heqing.search.segment.extend.cascade;

import com.heqing.search.analyzer.Separators;
import com.heqing.search.analyzer.StringUtil;
import com.heqing.search.segment.common.Token;
import com.heqing.search.segment.utils.LoadFileDataUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 该类主要针对切分后的结果进行再处理
 *
 * 全切分：
 * 英文强制补充拆分：  iphonexs  补充强制拆分  iphone xs
 * 标题中空格隔开的英文强制补充合并：  iphone xs 补充强制合并  iphonexs
 * 紧邻的切分结果强制补充：   5 g 强制补充 5g    [分词会切分为 5 g]
 * u 盘 强制补充 u盘   [分词会切分为 u 盘]
 * 例如： 红色u盘 -> 红色 u 盘 -> 红色 u 盘 u盘
 *
 * 概率切分：
 * 英文强制补充拆分：   iphonexs  强制拆分  iphone xs
 * 紧邻强制合并：       5 g 强制替换 5g
 * u 盘 强制替换 u盘
 * 例如： 红色u盘 -> 红色 u 盘 -> 红色 u盘
 *
 * @author liuxiangqian
 * @version 1.0
 * @className Phrase
 * @description
 * @date 2020/2/7 15:44
 **/
@Slf4j
public class Phrase {

    /**
     * 需要强制拆分的英文短语
     * iphonexs
     */
    private Map<String, String> splitWords = new HashMap<>();
    private String splitPath = "";

    /**
     * 需要强制合并才能得到的词汇，这些词汇都是混合词汇
     * 例如： u盘 5g
     */
    private Set<String> mixWords = new HashSet<>();
    private String mixPath = "";

    public Phrase(String dataDir) {
        dataDir = dataDir.trim();
        if (!dataDir.endsWith(Separators.SLASH)) {
            dataDir += Separators.SLASH;
        }

        dataDir += "phrase/";
        String splitPath = dataDir + "phrase.split";
        String mixPath = dataDir + "phrase.merge";

        this.splitPath = splitPath;
        this.mixPath = mixPath;

        //加载强制拆分数据
        LoadFileDataUtil.loadWordToMap(splitPath, splitWords, Separators.COMMA);

        //加载强制合并数据
        LoadFileDataUtil.loadSingleWordToSet(mixPath, mixWords, 1, 10);
    }

    /**
     * 全切分--停用词、合并、拆分
     */
    public List<Token> completeSegmentMergeAndSplit(List<Token> tokens) {
        List<Token> newTokens = new ArrayList<>();
        int size = tokens.size();
        for (int i = 0; i < size; i++) {
            Token curToken = tokens.get(i);

            //强制拆分
            if (splitWords.containsKey(curToken.getTerm())) {
                List<Token> tmpTokens = splitWords(curToken);
                newTokens.addAll(tmpTokens);
                continue;
            }

            //强制合并
            if (i == size - 1) {
                break;
            }

            Token nextToken = tokens.get(i + 1);

            //尝试进行相邻英文合并
            Token mergeToken = adjacentTwoEnglishMerge(curToken, nextToken);
            if (null != mergeToken) {
                newTokens.add(mergeToken);
                continue;
            }


            boolean beAdjacent = nextToken.getOffset() != curToken.getOffset() + curToken.getTerm().length() + 1;
            if (!beAdjacent) {
                //非紧邻词汇，不需要强制合并
                continue;
            }

            //尝试进行强制合并
            String newWord = curToken.getTerm() + nextToken.getTerm();
            if (mixWords.contains(newWord)) {
                Token tmpToken = new Token(newWord, curToken.getOffset(), 0.0f);
                newTokens.add(tmpToken);
                i += 1;
            }
        }

        newTokens.addAll(tokens);
        newTokens.sort(Comparator.comparing(a -> a.getOffset()));

        return newTokens;
    }

    /**
     * 干预分词结果，停用词、合并、拆分term
     *
     * @param tokens 分词结果
     */
    public List<Token> probSegDelMergeSplitTerm(List<Token> tokens) {
        List<Token> newTokens = new ArrayList<>();
        int size = tokens.size();
        for (int i = 0; i < size; i++) {
            Token curToken = tokens.get(i);
            //强制拆分
            if (splitWords.containsKey(curToken.getTerm())) {
                List<Token> tmpTokens = splitWords(curToken);
                newTokens.addAll(tmpTokens);
                continue;
            }

            if (i == size - 1) {
                newTokens.add(curToken);
                break;
            }

            //强制合并
            Token nextToken = tokens.get(i + 1);
            boolean beAdjacent = (nextToken.getOffset() != (curToken.getOffset() + curToken.getTerm().length() + 1));
            if (!beAdjacent) {
                //非紧邻词汇，不需要强制合并---停用词
                newTokens.add(curToken);
                continue;
            }


            String newWord = curToken.getTerm() + nextToken.getTerm();
            if (mixWords.contains(newWord)) {
                Token tmpToken = new Token(newWord, curToken.getOffset(), 0.0f);
                newTokens.add(tmpToken);
                i += 1;
            } else {
                newTokens.add(curToken);
            }
        }

        return newTokens;
    }

    /**
     * 相邻词汇 如果是英文要进行合并补充
     * 如果相邻词汇是英文，切距离为0或1就合并，或者就返回null
     *
     * @return
     */
    private Token adjacentTwoEnglishMerge(Token preToken, Token nextToken) {
        boolean beNull = (null == preToken || null == nextToken);
        boolean beEmpty = (preToken.getTerm().length() == 0 || nextToken.getTerm().length() == 0);
        boolean beAllEnglish = StringUtil.isAlphabet(preToken.getTerm().charAt(0))
                && StringUtil.isAlphabet(nextToken.getTerm().charAt(0));
        //紧邻关系 例如： iphone xs  中间有一个空格距离
        boolean beAdjacent = (preToken.offset + preToken.getTerm().length() + 1 == nextToken.getOffset());
        if (beNull || beEmpty || !beAllEnglish || !beAdjacent) {
            return null;
        }

        String newWord = preToken.getTerm() + nextToken.getTerm();
        Token token = new Token(newWord, preToken.getOffset(), 0.0f);
        return token;
    }

    /**
     * 拆分词
     */
    private List<Token> splitWords(Token token) {
        List<Token> newTokens = new ArrayList<>();
        //需要强制拆分
        String splitStr = splitWords.get(token.getTerm());
        //采用空白符分隔，免得空格写成了tab
        String[] arr = splitStr.split(Separators.SPACE);
        short pos = token.offset;
        for (String str : arr) {
            Token newToken = new Token();
            newToken.setOffset(pos);
            newToken.setTerm(str);

            newTokens.add(newToken);
            //句子不会很长的，不会出现问题
            pos = (short) (pos + str.length());
        }
        return newTokens;
    }

    /**
     * 更新拆分词库：增量更新，不覆盖本地库
     */
    public boolean updateSplitWords(Map<String, String> wordsMap) {
        if (wordsMap.isEmpty()) {
            return false;
        }

        Map<String, String> splitWordsTmp = new HashMap<>(1024);
        LoadFileDataUtil.loadWordToMap(this.splitPath, splitWordsTmp, Separators.COMMA);

        for (Map.Entry<String, String> entry : wordsMap.entrySet()) {
            if (splitWordsTmp.containsKey(entry.getKey())) {
                continue;
            } else {
                splitWordsTmp.put(entry.getKey(), entry.getValue());
            }
        }
        this.splitWords = splitWordsTmp;
        log.info("更新词库成功，现有拆分词总数：{}", this.splitWords.size());
        return true;
    }

    /**
     * 更新合并词库
     */
    public boolean updateMixWords(List<String> wordsList) {
        if (wordsList.isEmpty()) {
            return false;
        }

        Set<String> mixWordsTmp = new HashSet<>();
        LoadFileDataUtil.loadSingleWordToSet(mixPath, mixWordsTmp, 1, 10);

        for (String str : wordsList) {
            str = str.trim();
            if (str.isEmpty()) {
                continue;
            }
            mixWordsTmp.add(str);
        }

        this.mixWords = mixWordsTmp;
        log.info("更新词库成功，现有合并词总数：{}", this.mixWords.size());
        return true;
    }

    /**
     * 构建切分结果
     */
    private static List<Token> constructTokens(String[] arr) {
        List<Token> tokens = new ArrayList<>();
        short pos = 0;
        for (String str : arr) {
            Token token = new Token();
            token.setOffset(pos);
            token.setTerm(str);
            tokens.add(token);
            pos += str.length();
        }

        return tokens;
    }
}
