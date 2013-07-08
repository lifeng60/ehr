/**
 * Copyright zhangjin(zhjin@vip.163.com)
 */
package com.zhjin.util;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

/**
 * Session Bean implementation class SystemStartupBean
 */
@Singleton
@Startup
@LocalBean
public class SystemStartupBean extends BeanBase {
	
	@Inject
	private ApplicationPara appPara;
	
	
    /**
     * Default constructor. 
     */
    public SystemStartupBean() {
    }
    
    @PostConstruct
    public void initArg() throws Exception {
    	
    	List<SystemParameter> _list = dbUtility.getDataList("select * from systemparameter", SystemParameter.class, null);
    	
    	for (SystemParameter para : _list) {
    		appPara.getSysPara().put(para.getParaName(), para.getParaValue());
    	}

    	
    }
    
}
