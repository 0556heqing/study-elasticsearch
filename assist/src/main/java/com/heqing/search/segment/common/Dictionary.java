package com.heqing.search.segment.common;

import com.heqing.search.analyzer.Separators;
import com.heqing.search.analyzer.StringUtil;
import com.heqing.search.segment.core.npath.Nsp;
import com.heqing.search.segment.core.npath.VertexNode;

import java.util.*;

/**
 * @author suke
 * @version 1.0
 * @className TagDict
 * @description
 * @date 2020/7/10 19:46
 **/
public class Dictionary {
    private Nsp nsp;

    public Dictionary(int nPath, List<String> words){
        nsp=new Nsp(nPath);

        List<String> normalWords=new ArrayList<>();
        for (String content:words){
            content= StringUtil.normalServerString(content);
            if (content.length()>=2){
                normalWords.add(content);
            }
        }

        nsp.init(normalWords);
    }

    public List<String> getTagsByContent(String content){
        content= StringUtil.normalServerString(content);

        Map<String,Integer> tag2valueMap=new HashMap<>(20);
        String[] sentences=content.split(Separators.SPACE);

        for (String sentence:sentences){
            if (sentence.length()>100){
                continue;
            }

            //步骤一： 构建查出所有的词汇，computeNBestPathForEveryVectex并构建图
            List<VertexNode> graph=nsp.createGraph(sentence);

            //步骤二： 计算每个节点的N个最优前趋
            List<Nsp.Path> verPath=nsp.computeTopnBestPathForEveryVertex(graph);

            //步骤三： 回退计算N条最有路径
            List<String> nPathVec=nsp.getTopnBestPath(verPath);

            for (String ret:nPathVec){
                String[] arr=ret.split(Separators.TAB);
                collectKeys(arr,tag2valueMap);
            }
        }
        List<Map.Entry<String,Integer>> tag2valueList=new ArrayList<>(tag2valueMap.entrySet());
        tag2valueList.sort(Comparator.comparing(Map.Entry<String,Integer>::getValue).reversed());

        List<String> tags=new ArrayList<>();
        for (Map.Entry<String,Integer> entry:tag2valueList){
            String key=entry.getKey();
            tags.add(key);
        }

        tags=new ArrayList<>(new HashSet<>(tags));
        return tags;
    }

    private void collectKeys(String[] arr,Map<String,Integer> tags){
        for (String tag:arr){
            String key=tag.toLowerCase().trim();
            if (key!=null&&!key.isEmpty()&&key.length()>0&&nsp.getWeight(key)>=0){
                int count=1+tags.getOrDefault(key,0);
                tags.put(key,count);
            }
        }
    }
}
