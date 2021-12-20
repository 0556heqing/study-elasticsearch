package com.heqing.search;

import com.github.stuxuhai.jpinyin.PinyinException;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import com.heqing.search.analyzer.PinyinUtil;
import com.luhuiguo.chinese.ChineseUtils;
import com.luhuiguo.chinese.pinyin.PinyinFormat;
import org.junit.Test;

import java.util.Set;

/**
 * @author heqing
 * @date 2021/8/27 18:29
 */
public class TestChinese {

    String strSimplified = "资料库", strTraditional = "資料庫", strSimpl = "贺", strTradit = "賀";

    @Test
    public void test() {
        String str1 = "美宝湿润烧伤膏", str2 = "美寳湿润烧伤膏";
        System.out.println(str1 + " --> " + ChineseUtils.toTraditional(str1));
        System.out.println(str2 + " --> " +  ChineseUtils.toSimplified(str2));

    }

    @Test
    public void testToSimplified() {
        // 转简体
        System.out.println(strTraditional + " --> " + ChineseUtils.toSimplified(strTraditional));
        System.out.println(strTradit + " --> " + ChineseUtils.toSimplified(strTradit));
    }

    @Test
    public void testToTraditional() {
        // 转繁体
        System.out.println(strSimplified + "--> " + ChineseUtils.toTraditional(strSimplified));
        System.out.println(strSimpl + "--> " + ChineseUtils.toTraditional(strSimpl));
    }

    @Test
    public void testToPinyin() {
        // 转拼音
        System.out.println(strSimplified + " --> " + ChineseUtils.toPinyin(strSimplified));
        System.out.println(strSimplified + " --> " + ChineseUtils.toPinyin(strSimplified, PinyinFormat.UNICODE_PINYIN_FORMAT));
        System.out.println(strSimplified + " --> " + ChineseUtils.toPinyin(strSimplified, PinyinFormat.ABBR_PINYIN_FORMAT));
        System.out.println(strSimplified + " --> " + ChineseUtils.toPinyin(strSimplified, PinyinFormat.TONELESS_PINYIN_FORMAT));
    }

    String str = "的确";

    @Test
    public void testGetPinyin() {
        Set<String> pyList = PinyinUtil.getPinyin(str);
        pyList.forEach(System.out::println);
    }

    @Test
    public void testGetPinyinHeadChar() {
        Set<String> pyList = PinyinUtil.getPinyinHeadChar(str);
        pyList.forEach(System.out::println);
    }

    @Test
    public void testMakeStringByStringSet() {
        System.out.println(PinyinUtil.makeStringByStringSet(str));
    }


    @Test
    public void testPinyin() {
        String str = "贺小白";
        try {
            String mark = PinyinHelper.convertToPinyinString(str, ",", com.github.stuxuhai.jpinyin.PinyinFormat.WITH_TONE_MARK);
            System.out.println("mark --> " + mark);

            String number = PinyinHelper.convertToPinyinString(str, ",", com.github.stuxuhai.jpinyin.PinyinFormat.WITH_TONE_NUMBER);
            System.out.println("number --> " + number);

            String tone = PinyinHelper.convertToPinyinString(str, ",", com.github.stuxuhai.jpinyin.PinyinFormat.WITHOUT_TONE);
            System.out.println("tone --> " + tone);

            String shortPy = PinyinHelper.getShortPinyin(str);
            System.out.println("shortPy --> " + shortPy);
        } catch (PinyinException e) {
            e.printStackTrace();
        }
    }
}
