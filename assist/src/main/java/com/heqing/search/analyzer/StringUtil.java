package com.heqing.search.analyzer;

import com.luhuiguo.chinese.ChineseUtils;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串常用操作
 * @author heqing
 * @date 2021/11/18 18:26
 */
public class StringUtil {

    private final static String PUNCTUATIONS = "[\"`~!@#$%^&*()+=|{}':;',.·<>/?~！@#￥%……&*（）_《》——+|{}【】‘；：”“’。，、？\\-]";
    private final static String REG_EX_PUNCTUATION = "[\"`~!@#$%^&*()+=|{}':;',\\[\\].·<>/?~！@#￥%……&*（）_《》——+|{}【】‘；：”“’。，、？\\\\-]";

    private final static Pattern NUMBER_PATTERN = Pattern.compile("[0-9]+(.[0-9]+)?");
    private final static Pattern ALPHABET_PATTERN =  Pattern.compile("[a-zA-Z]+");

    private final static HashSet<Character> PUNCTUATION_SET = new HashSet<>();

    static {
        char[] chars = PUNCTUATIONS.toCharArray();

        for (char c : chars) {
            PUNCTUATION_SET.add(c);
        }
    }

    /**
     * 判断字符是否是标点符号
     *
     * @param c 字母
     * @return 是否是标点符号
     */
    public static boolean isPunctuation(char c) {
        return PUNCTUATION_SET.contains(c);
    }

    /**
     * 判断字符是否是空白字符
     *
     * @param c 字母
     * @return 是否是空白字符
     */
    public static boolean isSpace(char c) {
        return Character.isWhitespace(c);
    }

    /**
     * 是否是中文字符（汉字Unicode编码的区间为：0x4E00→0x9FA5(转)）
     * 参考：https://www.cnblogs.com/chenwenbiao/archive/2011/08/17/2142718.html
     *
     * @param c 字符
     * @return
     */
    public static boolean isChinese(char c) {
        int code = (int) c;
        int chineseUnicodeStart = 0x4E00, chineseUnicodeEnd = 0x9FA5;

        boolean beChinese = (code >= chineseUnicodeStart && code <= chineseUnicodeEnd) && Character.isLetter(c);
        if (beChinese) {
            return true;
        }
        return false;
    }

    /**
     * 判断字符是否是数字
     *
     * @param c 字符
     * @return 是否是数字
     */
    public static boolean isDigital(char c) {
        return Character.isDigit(c);
    }

    /**
     * 判断是否是数字字符串。
     *
     * @param str 字符串
     * @return true 是 | false 否
     */
    public static boolean isNumberString(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        return NUMBER_PATTERN.matcher(str).matches();
    }

    /**
     * 判断字符是否是英文字母
     *
     * @param c 字符
     * @return 是否是英文字母
     */
    public static boolean isAlphabet(char c) {
        int code = (int) c;
        //小写英文字母，半角
        int lowerEnglishHalfStart = 97, lowerEnglishHalfEnd = 122;
        //大写英文字母，半角
        int upperEnglishHalfStart = 65, upperEnglishHalfEnd = 90;
        //小写英文字母，全角
        int lowerEnglishFullStart = 65345, lowerEnglishFullEnd = 65370;
        //大写英文字母，全角
        int upperEnglishFullStart = 65313, upperEnglishFullEnd = 65338;

        boolean beAlphabet = (code >= lowerEnglishHalfStart && code <= lowerEnglishHalfEnd) ||
                (code >= upperEnglishHalfStart && code <= upperEnglishHalfEnd) ||
                (code >= lowerEnglishFullStart && code <= lowerEnglishFullEnd) ||
                (code >= upperEnglishFullStart && code <= upperEnglishFullEnd);
        return beAlphabet;
    }

    /**
     * 判断是否是英文字母字符串。
     *
     * @param str 字符串
     * @return true 是 | false 否
     */
    public static boolean isAlphabetString(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return ALPHABET_PATTERN.matcher(str).matches();
    }

    /**
     * 是否包含中文字符
     *
     * @param strName 字符串
     * @return 是否包含中文字符
     */
    public static boolean hasChinese(String strName) {
        char[] ch = strName.trim().toCharArray();

        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChinese(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否全由中文字符组成
     *
     * @param strName 字符串
     * @return 是否全由中文字符组成
     */
    public static boolean isAllChinese(String strName) {
        boolean containChinese = false;
        char[] ch = strName.trim().toCharArray();

        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isPunctuation(c)) {
                continue;
            }
            if (!isChinese(c)) {
                return false;
            } else {
                containChinese = true;
            }
        }
        return containChinese;
    }

    /**
     * 提取中文字符
     *
     * @param str
     * @return 中文字符组成的串
     */
    public static String extractChinese(String str) {
        StringBuffer sb = new StringBuffer();
        char[] ch = str.trim().toCharArray();

        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChinese(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 将非数字、字母和中文的字符剔除
     * 符号只保留分割用的空格和数字中间的点号
     *
     * @param data 字符串
     * @return 由中文、英文字母、数字和空格组成的字符串
     */
    public static String replaceNotDigitAlphaChineseCharsWithSpace(String data) {
        if (null == data) {
            return "";
        }

        data = data.trim();
        if (data.isEmpty()) {
            return "";
        }

        char[] chars = data.toCharArray();
        StringBuilder sb = new StringBuilder();
        boolean preCharIsSpace = true;

        for (int i = 0; i < chars.length; i++) {
            char ch = chars[i];
            if (isDigital(ch) || isAlphabet(ch) || isChinese(ch)) {
                sb.append(ch);
                preCharIsSpace = false;
            } else {
                if (!preCharIsSpace) {
                    //数字中间的小数点不去掉
                    if (ch == Separators.CHAR_POINT
                            && i > 0 && i < chars.length - 1
                            && isDigital(chars[i - 1])
                            && isDigital(chars[i + 1])) {
                        sb.append(Separators.CHAR_POINT);
                    } else {
                        sb.append(Separators.SPACE);
                        preCharIsSpace = true;
                    }
                }
            }
        }

        if (0 == sb.length()) {
            return "";
        }
        return sb.toString().trim();
    }


    /**
     * 去除所有的非文字符号(除了",","."和空格保留)，并用replaceStr替代
     *
     * @param input
     * @param replaceStr
     * @return
     */
    private static String replaceNotWordWithStr(final String input, String replaceStr) {
        final StringBuilder builder = new StringBuilder();
        for (final char c : input.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                builder.append(Character.isLowerCase(c) ? c : Character.toLowerCase(c));
            } else {
                if (c == Separators.CHAR_MIDDLE_LINE || c == Separators.CHAR_POINT || Character.isSpaceChar(c)) {
                    builder.append(c);
                } else {
                    builder.append(replaceStr);
                }
            }
        }

        String newStr = builder.toString();
        return newStr;
    }

    /**
     * 去除非中文、英文字母和数字的字符，返回剩余三者组成的字符串。
     *
     * @param data 字符串
     * @return 由中文、英文字母和数字的字符组成的字符串
     */
    public static String knockoutNotDigitAlphaChinese(String data) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(data)) {
            return "";
        }

        data = data.replaceAll(Separators.TAB, Separators.SPACE)
                .replaceAll(Separators.DOUBLE_SPACE, Separators.SPACE);
        data = data.trim();

        char[] chars = data.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            char ch = chars[i];

            if (isDigital(ch) || isAlphabet(ch) || isChinese(ch)) {
                sb.append(ch);
            } else if (ch == Separators.CHAR_POINT) {
                //数字中间的小数点不去掉
                if (i > 0 && i < chars.length - 1 && isDigital(chars[i - 1]) && isDigital(chars[i + 1])) {
                    sb.append(Separators.CHAR_POINT);
                }
            } else if (ch == Separators.CHAR_SPACE) {
                //数字中间的小数点不去掉
                if (i > 0 && i < chars.length - 1 && isAlphabet(chars[i - 1]) && isAlphabet(chars[i + 1])) {
                    sb.append(Separators.SPACE);
                }
            }
        }

        if (0 == sb.length()) {
            return "";
        }
        return sb.toString();
    }

    /**
     * 删除字符串中的空白字符。
     *
     * @param data
     * @return
     */
    public static String deleteSpace(String data) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(data)) {
            return "";
        }

        data = data.replaceAll("\\p{Space}", "");
        return data;
    }

    /**
     * 删除标点符号
     *
     * @param data
     * @return
     */
    public static String deletePunctuation(String data) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(data)) {
            return "";
        }

        Pattern p = Pattern.compile(REG_EX_PUNCTUATION);
        Matcher m = p.matcher(data);
        return m.replaceAll("").trim();
    }

    /**
     * 去掉标点符号，小数点保留。
     *
     * @param data 字符串
     * @return
     */
    public static String removePunctions(String data) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(data)) {
            return "";
        }

        data = data.trim();
        if (org.apache.commons.lang3.StringUtils.isEmpty(data)) {
            return "";
        }

        char[] chars = data.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            char ch = chars[i];

            if (!isPunctuation(ch) && (ch != ' ')) {
                sb.append(ch);
            } else if (ch == '.') {
                //数字中间的小数点不去掉
                if (i > 0 && i < chars.length - 1 && isDigital(chars[i - 1]) && isDigital(chars[i + 1])) {
                    sb.append('.');
                }
            }
        }

        if (0 == sb.length()) {
            return "";
        }
        return sb.toString();
    }

    /**
     * 用空格替换标点符号，小数点保留。
     *
     * @param data 字符串
     * @return
     */
    public static String replacePunctionsWithSpace(String data) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(data)) {
            return "";
        }

        data = data.trim();
        if (org.apache.commons.lang3.StringUtils.isEmpty(data)) {
            return "";
        }

        char[] chars = data.toCharArray();
        StringBuilder sb = new StringBuilder();
        boolean preCharIsSpace = true;

        for (int i = 0; i < chars.length; i++) {
            char ch = chars[i];
            if (!isPunctuation(ch) && (ch != ' ')) {
                sb.append(ch);
                preCharIsSpace = false;
            } else {
                if (!preCharIsSpace) {
                    //数字中间的小数点不去掉
                    if (ch == '.' && i > 0 && i < chars.length - 1 && isDigital(chars[i - 1]) && isDigital(chars[i + 1])) {
                        sb.append('.');
                    } else {
                        sb.append(' ');
                        preCharIsSpace = true;
                    }
                }
            }
        }

        if (0 == sb.length()) {
            return "";
        }
        return sb.toString().trim();
    }

    /**
     * 判断是否仅包含中文、英文、和数字字符（可以包含标点符号，但不能仅含有标点符号）。
     *
     * @param str                字符串
     * @param mustContainChinese 是否必须包含中文
     * @return true | false
     */
    public static boolean isAllCdaString(String str, boolean mustContainChinese) {
        boolean containChinese = false;
        boolean containAlphabetDigital = false;

        if (str == null || str.isEmpty()) {
            return false;
        }

        char[] ch = str.trim().toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];

            if (isPunctuation(c) || isSpace(c)) {
                continue;
            }
            if (isDigital(c) || isAlphabet(c)) {
                containAlphabetDigital = true;
            } else if (!isChinese(c)) {
                return false;
            } else {
                containChinese = true;
            }
        }

        //如果必须包含中文，但没有包含中文，则返回false
        if (mustContainChinese && !containChinese) {
            return false;
        }
        return containAlphabetDigital || containChinese;
    }

    /**
     * 对空格进行删除，仅保留数字之间或字母之间的空格
     *
     * @param data 字符串
     * @return 删除空格之后的字符串
     */
    public static String removeSpaceNotWithinEnglishOrDigit(String data) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(data)) {
            return "";
        }

        data = data.trim();
        if (org.apache.commons.lang3.StringUtils.isEmpty(data)) {
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();
        char[] chars = data.toCharArray();
        stringBuilder.append(chars[0]);

        for (int i = 1; i < chars.length - 1; i++) {
            char pre = chars[i-1];
            char cur = chars[i];
            char next = chars[i+1];
            if (isSpace(cur)) {
                //仅保留数字之间或字母之间的空格
                boolean beKeep = (isDigital(pre) && isDigital(next)) || (isAlphabet(pre) && isAlphabet(next));
                if (beKeep) {
                    stringBuilder.append(cur);
                }
            } else {
                stringBuilder.append(cur);
            }
        }

        stringBuilder.append(chars[chars.length-1]);
        String dealt = stringBuilder.toString();
        if (!data.equals(dealt)) {
            System.out.println(String.format("修改， %s vs %s", data, dealt));
        }
        return dealt;
    }

    /**
     * 计算字符串的长度，连续的字母为一个单词，连续的数字为一个，单个汉字为一个，空格不算长度。
     *
     * @param query 字符串
     * @return 字符串长度
     */
    public static int lengthOf(String query) {
        if (null == query) {
            return 0;
        }
        if (query.length() <= 1) {
            return query.trim().length();
        }

        query += " ";
        char[] chars = query.toCharArray();
        int lenght = 0;
        for (int i = 0; i < chars.length - 1; i++) {
            if (isSpace(chars[i])) {
                continue;
            }

            if (isChinese(chars[i])) {
                lenght++;
            } else if (isDigital(chars[i]) && !isDigital(chars[i + 1])) {
                lenght++;
            } else if (isAlphabet(chars[i]) && !isAlphabet(chars[i + 1])) {
                lenght++;
            }
        }
        return lenght;
    }

    /**
     * 计算字符串的长度，连续的字母为一个单词，连续的数字为一个，单个汉字为一个，空格不算长度。
     *
     * @param query   字符串
     * @param limit   截断长度
     * @param forWord true：按单词长度截断 | false：按字符长度截断
     * @return 字符串长度
     */
    public static String cutoff(String query, int limit, boolean forWord) {
        if (null == query || limit <= 0) {
            return query;
        }

        query = query.trim();
        if (!forWord) {
            //截断query
            if (query.length() > limit) {
                query = query.substring(0, limit);
            }
        } else {
            query += " ";
            char[] chars = query.toCharArray();
            StringBuilder sb = new StringBuilder();

            int length = 0;
            for (int i = 0; i < chars.length - 1; i++) {
                if (length >= limit) {
                    break;
                }
                sb.append(chars[i]);
                if (isSpace(chars[i])) {
                    continue;
                }
                if (isChinese(chars[i])) {
                    length++;
                } else if (isDigital(chars[i]) && !isDigital(chars[i + 1])) {
                    length++;
                } else if (isAlphabet(chars[i]) && !isAlphabet(chars[i + 1])) {
                    length++;
                }
            }
            query = sb.toString().trim();
        }
        return query;
    }

    /**
     * 全角转半角
     *
     * @param inputStr 输入字符串
     * @return 半角字符串
     */
    public static String full2Half(String inputStr) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(inputStr)) {
            return inputStr;
        }

        //编码集
        String charsetName = "unicode";
        //全角字符的标志
        int fullSign = -1;
        //全角转半角的因子
        int fullHalfDiff = 32;

        StringBuffer outSb = new StringBuffer("");
        String tmpStr = "";

        byte[] b;
        for (int i = 0; i < inputStr.length(); i++) {
            tmpStr = inputStr.substring(i, i + 1);
            if (Separators.SPACE.equals(tmpStr)) {
                outSb.append(Separators.SPACE);
                continue;
            }
            try {
                b = tmpStr.getBytes(charsetName);
                if (b[2] == fullSign) {
                    //表示全角
                    b[3] = (byte) (b[3] + fullHalfDiff);
                    b[2] = 0;
                    outSb.append(new String(b, charsetName));
                } else {
                    outSb.append(tmpStr);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return outSb.toString();
    }

    /**
     * 繁体转简体
     * @param tradStr
     * @return
     */
    private static String traditionalToSimple(String tradStr) {
        String simplifiedStr = ChineseUtils.toSimplified(tradStr);
        return simplifiedStr;
    }

    /**
     * 对字符串进行规则化，大写转小写，繁体转简体，全角转半角。如果replaceWithWhiteSpace指定为true，则非中文、英文字母和数字的字符串替换
     * 为空格，返回剩余三者和空格组成的字符串。如果出现连续多个空格，则仅保留一个。如果replaceWithWhiteSpace指定为false，则非中文、
     * 英文字母和数字的字符串被删除，返回剩余三者组成的字符串。不能以空格开头和结尾。
     * 1. 非数字、字母和中文的符号进行处理   剔除或空格替换
     * 2. 全角转半角
     * 3. 大写转小写
     * 4. 繁体转简体
     *
     * @param inputStr              字符串
     * @param replaceWithWhiteSpace 非中文、英文字母和数字的字符串替换为空格
     * @return 规则化后的字符串
     */
    public static String normalize(String inputStr, boolean replaceWithWhiteSpace) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(inputStr)) {
            return "";
        }

        inputStr = inputStr.trim();
        if (org.apache.commons.lang3.StringUtils.isEmpty(inputStr)) {
            return "";
        }

        if (replaceWithWhiteSpace) {
            // 由中文、英文字母、数字和空格组成的字符串
            inputStr = replaceNotDigitAlphaChineseCharsWithSpace(inputStr);
        } else {
            // 返回中文、英文字母和数字的字符组成的字符串
            inputStr = knockoutNotDigitAlphaChinese(inputStr);
        }

        if (org.apache.commons.lang3.StringUtils.isEmpty(inputStr)) {
            return "";
        }
        // 全角转半角
        inputStr = full2Half(inputStr);
        // 繁体转简体
        inputStr = traditionalToSimple(inputStr);
        // 大写转小写
        inputStr = inputStr.toLowerCase();
        return inputStr;
    }


    public static String normalServerString(String str) {
        String normalStr = str;
        normalStr = full2Half(normalStr);
        normalStr = traditionalToSimple(normalStr);
        normalStr = normalStr.toLowerCase();
        normalStr = replaceNotWordWithStr(normalStr, Separators.SPACE);
        return normalStr;
    }

    public static String normalTrainString(String str) {
        String normalStr = str;
        normalStr = full2Half(normalStr);
        normalStr = traditionalToSimple(normalStr);
        normalStr = normalStr.toLowerCase();
        normalStr = replaceNotWordWithStr(normalStr, Separators.TAB);
        //这条语句保证标点符号保留下来
        normalStr = normalStr.replaceAll("\t\t", Separators.TAB);
        //任何连续两个空白字符，都替换为空格
        normalStr = normalStr.replaceAll("[\\s]{2,}", Separators.SPACE);
        normalStr = normalStr.replaceAll("  ", Separators.SPACE);
        normalStr = normalStr.trim();
        return normalStr;
    }
}
