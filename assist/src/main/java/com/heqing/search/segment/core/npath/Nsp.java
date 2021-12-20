package com.heqing.search.segment.core.npath;

import com.heqing.search.analyzer.Separators;
import com.heqing.search.segment.probability.NgramModle;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;

/**
 * @author suke
 * @version 1.0
 * @description
 * @date 2020/8/12 11:17
 **/
@Slf4j
public class Nsp {
    /**
     * 最优n路径
     */
    private int nPath = 0;
    private Set<String> dict = new HashSet<>();
    /**
     * 最大中文字长
     */
    private int maxWordLen = 20;

    /**
     * 排除无效路径
     */
    private Set<String> wordStartSet = new HashSet<>();

    public Nsp(int nPath) {
        this.nPath = nPath;
    }

    public class Path {
        int nPath = 0;
        /**
         * 权重——>parent
         */
        public Map<Integer, Parent> parent = new HashMap<>();

        Path(int nPath) {
            this.nPath = nPath;
        }

        public void sortAndTrimToNpath() {
            if (parent.size() > Nsp.this.nPath) {
                //遍历mParent,只保留mN条路径的链表
                parent = insertSort();

            }
        }

        private Map<Integer, Parent> insertSort() {
            List<Integer> tmpList = new ArrayList<>();
            for (Map.Entry<Integer, Parent> entry : parent.entrySet()) {
                int key = entry.getKey();

                //找到插入位置
                int i = tmpList.size() - 1;
                for (; i >= 0; i--) {
                    if (tmpList.get(i) < key) {
                        //key应该插入到i+1位置index
                        break;
                    }
                }

                tmpList.add(i + 1, key);
                if (tmpList.size() > nPath) {
                    //如果超过mN，删除最后一个
                    tmpList.remove(tmpList.size() - 1);
                }
            }

            Map<Integer, Parent> tmpMap = new HashMap<>(10);
            for (int i = 0; i < tmpList.size(); i++) {
                int key = tmpList.get(i);
                Parent value = parent.get(key);

                tmpMap.put(key, value);
            }

            return tmpMap;
        }
    }

    static class Parent {
        /**
         * 到该节点的路径权重
         */
        int pathWeight = 100;
        /**
         * 同一个权重来源
         */
        Parent next = null;
        /**
         * 中间用"\t"隔开
         */
        String wordPath = "";
    }

    static class Term {
        /**
         * 句子中的位置
         */
        public int pos = -1;
        /**
         * 词汇的长度
         */
        public int len = -1;
        /**
         * 词汇
         */
        public String word = "";
    }

    /**
     * 加载词典
     *
     * @param path
     */
    public void init(String path) {
        //加载N最优路径词汇 npathword.data
        String datafile = path + "npathword.data";
        String encoding = "utf-8";
        File file = new File(datafile);

        //考虑到编码格式
        try (InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);
             BufferedReader bufferedReader = new BufferedReader(read)) {
            String lineTxt;
            bufferedReader.readLine();
            while ((lineTxt = bufferedReader.readLine()) != null) {
                lineTxt = lineTxt.trim();
                if (lineTxt.length() > 1) {
                    dict.add(lineTxt);
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            log.error("cannot find file: " + datafile, e);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String word : dict) {
            String start = word.substring(0, 1);
            wordStartSet.add(start);
        }
    }


    /**
     *
     * 加载词典
     * @param words
     */
    public void init(List<String> words) {
        //加载N最优路径词汇 npathword.data
        for (String word : words) {
            dict.add(word);
        }

        for (String word : dict) {
            String start = word.substring(0, 1);
            wordStartSet.add(start);
        }
    }

    List<Term> splitAllWords(String sentence) {
        List<Term> words = new ArrayList<>();
        for (int i = 0; i < sentence.length(); i++) {
            int j = sentence.length() - i;
            if (j > maxWordLen) {
                j = maxWordLen;
            }

            for (int start = 2; j >= start; j--) {
                String subStr = sentence.substring(i, i + j);
                if (dict.contains(subStr)) {
                    Term aterm = new Term();
                    aterm.pos = i;
                    aterm.len = subStr.length();
                    aterm.word = subStr;

                    words.add(aterm);
                }
            }
        }

        return words;
    }

    /**
     * 创建图
     *
     * @param sentence
     * @return
     */
    public List<VertexNode> createGraph(String sentence) {
        sentence = sentence.replaceAll(Separators.TAB, Separators.SPACE);

        //步骤一： 构建图
        //初始化图结构
        List<VertexNode> graph = new ArrayList<>();
        for (int i = 0; i < sentence.length(); i++) {
            VertexNode vertexNode = new VertexNode();

            //第一条出边
            EdgeNode e = new EdgeNode();
            e.next = null;
            e.endPoint = i + 1;
            e.weight = 1;
            e.word = sentence.substring(i, i + 1);
            vertexNode.mEdgeNode = e;

            vertexNode.mPointNum = i;

            graph.add(vertexNode);
        }

        //末节点
        VertexNode vertexNode = new VertexNode();
        vertexNode.mEdgeNode = null;
        vertexNode.mPointNum = sentence.length();
        graph.add(vertexNode);

        //添加长距离边（跨两个字符或以上的边）
        List<Term> words = splitAllWords(sentence);

        for (Term term : words) {
            //有一条节点pos到节点pos+len的边
            int len = term.len;
            int pos = term.pos;
            VertexNode vnode = graph.get(pos);

            //建一条边
            EdgeNode enode = new EdgeNode();
            enode.next = null;
            enode.endPoint = pos + len;
            enode.word = term.word;

            EdgeNode pNode = vnode.mEdgeNode;
            while (pNode.next != null) {
                pNode = pNode.next;
            }

            pNode.next = enode;
        }

        return graph;
    }

    /**
     * 创建图
     *
     * @param sentence
     * @return
     */
    public List<VertexNode> createGraph(String sentence, NgramModle nGram) {
        sentence = sentence.replaceAll(Separators.TAB, Separators.SPACE);

        //步骤一： 构建图
        //初始化图结构
        List<VertexNode> graph = new ArrayList<>();
        for (int i = 0; i < sentence.length(); i++) {
            VertexNode vertexNode = new VertexNode();

            //第一条出边
            EdgeNode e = new EdgeNode();
            e.next = null;
            e.endPoint = i + 1;
            e.word = sentence.substring(i, i + 1);
            e.weight = nGram.oneWordLogProbilityNormal(e.word);

            vertexNode.mEdgeNode = e;

            vertexNode.mPointNum = i;

            graph.add(vertexNode);
        }

        //末节点
        VertexNode vertexNode = new VertexNode();
        vertexNode.mEdgeNode = null;
        vertexNode.mPointNum = sentence.length();
        graph.add(vertexNode);

        //添加长距离边（跨两个字符或以上的边）
        List<Term> words = splitAllWords(sentence);
        for (Term term : words) {
            //有一条节点pos到节点pos+len的边
            int len = term.len;
            int pos = term.pos;
            VertexNode vnode = graph.get(pos);

            //建一条边
            EdgeNode enode = new EdgeNode();
            enode.next = null;
            enode.endPoint = pos + len;
            enode.word = term.word;
            enode.weight = nGram.oneWordLogProbilityNormal(enode.word);

            EdgeNode pNode = vnode.mEdgeNode;
            while (pNode.next != null) {
                pNode = pNode.next;
            }

            pNode.next = enode;
        }

        return graph;
    }

    public List<Path> computeTopnBestPathForEveryVertex(List<VertexNode> graph) {
        //初始化mPath
        List<Path> paths = new ArrayList<>();
        for (int i = 0; i < graph.size(); i++) {
            paths.add(new Path(nPath));
        }

        for (VertexNode vnode : graph) {
            computeTopnPathOfChildrenVertexe(vnode, paths);
        }

        return paths;
    }

    /**
     * 计算概率率最大的n个前驱
     *
     * @param graph
     * @return
     */
    public List<Path> computeTopnBestPathForEveryVertex(List<VertexNode> graph, NgramModle nGramModle) {
        //初始化mPath
        List<Path> paths = new ArrayList<>();
        for (int i = 0; i < graph.size(); i++) {
            paths.add(new Path(nPath));
        }

        for (VertexNode vnode : graph) {
            computeTopnPathOfChildrenVertexe(vnode, paths, nGramModle);
        }

        return paths;
    }

    /**
     * 计算从开始到当前节点的孩子节点的路径
     *
     * @param vnode
     * @param paths
     */
    private void computeTopnPathOfChildrenVertexe(VertexNode vnode, List<Path> paths) {
        EdgeNode enode = vnode.mEdgeNode;
        while (enode != null) {
            computeTopnPathForOneChildrenVertex(vnode, enode, paths);
            enode = enode.next;
        }
    }


    /**
     * 计算从开始到当前节点的孩子节点的路径
     *
     * @param vnode
     * @param paths
     */
    private void computeTopnPathOfChildrenVertexe(VertexNode vnode, List<Path> paths, NgramModle nGramModle) {
        EdgeNode enode = vnode.mEdgeNode;
        while (enode != null) {
            computeTopnPathForOneChildrenVertex(vnode, enode, paths, nGramModle);
            enode = enode.next;
        }
    }

    private void computeTopnPathForOneChildrenVertex(VertexNode vnode, EdgeNode enode, List<Path> paths) {
        int endVer = enode.endPoint;
        int weight = enode.weight;
        String word = enode.word;

        //节点endVer的父路径收集
        Path childPath = paths.get(endVer);
        Map<Integer, Parent> parentMap = childPath.parent;

        //步骤一：首节点处理
        if (vnode.mPointNum == 0) {
            Parent pp = new Parent();
            pp.next = null;
            pp.pathWeight = 0;
            //到该节点的路径
            pp.wordPath = word;

            //当前路径到此节点的权重
            int curWeight = pp.pathWeight + weight;
            if (parentMap.containsKey(curWeight)) {
                Parent parent = parentMap.get(curWeight);
                pp.next = parent;
                parent = pp;
                parentMap.put(curWeight, parent);
            } else {
                parentMap.put(curWeight, pp);
            }

            return;
        }

        //步骤二：非首节点
        //当前点的路径
        Path curPath = paths.get(vnode.mPointNum);
        //保留N最优路径
        curPath.sortAndTrimToNpath();

        Map<Integer, Parent> integerParentMap = curPath.parent;
        for (Map.Entry<Integer, Parent> entry : integerParentMap.entrySet()) {
            int preWeight = entry.getKey();
            //当前路径到此节点的权重  前面节点的权重+边的权重
            int curWeight = preWeight + weight;

            Parent prePar = entry.getValue();
            while (prePar != null) {
                String curWordPath = prePar.wordPath;

                Parent pp = new Parent();
                pp.next = null;
                pp.pathWeight = preWeight;
                pp.wordPath = curWordPath + Separators.TAB + word;

                if (parentMap.containsKey(curWeight)) {
                    Parent parent = parentMap.get(curWeight);
                    pp.next = parent;
                    parent = pp;
                    parentMap.put(curWeight, parent);
                } else {
                    parentMap.put(curWeight, pp);
                }

                prePar = prePar.next;
            }
        }
    }


    private void computeTopnPathForOneChildrenVertex(VertexNode vnode, EdgeNode enode, List<Path> paths, NgramModle nGramModle) {
        int endVer = enode.endPoint;
        String word = enode.word;
        int weight = Integer.MAX_VALUE;

        //节点endVer的父路径收集
        Path childPath = paths.get(endVer);
        Map<Integer, Parent> parentMap = childPath.parent;

        //步骤一：首节点处理
        if (vnode.mPointNum == 0) {
            Parent pp = new Parent();
            pp.next = null;
            pp.pathWeight = 0;
            //到该节点的路径
            pp.wordPath = word;

            weight = nGramModle.oneWordLogProbilityNormal(word);
            //当前路径到此节点的权重
            int curWeight = pp.pathWeight + weight;
            if (parentMap.containsKey(curWeight)) {
                Parent parent = parentMap.get(curWeight);
                pp.next = parent;
                parent = pp;
                parentMap.put(curWeight, parent);
            } else {
                parentMap.put(curWeight, pp);
            }

            return;
        }

        //步骤二：非首节点
        //当前点的路径
        Path curPath = paths.get(vnode.mPointNum);
        //保留N最优路径
        curPath.sortAndTrimToNpath();

        Map<Integer, Parent> integerParentMap = curPath.parent;
        for (Map.Entry<Integer, Parent> entry : integerParentMap.entrySet()) {
            int preWeight = entry.getKey();

            Parent prePar = entry.getValue();
            while (prePar != null) {
                String curWordPath = prePar.wordPath;

                Parent pp = new Parent();
                pp.next = null;
                pp.pathWeight = preWeight;
                pp.wordPath = curWordPath + Separators.TAB + word;

                int index = curWordPath.lastIndexOf(Separators.TAB);
                //对于第二个词，前缀没有tab
                index = index >0?(index+1):0;

                String preWord = curWordPath.substring(index);

                weight = nGramModle.twoWordLogProbilityNormal(preWord, word);
                if(weight < 0){
                    log.error("log概率不能小于0");
                }

                int curWeight = preWeight + weight;
                if (parentMap.containsKey(curWeight)) {
                    Parent parent = parentMap.get(curWeight);
                    pp.next = parent;
                    parent = pp;
                    parentMap.put(curWeight, parent);
                } else {
                    parentMap.put(curWeight, pp);
                }

                prePar = prePar.next;
            }
        }
    }

    public List<String> getTopnBestPath(List<Path> path) {
        List<String> nPath = new ArrayList<>();
        int end = path.size() - 1;
        Path endPath = path.get(end);
        endPath.sortAndTrimToNpath();
        Map<Integer, Parent> parent = endPath.parent;
        for (Map.Entry<Integer, Parent> entry : parent.entrySet()) {
            Parent pp = entry.getValue();
            while (pp != null) {
                nPath.add(pp.wordPath);
                pp = pp.next;
            }
        }

        return nPath;
    }

    public int getWeight(String key) {
        key = key.trim();
        if (dict.contains(key)) {
            return 1;
        }

        return -1;
    }

    public static void main(String[] cmdline) {
        Nsp nsp = new Nsp(10);
        nsp.init("./data/trainOut/");

        long start = System.currentTimeMillis();
        int num = 100;
        for (int i = 0; i < num; i++) {
            //步骤一： 构建查出所有的词汇，computeNBestPathForEveryVectex并构建图
            //			String sentence = "他说的确实在理";
            String sentence = "建设中国特色社会主义法制体系";
            List<VertexNode> graph = nsp.createGraph(sentence);

            //步骤二： 计算每个节点的N个最优前趋
            List<Path> verPath = nsp.computeTopnBestPathForEveryVertex(graph);

            //步骤三： 回退计算N条最有路径
            List<String> nPathVec = nsp.getTopnBestPath(verPath);

            for (String ret : nPathVec) {
                String[] arr = StringUtils.split(ret, Separators.TAB);
                log.info(arr.length + ":\t" + ret);
            }
        }

        long end = System.currentTimeMillis();

        long cha = end - start;
        log.info(num + "次需要时间： " + cha + " 毫秒");
    }
}
