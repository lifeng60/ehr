/**
 * Copyright zhangjin(zhjin@vip.163.com)
 * Licensed under GNU GENERAL PUBLIC LICENSE
 */
package com.zhjin.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;

import com.zhjin.util.ArgMap;
import com.zhjin.util.Utility;

public class TestJava {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String s = "wid=menuconv?dcid=729e4958-2691-469f-9ef9-57e167f8ea1d";
		String s1 = s.replace('?', '&');
		System.out.println(s1);
	}
	
	private static void a(String s, String argname, TreeMap<Integer, String> sear) {
		
	}

}
