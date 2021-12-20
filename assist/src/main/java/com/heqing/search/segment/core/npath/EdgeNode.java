/**
 * EdgeNode
 * 
 * @author liuxiangqian
 * @date 2019/5/6
 */
package com.heqing.search.segment.core.npath;


public class EdgeNode {
	/**
	 * 边的入节点
	 */
	public int endPoint = -1;
	/**
	 * 兄弟边
	 */
	public EdgeNode next = null;
	/**
	 * 边的权重
	 */
	public int weight = 1;
	/**
	 * 边对应的词
	 */
	public String word = "";
}
