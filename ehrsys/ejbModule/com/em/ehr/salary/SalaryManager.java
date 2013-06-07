/**
 * Copyright zhangjin(zhjin@vip.163.com)
 */
package com.em.ehr.salary;

import com.zhjin.util.BeanBase;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Named;

/**
 * Session Bean implementation class SalaryManager
 */
@Named
@Stateless
@LocalBean
public class SalaryManager extends BeanBase {

    /**
     * Default constructor. 
     */
    public SalaryManager() {

    }
    
    public void salaryStatusChange(Object obj) throws Exception {
    	
    }

}
