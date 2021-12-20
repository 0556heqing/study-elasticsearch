package com.heqing.search;

import com.luhuiguo.chinese.ChineseUtils;
import com.luhuiguo.chinese.pinyin.PinyinFormat;
import org.junit.Test;

/**
 * @author heqing
 * @date 2021/8/31 10:35
 */
public class TestCompute {

    @Test
    public void testSimilarDegree() {
        String str1 = "阿莫西林胶囊", str2 = "阿莫西林软膏";
        double str = Computeclass.similarDegree(str1, str2);
        System.out.println("1 ---> " + str);

        str1 = ChineseUtils.toSimplified(str1);
        str2 = ChineseUtils.toSimplified(str2);
        str = Computeclass.similarDegree(str1, str2);
        System.out.println("2 ---> " + str);

        str1 = ChineseUtils.toPinyin(str1, PinyinFormat.TONELESS_PINYIN_FORMAT);
        str2 = ChineseUtils.toPinyin(str2, PinyinFormat.TONELESS_PINYIN_FORMAT);
        str = Computeclass.similarDegree(str1, str2);
        System.out.println("3 ---> " + Computeclass.similarityResult(str));
    }
}
