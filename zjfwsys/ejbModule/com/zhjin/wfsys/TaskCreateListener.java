/**
 * Copyright zhangjin(zhjin@vip.163.com)
 */
package com.zhjin.wfsys;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

import com.zhjin.util.Utility;

public class TaskCreateListener implements TaskListener {

	@Override
	public void notify(DelegateTask arg0) {
		Utility.executeMethodExpression("#{wfManager.userTaskCreate}", 
				new Class[]{String.class, String.class, String.class}, 
				new Object[]{arg0.getProcessDefinitionId(), arg0.getProcessInstanceId(), arg0.getTaskDefinitionKey()});
	}

}
