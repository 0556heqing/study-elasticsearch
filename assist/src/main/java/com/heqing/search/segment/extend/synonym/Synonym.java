package com.heqing.search.segment.extend.synonym;

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
 * @className Synonym
 * @description
 * @date 2020/7/20 11:22
 **/
@Slf4j
public class Synonym extends BaseChineseTag {
    private Synonym() {
    }

    public Synonym(String dataDir, int nPath) {
        super(dataDir, nPath);
    }

    @Override
    protected void loadData(String dataDir, Set<String> words, Map<String, Set<String>> word2tags) {
        dataDir += "synonym/";
        String dataFile = dataDir + "synonym.data";
        String encoding = "utf-8";
        File file = new File(dataFile);

        try (FileInputStream fileInputStream = new FileInputStream(file);
             InputStreamReader reader = new InputStreamReader(fileInputStream, encoding);
             BufferedReader bf = new BufferedReader(reader)) {
            String line;
            while ((line = bf.readLine()) != null) {
                line = line.trim();

                //对基础数据规范化，去除空格
                String[] elements = line.split(Separators.COMMA);
                Set<String> synonyms = new HashSet<>();
                for (int i = 0; i < elements.length; i++) {
                    String word = elements[i].trim();
                    if (word.length() > 1) {
                        synonyms.add(word);
                    }
                }

                //只处理有同义关系的词组
                if (synonyms.size() <= 1) {
                    continue;
                }

                //收集同义关系
                synonyms.forEach(word -> {
                    words.add(word);

                    Set<String> wordSynonyms = new HashSet<>();
                    wordSynonyms.addAll(synonyms);
                    wordSynonyms.remove(word);
                    word2tags.put(word, wordSynonyms);
                });
            }
            log.info("同义词标签数量：{}", words.size());

        } catch (Exception e) {
            log.error("读取文件失败：{},{}", dataFile, e);
        }
    }

    public static void main(String[] args) {
        String dataDir = "data/segment-recall/extend";
        int nPath = 3;
        Synonym synonymTag = new Synonym(dataDir, nPath);
        String synTags = synonymTag.getTags("止咳糖浆婴幼儿奶粉");
        System.out.println(synTags);
    }
}
