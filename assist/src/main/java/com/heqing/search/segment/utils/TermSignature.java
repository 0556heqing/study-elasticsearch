package com.heqing.search.segment.utils;

import java.security.MessageDigest;


/**
 * @author liuxiangqian
 * @version 1.0
 * @className TermSignature
 * @description 字符串编码为long型
 * @date 2019/6/13 17:19
 **/
public class TermSignature {
    public final static long signatureTerm(String s) {
        try {
            byte[] btInput = s.getBytes("utf-8");
            //获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            //使用指定的字节更新摘要
            mdInst.update(btInput);
            //获得密文
            byte[] md = mdInst.digest();
            byte[] reduceMd = new byte[8];

            //byte位置进行压缩
            for (int i = 0, step = 2; i < md.length; i += step) {
                reduceMd[i / 2] = (byte) (md[i] + md[i + 1]);
            }

            //把密文转换成长整形
            int j = reduceMd.length;
            long retNumber = 0;
            for (int i = 0; i < j; i++) {
                long oneByte = reduceMd[i];
                int bitNumber = i * 8;
                retNumber += (oneByte << bitNumber);
            }

            if (retNumber < 0) {
                retNumber = 0 - retNumber;
            }

            return retNumber;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void main(String[] args) {
        System.out.print(TermSignature.signatureTerm("地方"));
    }
}
