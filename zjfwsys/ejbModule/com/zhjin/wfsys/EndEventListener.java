/**
 * Copyright zhangjin(zhjin@vip.163.com)
 */
package com.zhjin.wfsys;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

import com.zhjin.util.Utility;

public class EndEventListener implements ExecutionListener {

	@Override
	public void notify(DelegateExecution arg0) throws Exception {
		Utility.executeMethodExpression("#{wfManager.processInstanceEndProcess}", 
				new Class[]{String.class, String.class}, 
				new Object[]{arg0.getProcessDefinitionId(), arg0.getProcessInstanceId()});
	}

}
