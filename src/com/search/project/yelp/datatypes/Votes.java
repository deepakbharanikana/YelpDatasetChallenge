package com.search.project.yelp.datatypes;

public class Votes {
	public int funny;
	public int useful;
	public int cool;

	public int getFunny() {
		return funny;
	}

	public void setFunny(int funny) {
		this.funny = funny;
	}

	public int getUseful() {
		return useful;
	}

	public void setUseful(int useful) {
		this.useful = useful;
	}

	public int getCool() {
		return cool;
	}

	public void setCool(int cool) {
		this.cool = cool;
	}

	@Override
	public String toString() {
		return "Votes [funny=" + funny + ", useful=" + useful + ", cool="
				+ cool + "]";
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
