package com.heqing.search.segment.probability;

import com.heqing.search.analyzer.Separators;
import com.heqing.search.segment.common.Term;
import com.heqing.search.segment.common.TermVec;
import com.heqing.search.segment.core.npath.Nsp;
import com.heqing.search.segment.core.npath.VertexNode;
import com.heqing.search.segment.utils.LoadFileDataUtil;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @author suke
 * @version 1.0
 * @description
 * @date 2020/8/12 11:17
 **/
@Slf4j
public class ProbabilityModel {
    private final static int SENTENCE_MAX_LEN = 250;
    /**
     * 概率计算模型
     */
    NgramModle ngramModle = null;
    private Nsp nsp = null;
    /**
     * 标点符号集合 排除'-' '-'作为型号的一部分，所以需要当成英文字符
     */
    private Set<String> punctuationSet = new HashSet<>();

    /**
     * 初始化： 加载数据   NPath： 搜集多少级别的路径
     *
     * @param datapath
     * @param nPath
     * @param ngram
     */
    public void init(String datapath, int nPath, int ngram) {
        log.info("分词初始化开始............");
        long startInit = System.currentTimeMillis();

        datapath = datapath.trim();
        char c = datapath.charAt(datapath.length() - 1);
        if (Separators.CHAR_SLASH != c) {
            datapath += Separators.SLASH;
        }

        //NSP初始化
        nsp = new Nsp(nPath);
        nsp.init(datapath);

        //NGram模型数据加载
        ngramModle = new NgramModle(ngram);
        ngramModle.loadModleData(datapath);

        //加载需要注意的数据
        String punctuationPath = datapath + "punctuation.data";
        LoadFileDataUtil.loadSingleWordToSet(punctuationPath, punctuationSet, 1, 1);

        long endInit = System.currentTimeMillis();
        long chaInit = endInit - startInit;
        log.info("分词初始化总时间： " + chaInit / 1000 + " 秒");
    }


    private String probabilitySegmentQuick(String chineseStr) {
        String ret = "";
        //步骤一： 构图
        List<VertexNode> graph = nsp.createGraph(chineseStr, ngramModle);

        //步骤二： 计算每个节点的N个最优前趋
        List<Nsp.Path> verPath = nsp.computeTopnBestPathForEveryVertex(graph, ngramModle);

        //步骤三： 回退计算N条最优路径
        List<String> nPathVec = nsp.getTopnBestPath(verPath);

        //步骤四： 计算最可能的路径
        double maxValue = Double.MAX_VALUE;
        //最可能的切分串
        String mostMatchStr = "";

        for (String cStr : nPathVec) {
            double pro = computeProbility(cStr);
            if (pro < maxValue) {
                maxValue = pro;
                mostMatchStr = cStr;
            }
        }

        return mostMatchStr;
    }


    private double computeProbility(String chineseStr) {
        return ngramModle.computeSentenceProbility(chineseStr);
    }

    /**
     * N 最优路径  综合切分结果  相当于全切分
     *
     * @param chineseStr
     * @return
     */
    private List<String> nShortestPathSplit(String chineseStr) {
        if (chineseStr.length() > SENTENCE_MAX_LEN) {
            log.info("全切分字段:" + chineseStr);
            chineseStr = chineseStr.substring(0, SENTENCE_MAX_LEN);
        }

        //步骤一： 构图
        List<VertexNode> graph = nsp.createGraph(chineseStr);

        //步骤二： 计算每个节点的N个最优前趋
        List<Nsp.Path> verPath = nsp.computeTopnBestPathForEveryVertex(graph);

        //步骤三： 回退计算N条最优路径
        List<String> nPathVec = nsp.getTopnBestPath(verPath);

        return nPathVec;
    }

    /**
     * @param c
     * @return 0: 中文和标点符号  1：数字   2： 英文串
     */
    private int charCat(char c) {
        //必须保证不为空
        if (Character.isDigit(c)) {
            return 1;
        }

        char a = 'a', z = 'z';
        if (c >= a && c <= z) {
            return 2;
        }

        return 0;
    }


    /**
     * @param str
     * @return 0: 中文   1：数字  2：英文
     */
    private int stringCat(String str) {
        //判断字符串类型
        char c = str.charAt(0);
        return charCat(c);
    }

    public HzPySegPos segHzAndPy(String query) {
        HzPySegPos ret = new HzPySegPos();
        if (query == null || query.length() == 0) {
            return ret;
        }

        //步骤一：将标点符号用空格代替（除了数字中间的.号）
        //苹果hf.l手机l-10 ---> 苹果hf l手机l 10
        String newQuery = replacePunctuationWitchSpace(query);

        //步骤二：中文、数字和英文字符自然切分收集
        String[] arr = newQuery.split(Separators.SPACE);
        int pos = 0;
        for (int i = 0; i < arr.length; i++) {
            String curStr = arr[i];
            if (i > 0) {
                //加1： 是因为自然切分标志 空格 占据一个位置
                //当前处理串的起始为止
                pos += 1;
            }

            //中文、数字和英文字符之间添加','号分割
            String tmpStr = splitChineseDigitAndEnglishWithComma(curStr);

            // 中文，英文和数字分割开
            pos = naturalSplitStrAndCollect(ret, tmpStr, pos);
        }

        return ret;
    }

    /**
     * 将字符串中的标点用空格替换
     *
     * @param str 字符串
     * @return
     */
    private String replacePunctuationWitchSpace(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            String tmp = str.substring(i, i + 1);

            //标点符号的处理
            if (punctuationSet.contains(tmp)) {
                if (Separators.POINT.equals(tmp)) {
                    if (i == 0 || i == str.length() - 1) {
                        sb.append(Separators.SPACE);
                    } else {
                        char pre = str.charAt(i - 1);
                        char next = str.charAt(i + 1);

                        if (charCat(pre) == 1 && charCat(next) == 1) {
                            sb.append(Separators.CHAR_POINT);
                        } else {
                            sb.append(Separators.SPACE);
                        }
                    }
                } else {
                    sb.append(Separators.SPACE);
                }
            } else {
                sb.append(tmp);
            }
        }


        return sb.toString();
    }

    /**
     * 将中文，英文和数字用逗号隔开
     *
     * @param str
     * @return
     */
    private String splitChineseDigitAndEnglishWithComma(String str) {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < str.length(); j++) {
            if (j == 0 || str.charAt(j) == Separators.CHAR_MIDDLE_LINE || str.charAt(j) == Separators.CHAR_POINT) {
                sb.append(str.charAt(j));
            } else {
                char pre = str.charAt(j - 1);
                char now = str.charAt(j);
                boolean diffWithPre = (charCat(pre) != charCat(now));
                boolean preIsNotCommaAndHorizontalBar = (pre != Separators.CHAR_POINT && pre != Separators.CHAR_MIDDLE_LINE);
                if (diffWithPre && preIsNotCommaAndHorizontalBar) {
                    sb.append(Separators.COMMA).append(str.charAt(j));
                } else {
                    sb.append(str.charAt(j));
                }
            }
        }

        return sb.toString();
    }

    /**
     * 对当前串进行搜集
     *
     * @param ret    收集结构
     * @param tmpStr 处理的串
     * @param pos    处理字符串的在原始串中的起始为止
     * @return 原始串中该串后一个为止
     */
    private int naturalSplitStrAndCollect(HzPySegPos ret, String tmpStr, int pos) {
        String[] curArr = StringUtils.split(tmpStr, Separators.COMMA);
        for (int j = 0; j < curArr.length; j++) {
            //判断是什么字符串
            if (curArr[j].length() == 0) {
                continue;
            }

            if (stringCat(curArr[j]) == 0) {
                //中文串
                Term term = new Term();
                term.start = pos;
                term.len = curArr[j].length();
                term.word = curArr[j];
                pos += term.len;

                ret.hzList.add(term);
                continue;
            }

            if (stringCat(curArr[j]) == 1) {
                //数字串
                Term term = new Term();
                term.start = pos;
                term.len = curArr[j].length();
                term.word = curArr[j];
                pos += term.len;

                ret.numList.add(term);
                continue;
            }

            //英文串
            Term term = new Term();
            term.start = pos;
            term.len = curArr[j].length();
            term.word = curArr[j];
            pos += term.len;

            ret.pyList.add(term);
        }

        return pos;
    }


    public Map<String, TermVec> productTitleSegmentPos(String title) {
        Map<String, TermVec> resultMap = new HashMap<>(8);
        if (title == null || title.length() == 0) {
            return resultMap;
        }

        //步骤一： 中文和英文数字分开
        HzPySegPos ret = segHzAndPy(title);

        //步骤二： 中文切分{强制切分先不要}
        List<Term> hzVec = ret.hzList;
        for (Term term : hzVec) {
            chineseSplitForTitle(term, resultMap);
        }

        //步骤三： 英文切分结果
        List<Term> pyVec = ret.pyList;
        for (Term term : pyVec) {
            englishSplitForTitle(term, resultMap);
        }

        //步骤四：数字且分结果
        List<Term> nVec = ret.numList;
        for (Term term : nVec) {
            digitSplitForTitle(term, resultMap);
        }

        return resultMap;
    }

    /**
     * 用户query切分
     *
     * @param query
     * @return
     */
    public HashMap<String, TermVec> querySegmentQuick(String query) {
        HashMap<String, TermVec> tvMap = new HashMap<>(10);

        if (query == null || query.length() == 0) {
            return tvMap;
        }

        //步骤一： 中文和英文数字分开
        HzPySegPos ret = segHzAndPy(query);

        //步骤二： 中文切分{强制切分先不要}
        for (Term term : ret.hzList) {
            String hzStr = term.word;
            int pos = term.start;

            //概率切分结果
            String pSplitStr = probabilitySegmentQuick(hzStr);
            String[] spArr = pSplitStr.split(Separators.TAB);
            for (int i = 0; i < spArr.length; i++) {
                String word = spArr[i];
                int len = word.length();
                tvMap.computeIfAbsent(word, t -> new TermVec(t, t.length())).startPosSet.add(pos);

                pos += len;
            }
        }

        //步骤三： 英文切分结果
        for (Term term : ret.pyList) {
            String pinyin = term.word;
            //一个英文传切分
            String[] arr = pinyin.split(Separators.MIDDLE_LINE);
            List<Integer> sList = new ArrayList<>();
            int start = term.start;
            for (int i = 0; i < arr.length; i++) {
                sList.add(start);
                start += arr[i].length() + 1;
            }

            for (int i = 0; i < arr.length; i++) {
                int pos = sList.get(i);
                String word = arr[i];
                int len = word.length();
                tvMap.computeIfAbsent(word, t -> new TermVec(word, len)).startPosSet.add(pos);
            }
        }

        //步骤三：数字切分
        for (Term term : ret.numList) {
            String word = term.word;
            int pos = term.start;
            int len = word.length();

            tvMap.computeIfAbsent(word, t -> new TermVec(word, len)).startPosSet.add(pos);
        }

        return tvMap;
    }

    private void chineseSplitForTitle(Term term, Map<String, TermVec> resultMap) {
        String tmpStr = term.word;
        List<String> pathVec = nShortestPathSplit(tmpStr);

        for (String pSplitStr : pathVec) {
            String[] spArr = pSplitStr.split(Separators.TAB);
            int pos = term.start;
            for (int j = 0; j < spArr.length; j++) {
                resultMap.computeIfAbsent(spArr[j], t -> new TermVec(t, t.length())).startPosSet
                        .add(pos);
                pos += spArr[j].length();
            }
        }
    }

    private void englishSplitForTitle(Term term, Map<String, TermVec> resultMap) {
        //强制切分  品牌词汇
        String pinyin = term.word;
        int pos = term.start;
        String[] arr = pinyin.split(Separators.MIDDLE_LINE);
        for (int i = 0; i < arr.length; i++) {
            String word = arr[i];
            resultMap.computeIfAbsent(arr[i], t -> new TermVec(word, word.length())).startPosSet.add(pos);
            pos += arr[i].length() + 1;
        }

        //有"-"串进行拼接
        if (pinyin.contains(Separators.MIDDLE_LINE)) {
            String[] hArr = pinyin.split(Separators.MIDDLE_LINE);

            //只存在两个数字或英文紧邻的情况才允许合并成词
            int mergeLen = 2;
            //对紧邻的相同数字串或者英文串，进行合并成词
            if (hArr.length == mergeLen && stringCat(hArr[0]) == stringCat(hArr[1])) {
                String npy = pinyin.replaceAll(Separators.MIDDLE_LINE, Separators.EMPTY_STRING);
                resultMap.computeIfAbsent(npy, t -> new TermVec(npy, npy.length())).startPosSet.add(term.start);
            }
        }
    }

    private void digitSplitForTitle(Term term, Map<String, TermVec> resultMap) {
        String word = term.word;
        String tmpStr = word;
        word = word.replaceAll(Separators.MIDDLE_LINE, Separators.EMPTY_STRING);
        int pos = term.start;
        resultMap.computeIfAbsent(word, t -> new TermVec(t, t.length())).startPosSet.add(pos);

        word = tmpStr;
        if (word.contains(Separators.POINT) || word.contains(Separators.MIDDLE_LINE)) {
            word = word.replace(Separators.POINT, Separators.WELL_NUMBER);
            word = word.replace(Separators.MIDDLE_LINE, Separators.WELL_NUMBER);
            String[] numArr = word.split(Separators.WELL_NUMBER);
            for (int i = 0; i < numArr.length; i++) {
                if (i > 0) {
                    pos++;
                }

                String nnum = numArr[i];
                resultMap.computeIfAbsent(nnum, t -> new TermVec(t, t.length())).startPosSet.add(pos);
                if (!resultMap.containsKey(nnum)) {
                    pos += nnum.length();
                }
            }
        }
    }

    public static class HzPySegPos {
        public List<Term> hzList = new ArrayList<>();
        public List<Term> pyList = new ArrayList<>();
        public List<Term> numList = new ArrayList<>();
    }
}
