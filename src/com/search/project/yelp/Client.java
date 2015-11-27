package com.search.project.yelp;

import java.util.Arrays;

public class Client {

	public static void main(String[] args) {
		String[] array = new String[] { "Me may mo", "Milind Gokhale" };
		for (String text : array) {
			System.out.println(text);
		}
		System.out.println(Arrays.toString(array));
	}

}
