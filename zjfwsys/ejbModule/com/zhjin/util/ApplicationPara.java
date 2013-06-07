/**
 * Copyright zhangjin(zhjin@vip.163.com)
 */
package com.zhjin.util;

import java.util.HashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@Named
@ApplicationScoped
public class ApplicationPara {
	
	private HashMap<String, String> sysPara = new HashMap<String, String>();

	public HashMap<String, String> getSysPara() {
		return sysPara;
	}

	public void setSysPara(HashMap<String, String> sysPara) {
		this.sysPara = sysPara;
	}
	
}
