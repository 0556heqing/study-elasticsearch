package com.heqing.search.segment.extend.hypernym;

import com.heqing.search.analyzer.Separators;
import com.heqing.search.segment.extend.BaseChineseTag;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author suke
 * @version 1.0
 * @className Hypernym
 * @description
 * @date 2020/7/20 11:28
 **/
@Slf4j
public class Hypernym extends BaseChineseTag {

    private Hypernym() {
    }

    public Hypernym(String dataDir, int nPath) {
        super(dataDir, nPath);
    }

    @Override
    protected void loadData(String dataDir, Set<String> words, Map<String, Set<String>> word2tags) {
        dataDir += "upwords/";
        String dataFile = dataDir + "upwords.data";
        String encoding = "utf-8";
        File file = new File(dataFile);

        try (FileInputStream fileInputStream = new FileInputStream(file);
             InputStreamReader reader = new InputStreamReader(fileInputStream, encoding);
             BufferedReader bf = new BufferedReader(reader)) {


            String line;
            while ((line = bf.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }
                String[] elements = line.split(Separators.COLON);
                String downWord = elements[0].trim();
                String upWordsStr = elements[1].trim();
                words.add(downWord);

                Set<String> upWordsSet = word2tags.getOrDefault(elements[0].trim(), new HashSet<>());

                String[] upWords = upWordsStr.split(Separators.COMMA);
                for (String word : upWords) {
                    word = word.trim();
                    upWordsSet.add(word);
                }
                word2tags.put(downWord, upWordsSet);
            }
            log.info("上下位词标签数量：{}", words.size());

        } catch (Exception e) {
            log.error("读取文件失败：{},{}", dataFile, e);
        }
    }


    public static void main(String[] args) {
        String dataDir = "data/segment-recall/extend/";
        int nPath = 3;
        BaseChineseTag upWordsTag = new Hypernym(dataDir, nPath);
        String com = upWordsTag.getTags("薄被子内衣");
        System.out.println(com);
    }
}
