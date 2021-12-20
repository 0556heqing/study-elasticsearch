package com.heqing.elasticsearch.util;

import com.hankcs.hanlp.HanLP;

/**
 * @author heqing
 * @date 2021/7/28 16:15
 */
public class HanlpTool {

    public static String convertToPinyinString(String text) {
        text = HanLP.convertToPinyinString(text, " ", true);
        String[] word = text.split(" ");
        StringBuffer buff = new StringBuffer();
        String[] var3 = word;
        int var4 = word.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            String str = var3[var5];
            if (!"none".equals(str)) {
                buff.append(str);
            }
        }

        return buff.toString();
    }

    public static String convertToPinyinFirstCharString(String text) {
        text = HanLP.convertToPinyinFirstCharString(text, "", true);
        text = text.replaceAll(" ", "");
        return text;
    }
}
