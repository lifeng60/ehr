/**
 * Copyright zhangjin(zhjin@vip.163.com)
 */
package com.zhjin.wfsys;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.helper.ClassDelegate;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.handler.AbstractBpmnParseHandler;
import org.activiti.engine.impl.pvm.process.ActivityImpl;

public class ZhjinUserTaskParseHandler extends AbstractBpmnParseHandler<UserTask> {

	@Override
	protected Class<? extends BaseElement> getHandledType() {
		return UserTask.class;
	}
	
	@Override
	protected void executeParse(BpmnParse bpmnParse, UserTask userTask) {
		
		ActivityImpl activity = findActivity(bpmnParse, userTask.getId());
		if (activity.getActivityBehavior() instanceof UserTaskActivityBehavior ) {
			((UserTaskActivityBehavior)activity.getActivityBehavior()).getTaskDefinition().addTaskListener(
					TaskListener.EVENTNAME_COMPLETE, new ClassDelegate(TaskCompleteListener.class, null));
			((UserTaskActivityBehavior)activity.getActivityBehavior()).getTaskDefinition().addTaskListener(
					TaskListener.EVENTNAME_CREATE, new ClassDelegate(TaskCreateListener.class, null));
		}	
		
	}

}
