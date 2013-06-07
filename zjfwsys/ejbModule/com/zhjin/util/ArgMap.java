/**
 * Copyright zhangjin(zhjin@vip.163.com)
 */
package com.zhjin.util;

import java.util.HashMap;

public class ArgMap extends HashMap<String, Object> {
	
	public ArgMap add(String s, Object o) {
		this.put(s, o);
		return this;
	}
}
