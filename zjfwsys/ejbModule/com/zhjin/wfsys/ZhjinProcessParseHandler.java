/**
 * Copyright zhangjin(zhjin@vip.163.com)
 */
package com.zhjin.wfsys;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.bpmn.helper.ClassDelegate;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.handler.AbstractBpmnParseHandler;

public class ZhjinProcessParseHandler extends AbstractBpmnParseHandler<Process> {

	@Override
	protected Class<? extends BaseElement> getHandledType() {
		return Process.class;
	}
	
	@Override
	protected void executeParse(BpmnParse arg0, Process arg1) {
		arg0.getCurrentProcessDefinition().addExecutionListener(ExecutionListener.EVENTNAME_END, 
				new ClassDelegate(EndEventListener.class, null));
	}

}
