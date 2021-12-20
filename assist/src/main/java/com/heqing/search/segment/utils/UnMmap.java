package com.heqing.search.segment.utils;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

/**
 * @author liuxiangqian
 * @version 1.0
 * @className UnMmap
 * @description 内存映射取消
 * @date 2019/6/13 17:20
 **/
public class UnMmap {
    public static void unMmap(ByteBuffer bb) {
        if (null==bb || !bb.isDirect()) {
            return;
        }

        // we could use this type cast and call functions without reflection code,
        // but static import from sun.* package is risky for non-SUN virtual machine.
        //try { ((sun.nio.ch.DirectBuffer)cb).cleaner().clean(); } catch (Exception ex) { }
        try {
            Method cleaner = bb.getClass().getMethod("cleaner");
            cleaner.setAccessible(true);
            Method clean = Class.forName("sun.misc.Cleaner").getMethod("clean");
            clean.setAccessible(true);
            clean.invoke(cleaner.invoke(bb));
        } catch (Exception ex) {
        }
        bb = null;
    }
}
