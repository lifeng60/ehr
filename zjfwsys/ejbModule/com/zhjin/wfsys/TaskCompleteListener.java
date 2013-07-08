/**
 * Copyright zhangjin(zhjin@vip.163.com)
 */
package com.zhjin.wfsys;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

public class TaskCompleteListener implements TaskListener {
	
	@Override
	public void notify(DelegateTask arg0) {
		System.out.println("user task: " + arg0.getName());
		System.out.println(arg0.getProcessInstanceId());
	}

}
