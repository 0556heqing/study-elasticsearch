package com.heqing.search.segment.extend;

import java.util.ArrayList;
import java.util.List;

/**
 * 分词扩展功能
 * 停用词，同义词，级联词汇，上位词
 * @author liuxiangqian
 */
public enum ExtendType {
    /**
     * 停用词启用标志
     */
    STOP(1),
    /**
     * 同义词功能启用标志
     */
    SYNONYM(2),
    /**
     * 级联词汇启用标志
     */
    CASCADE(3),
    /**
     * 上位词启用标志
     */
    HYPERNYM(4);

    private final int extendType;

    ExtendType(int extendType) {
        this.extendType = extendType;
    }

    public int getExtendType() {
        return extendType;
    }

    public static void main(String[] args) {
        List<ExtendType> extendTypeList = new ArrayList<>();
        extendTypeList.add(ExtendType.STOP);
        extendTypeList.add(ExtendType.SYNONYM);
        extendTypeList.add(ExtendType.CASCADE);
        extendTypeList.add(ExtendType.HYPERNYM);

        extendTypeList.forEach(type -> {
            System.out.println(type);
        });
    }
}
