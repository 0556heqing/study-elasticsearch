package com.heqing.search.analyzer;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.*;

/**
 * @author heqing
 */
public class PinyinUtil {

    /**
     * 每个汉字最多保留多少个多音
     */
    private final static int MAX_KEEP_PINYIN_PER_WORD = 10;

    /**
     * 字符串集合转换字符串(逗号分隔)
     *
     * @param src
     * @return
     * @author wyh
     */
    public static String makeStringByStringSet(String src) {
        List<String> list = new ArrayList<String>();

        Set<String> stringSet = getPinyin(src);

        if (null != stringSet) {
            list.addAll(stringSet);
            Set<String> pinyinHeadChar = getPinyinHeadChar(src);

            if (null != pinyinHeadChar) {
                list.addAll(pinyinHeadChar);
            }
        }

        StringBuilder sb = new StringBuilder();
        int i = 0;

        for (String s : list) {
            if (i == list.size() - 1) {
                sb.append(s);
            } else {
                sb.append(s + ",");
            }

            i++;
        }

        return sb.toString().toLowerCase();
    }

    /**
     * 获取拼音集合
     *
     * @param src
     * @return Set<String>
     * @author wyh
     */
    public static Set<String> getPinyin(String src) {
        if (src != null && !"".equalsIgnoreCase(src.trim())) {
            char[] srcChar;
            srcChar = src.toCharArray();
            //汉语拼音格式输出类
            HanyuPinyinOutputFormat hanYuPinOutputFormat = new HanyuPinyinOutputFormat();

            //输出设置，大小写，音标方式等
            hanYuPinOutputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
            hanYuPinOutputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
            hanYuPinOutputFormat.setVCharType(HanyuPinyinVCharType.WITH_V);

            String[][] temp = new String[src.length()][];
            for (int i = 0; i < srcChar.length; i++) {
                char c = srcChar[i];
                //是中文或者a-z或者A-Z转换拼音
                if (String.valueOf(c).matches("[\\u4E00-\\u9FA5]+")) {
                    try {
                        temp[i] = PinyinHelper.toHanyuPinyinStringArray(srcChar[i], hanYuPinOutputFormat);
                        temp[i] = deDuplicationAndLimitPinyinCount(temp[i], MAX_KEEP_PINYIN_PER_WORD);
                    } catch (BadHanyuPinyinOutputFormatCombination e) {
                        e.printStackTrace();
                    }
                } else {
                    temp[i] = new String[]{String.valueOf(srcChar[i])};
                }
            }
            String[] pingyinArray = exchange(temp);
            Set<String> pinyinSet = new HashSet<String>();
            for (int i = 0; i < pingyinArray.length; i++) {
                pinyinSet.add(pingyinArray[i]);
            }
            return pinyinSet;
        }
        return null;
    }

    /**
     * 获取拼音首字母集合
     *
     * @param src
     * @return Set<String>
     * @author wyh
     */
    public static Set<String> getPinyinHeadChar(String src) {
        if (src != null && !"".equalsIgnoreCase(src.trim())) {
            char[] srcChar;
            srcChar = src.toCharArray();
            //汉语拼音格式输出类
            HanyuPinyinOutputFormat hanYuPinOutputFormat = new HanyuPinyinOutputFormat();

            //输出设置，大小写，音标方式等
            hanYuPinOutputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
            hanYuPinOutputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
            hanYuPinOutputFormat.setVCharType(HanyuPinyinVCharType.WITH_V);

            String[][] temp = new String[src.length()][];
            for (int i = 0; i < srcChar.length; i++) {
                char c = srcChar[i];
                //是中文或者a-z或者A-Z转换拼音(我的需求，是保留中文或者a-z或者A-Z)
                if (String.valueOf(c).matches("[\\u4E00-\\u9FA5]+")) {
                    try {
                        String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(srcChar[i], hanYuPinOutputFormat);
                        pinyins = deDuplicationAndLimitPinyinCount(pinyins, MAX_KEEP_PINYIN_PER_WORD);

                        if (null != pinyins && 0 != pinyins.length) {
                            temp[i] = new String[pinyins.length];

                            for (int k = 0; k < pinyins.length; k++) {
                                temp[i][k] = String.valueOf(pinyins[k].charAt(0));
                            }
                        } else {
                            //说明有奇怪的字，无法识别
                            return null;
                        }
                    } catch (BadHanyuPinyinOutputFormatCombination e) {
                        e.printStackTrace();
                    }
                } else {
                    temp[i] = new String[]{String.valueOf(srcChar[i])};
                }
            }
            String[] pingyinArray = exchange(temp);
            Set<String> pinyinSet = new HashSet<String>();
            for (int i = 0; i < pingyinArray.length; i++) {
                pinyinSet.add(pingyinArray[i]);
            }
            return pinyinSet;
        }
        return null;
    }

    /**
     * 每个汉字只保留keepCount个拼音
     *
     * @param src
     * @param keepCount
     * @return
     */
    private static String[] deDuplicationAndLimitPinyinCount(String[] src, int keepCount) {
        if (null == src || src.length <= 1) {
            return src;
        }

        Set<String> tmpSet = new HashSet<>(Arrays.asList(src));
        List<String> tmpList = new ArrayList<>(tmpSet);
        if (tmpList.size() > keepCount) {
            tmpList = tmpList.subList(0, keepCount + 1);
        }
        String[] tmpArr = new String[tmpList.size()];
        tmpList.toArray(tmpArr);
        return tmpArr;
    }

    /**
     * 递归
     *
     * @param strJaggedArray
     * @return
     * @author wyh
     */
    public static String[] exchange(String[][] strJaggedArray) {
        String[][] temp = doExchange(strJaggedArray);
        return temp[0];
    }

    /**
     * 递归
     *
     * @param strJaggedArray
     * @return
     * @author wyh
     */
    private static String[][] doExchange(String[][] strJaggedArray) {
        int len = strJaggedArray.length;
        int minLen = 2;
        if (len >= minLen) {
            int len1 = strJaggedArray[0].length;
            int len2 = strJaggedArray[1].length;
            int newlen = len1 * len2;
            String[] temp = new String[newlen];
            int index = 0;
            for (int i = 0; i < len1; i++) {
                for (int j = 0; j < len2; j++) {
                    temp[index] = strJaggedArray[0][i] + strJaggedArray[1][j];
                    index++;
                }
            }
            String[][] newArray = new String[len - 1][];
            for (int i = minLen; i < len; i++) {
                newArray[i - 1] = strJaggedArray[i];
            }
            newArray[0] = temp;
            return doExchange(newArray);
        } else {
            return strJaggedArray;
        }
    }
}