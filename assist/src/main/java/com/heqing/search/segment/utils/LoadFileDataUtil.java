package com.heqing.search.segment.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;

/**
 * @author suke
 * @version 1.0
 * @className LoadFileDataUtil
 * @description
 * @date 2020/8/12 11:17
 **/
@Slf4j
public class LoadFileDataUtil {
    /**
     * 读取文件数据存为set：一行一个词
     *
     * @param oneSet 存入的set
     * @param minLen 文本最小长度
     * @param maxLen 文本最大长度
     */
    public static void loadSingleWordToSet(String filePath, Set<String> oneSet, int minLen, int maxLen) {
        String encoding = "utf-8";
        File file = new File(filePath);
        //判断文件是否存在 考虑到编码格式
        try (InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);
             BufferedReader bf = new BufferedReader(read)) {
            String lineTxt;
            int count = 0;
            while ((lineTxt = bf.readLine()) != null) {
                lineTxt = lineTxt.trim();
                lineTxt = lineTxt.toLowerCase();
                if (lineTxt.length() >= minLen && lineTxt.length() <= maxLen) {
                    oneSet.add(lineTxt);
                    count++;
                }
            }
            log.info("文件:{},包含词数量：{}", filePath, count);
        } catch (Exception e) {
            log.info("读取文件失败：{}{}", filePath, e);
        }
    }

    /**
     * 读取文件数据存为set：一行一个词
     *
     * @param oneMap    存入的map结构
     * @param separator 文本最小长度
     */
    public static void loadWordToMap(String filePath, Map<String, String> oneMap, String separator) {
        String encoding = "utf-8";
        File file = new File(filePath);
        try (InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);
             BufferedReader bf = new BufferedReader(read)) {
            int count = 0;
            String lineTxt;
            while ((lineTxt = bf.readLine()) != null) {
                lineTxt = lineTxt.trim();
                lineTxt = lineTxt.toLowerCase();

                if (lineTxt.isEmpty()) {
                    continue;
                }

                String[] elements = lineTxt.split(separator);

                if (elements.length != 2) {
                    continue;
                }

                oneMap.put(elements[0], elements[1]);
                count++;
            }
            log.info("文件:{},包含词数量：{}", filePath, count);
        } catch (Exception e) {
            log.info("读取文件失败：{}{}", filePath, e);
        }
    }
}
