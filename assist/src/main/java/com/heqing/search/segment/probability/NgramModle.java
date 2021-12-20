/**
 * NgramModle
 *
 * @author liuxiangqian
 * @date 2019/5/6
 */
package com.heqing.search.segment.probability;

import com.heqing.search.analyzer.Separators;
import com.heqing.search.segment.utils.TermSignature;
import com.heqing.search.segment.utils.UnMmap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

@Slf4j
public class NgramModle {
    private final static int THREE_GRAM = 3;
    /**
     * log概率值的最小值
     */
    private final static byte LOG_PROB_MIN = 1;
    /**
     * log概率值的最大值
     */
    private final static byte LOG_PROB_MAX = Byte.MAX_VALUE;
    /**
     * 对log概率进行整数转换的因子
     */
    private final static int NORMAL_FACTOR = 100000;

    /**
     * 概率结构
     */
    static class ProTerm {
        /**
         * 概率值
         */
        double pro = 0.0;
        /**
         * -log(mPro)
         */
        double logPro = 1.0;
    }

    /**
     * 训练总词汇数  将来计算概率的时候，如果出现为见过的词汇，可以默认平滑概率为: 1／mTotalNum
     */
    private int mTotalNum = 0;
    /**
     * 词的编号
     */
    private long[] countWords = null;
    /**
     * 词对应的出现次数
     */
    private short[] counts = null;

    private long[] probWords = null;
    /**
     * 词汇对应的概率值
     */
    private float[] probs = null;
    /**
     * 词汇对应的概率log值的相反数(-log(prob))
     */
    private float[] logProbs = null;

    private long[] suffixWords = null;
    private short[] suffixCounts = null;

    private int ngram;

    NgramModle(int ngram) {
        this.ngram = ngram;
    }

    /**
     * todo 前三个文件是经过模型训练出来的，怎么训练的？
     * @param dataDir
     */
    public void loadModleData(String dataDir) {
        // 词频
        loadWordsToCount(dataDir);
        //
        loadWordsToSuffixCount(dataDir);
        // 概率切分
        loadWordsToProbality(dataDir);
        // 两个数组内容一样，进行修改减少内存占用
        probWords = countWords;

        loadWordsTotalNumber(dataDir);
    }

    /**
     * 返回后接词汇去重数
     *
     * @param word
     * @return
     */
    private int searchWordCount(long word) {
        int index = Arrays.binarySearch(countWords, word);
        if (index < 0) {
            return index;
        }

        return counts[index];
    }

    private ProTerm searchWordPro(long word) {
        int index = Arrays.binarySearch(probWords, word);
        if (index < 0) {
            return null;
        }
        ProTerm proTerm = new ProTerm();
        proTerm.pro = probs[index];
        proTerm.logPro = logProbs[index];

        return proTerm;
    }

    /**
     * 返回后缀词数目
     *
     * @param word
     * @return
     */
    private int searchSuffixCount(long word) {
        int index = Arrays.binarySearch(suffixWords, word);
        if (index < 0) {
            return index;
        }

        return suffixCounts[index];
    }

    private void closeMmapFile(RandomAccessFile raf, FileChannel fc, MappedByteBuffer mbf) {
        try {
            if (null != raf) {
                raf.close();
            }

            if (null != fc) {
                fc.close();
            }

            if (null != mbf) {
                UnMmap.unMmap(mbf);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Pair<long[], short[]> loadLongShortPairFile(String dataPath) {
        RandomAccessFile raf = null;
        FileChannel fc = null;
        MappedByteBuffer mbf = null;
        long[] words = null;
        short[] counts = null;
        try {
            raf = new RandomAccessFile(dataPath, "rw");
            fc = raf.getChannel();
            mbf = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            int len = mbf.getInt();
            int index = 0;
            words = new long[len];
            counts = new short[len];

            while (mbf.hasRemaining()) {
                long wordsCode = mbf.getLong();
                short wordsCount = mbf.getShort();
                words[index] = wordsCode;

                counts[index] = wordsCount;
                index++;
            }

        } catch (IOException e) {
        } finally {
            closeMmapFile(raf, fc, mbf);
        }

        return Pair.of(words, counts);
    }


    private void loadWordsToCount(String dataDir) {
        String wordsToCountFile = dataDir + "ngrammodle/wordstocount.data";

        Pair<long[], short[]> pair = loadLongShortPairFile(wordsToCountFile);
        countWords = pair.getKey();
        counts = pair.getValue();
    }

    private void loadWordsToSuffixCount(String dataDir) {
        String wordsToSuffixCountFile = dataDir + "ngrammodle/wordstosuffixcount.data";

        Pair<long[], short[]> pair = loadLongShortPairFile(wordsToSuffixCountFile);
        suffixWords = pair.getKey();
        suffixCounts = pair.getValue();
    }

    private void loadWordsToProbality(String dataDir) {
        String wordsToProbalityFile = dataDir + "ngrammodle/wordstopro.data";
        MappedByteBuffer mbf = null;
        try (RandomAccessFile raf = new RandomAccessFile(wordsToProbalityFile, "rw");
             FileChannel fc = raf.getChannel()) {
            mbf = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            int len = mbf.getInt();
            int index = 0;
            probWords = new long[len];
            //float能够满足需要  minPro:1.6530565E-8 maxPro:1.3086449E-4
            probs = new float[len];
            //byte能够满足需要 minLogPro:0.99986917 maxLogPro:17.918055  对应关系需要确认下
            logProbs = new float[len];

            float minProb = 0.1f;
            float minLogProb = 0.1f;
            float maxProb = 0.0f;
            float maxLogProb = 0.0f;

            while (mbf.hasRemaining()) {
                long wordsCode = mbf.getLong();
                float pro = mbf.getFloat();
                float logPro = mbf.getFloat();

                probWords[index] = wordsCode;
                probs[index] = pro;
                logProbs[index] = logPro;
                index++;
            }

            log.info("minPro:{} maxPro:{}   minLogPro:{} maxLogPro:{} ", minProb, minLogProb, maxProb, maxLogProb);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        UnMmap.unMmap(mbf);
    }

    private void loadWordsTotalNumber(String dataDir) {
        String wordsToCountFile = dataDir + "ngrammodle/totalnum.data";
        String encoding = "utf-8";
        try (InputStreamReader read = new InputStreamReader(new FileInputStream(new File(wordsToCountFile)), encoding);
             BufferedReader bufferedReader = new BufferedReader(read)) {
            String lineTxt;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                mTotalNum = Integer.parseInt(lineTxt);
                break;
            }
        } catch (IOException e1) {
            log.error(e1.toString());
        }

        if (0 == mTotalNum) {
            log.error("语料总词汇数有问题, mTotalNum=" + mTotalNum);
        }
    }

    public double computeSentenceProbility(String sentence) {
        if (THREE_GRAM == ngram) {
            return computeSentenceProbility3Gram(sentence);
        }

        return computeSentenceProbility2Gram(sentence);
    }

    /**
     * 词汇用空格隔开
     *
     * @param sentence
     * @return
     */
    public double computeSentenceProbility3Gram(String sentence) {
        String[] arr = sentence.split(Separators.TAB);
        double num = 0.0;

        for (int i = 0; i < arr.length; i++) {
            num += transferlogprobility3Gram(arr, i);
        }

        return num;
    }

    public double computeSentenceProbility2Gram(String sentence) {
        String[] arr = sentence.split(Separators.TAB);
        double num = 0.0;

        for (int i = 0; i < arr.length; i++) {
            num += transferLogProbility2Gram(arr, i);
        }

        return num;
    }

    private double transferLogProbility2Gram(String[] arr, int pos) {
        if (pos < 0) {
            return Double.MAX_VALUE;
        }

        if (pos == 0) {
            //单个词的平滑概率
            return oneWordLogProbility(arr[0]);
        } else {
            //P(wi/wi-1)
            return twoWordLogProbility(arr[pos - 1], arr[pos]);
        }
    }

    private double transferlogprobility3Gram(String[] arr, int pos) {
        if (pos < 0) {
            return Double.MAX_VALUE;
        }

        if (pos == 0) {
            //单个词的平滑概率
            return oneWordLogProbility(arr[0]);
        } else if (pos == 1) {
            return twoWordLogProbility(arr[0], arr[1]);
        } else {
            return threeWordLogProbility(arr[pos - 2], arr[pos - 1], arr[pos]);
        }
    }

    /**
     * @param word
     * @return
     */
    private double oneWordLogProbility(String word) {
        long wordCode = TermSignature.signatureTerm(word);
        ProTerm proTerm = searchWordPro(wordCode);
        if (proTerm != null) {
            return proTerm.logPro;
        }

        return 0.0 - Math.log((1.0) / mTotalNum);
    }

    public int oneWordLogProbilityNormal(String word) {
        long wordCode = TermSignature.signatureTerm(word);
        ProTerm proTerm = searchWordPro(wordCode);
        if (proTerm != null) {
            return (int) (NORMAL_FACTOR * proTerm.logPro);
        }

        return (int) (NORMAL_FACTOR * (0.0 - Math.log((1.0) / mTotalNum)));
    }

    private double oneWordProbility(String word) {
        long wordCode = TermSignature.signatureTerm(word);
        ProTerm proTerm = searchWordPro(wordCode);
        if (proTerm != null) {
            return proTerm.pro;
        }

        return (1.0) / mTotalNum;
    }

    private Pair<Integer, Integer> searchWordCountAndSuffixCount(String word) {
        int wplus = 0;
        int nplus = 0;
        long leftCode = TermSignature.signatureTerm(word);
        int wc = searchWordCount(leftCode);
        int sc = searchSuffixCount(leftCode);
        if (wc >= 0 && sc >= 0) {
            wplus = wc;
            nplus = sc;
        } else {
            nplus = 1;
            wplus = 1;
        }

        Pair<Integer, Integer> pair = Pair.of(wplus, nplus);
        return pair;
    }

    private double twoWordLogProbility(String leftWord, String rightWord) {
        String key = leftWord + Separators.SPACE + rightWord;
        long keyCode = TermSignature.signatureTerm(key);

        ProTerm proTerm = searchWordPro(keyCode);
        if (proTerm != null) {
            return proTerm.logPro;
        }

        int cw2 = 0;
        //后接词的词数
        int nplus = 0;
        //总出现次数
        int wiplus = 0;

        Pair<Integer, Integer> pair = searchWordCountAndSuffixCount(leftWord);
        nplus = pair.getKey();
        wiplus = pair.getValue();

        double pwbi = oneWordProbility(rightWord);
        double pwb = (cw2 + nplus * pwbi + 0.0) / (wiplus + nplus + 0.0);
        double num = 10000.0;
        if (pwb > 0) {
            num = 0.0 - Math.log(pwb);
        }

        return num;
    }

    public int twoWordLogProbilityNormal(String leftWord, String rightWord) {
        String key = leftWord + Separators.SPACE + rightWord;
        long keyCode = TermSignature.signatureTerm(key);

        ProTerm proTerm = searchWordPro(keyCode);
        if (proTerm != null) {
            return (int) (NORMAL_FACTOR * proTerm.logPro);
        }

        int cw2 = 0;
        //后接词的词数
        int nplus = 0;
        //总出现次数
        int wiplus = 0;

        Pair<Integer, Integer> pair = searchWordCountAndSuffixCount(leftWord);
        nplus = pair.getKey();
        wiplus = pair.getValue();

        double pwbi = oneWordProbility(rightWord);
        double pwb = (cw2 + nplus * pwbi + 0.0) / (wiplus + nplus + 0.0);

        if (pwb > 1.0) {
            System.out.println("ERROR: ");
        }
        double num = 10000.0;
        if (pwb > 0) {
            num = 0.0 - Math.log(pwb);
        }

        return (int) (NORMAL_FACTOR * num);
    }

    private double twoWordProbility(String leftWord, String rightWord) {
        String key = leftWord + Separators.SPACE + rightWord;
        long keyCode = TermSignature.signatureTerm(key);

        ProTerm proTerm = searchWordPro(keyCode);
        if (proTerm != null) {
            return proTerm.pro;
        }

        int cw2 = 0;
        int nplus = 0;
        int wplus = 0;

        Pair<Integer, Integer> pair = searchWordCountAndSuffixCount(leftWord);
        nplus = pair.getKey();
        wplus = pair.getValue();

        double pwbi = oneWordProbility(rightWord);
        double pwb = (cw2 + nplus * pwbi + 0.0) / (wplus + nplus + 0.0);

        return pwb;
    }

    private double threeWordLogProbility(String leftWord, String midWord, String endWord) {
        String key = leftWord + Separators.SPACE + midWord + Separators.SPACE + endWord;
        long keyCode = TermSignature.signatureTerm(key);

        ProTerm proTerm = searchWordPro(keyCode);
        if (proTerm != null) {
            return proTerm.logPro;
        }

        int cw3 = 0;
        //后接词的词数
        int nplus = 0;
        //总出现次数
        int wijplus = 0;
        String twoWord = leftWord + Separators.SPACE + midWord;
        Pair<Integer, Integer> pair = searchWordCountAndSuffixCount(twoWord);
        nplus = pair.getKey();
        wijplus = pair.getValue();

        double pwbij = twoWordProbility(midWord, endWord);
        double pwb = (cw3 + nplus * pwbij + 0.0) / (wijplus + nplus + 0.0);
        double num = 10000.0;
        if (pwb > 0) {
            num = 0.0 - Math.log(pwb);
        }
        return num;
    }

}
