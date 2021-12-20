package com.heqing.search.segment.common;

import java.io.Serializable;

/**
 * @author suke
 * @version 1.0
 * @description
 * @date 2020/8/12 11:17
 **/
public final class Token implements Serializable {
	private static final long serialVersionUID = 6625407732567044359L;
	public String term;
	/**
	 * 预留：词的长度
	 */
	public int len = -1;
	public short offset;
	public float weight;

	public Token() {
		super();
	}

	public Token(String term, short offset, float weight) {
		super();

		this.term = term;
		this.offset = offset;
		this.weight = weight;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public short getOffset() {
		return offset;
	}

	public void setOffset(short offset) {
		this.offset = offset;
	}

	public float getWeight() {
		return weight;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

	public int getLen() {
		return len;
	}

	public void setLen(int len) {
		this.len = len;
	}

	@Override
	public String toString() {
		return "[" + term + ", " + offset + ", " + (offset + len) + "]";
	}
}
