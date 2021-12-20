/**
 * TermVec
 * 
 * @author liuxiangqian
 * @date 2019/5/6
 */
package com.heqing.search.segment.common;

import java.util.HashSet;

public class TermVec {
	/**
	 * 在句子中起始位置
	 */
	public HashSet<Integer> startPosSet = new HashSet<>();
	/**
	 * 预留：词的长度
	 */
	public int len = -1;
	/**
	 * 词汇
	 */
	public String word = "";

	public TermVec(String word, int len) {
		this.word = word;
		this.len = len;
	}

	public int getLen() {
		return len;
	}

	public void setLen(int len) {
		this.len = len;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}
}
