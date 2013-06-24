/**
 * Copyright zhangjin(zhjin@vip.163.com)
 */
package com.zhjin.wfsys;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import com.zhjin.base.entity.SysUploadFile;

/**
 * 流程数据基类
 * @author zhjin
 */
public abstract class WFDataBase {
	
	public static final String WF_NEW = "NEW";
	public static final String WF_APPLY = "APPLY";
	public static final String WF_VIEW = "VIEW";
	
	private long wfId;

	private String wfInstanceId;

	private String nodeId;

	private String url;

	private long attachmentId;
	
	private int attachFileNum;
	
	private List<SysUploadFile> attachFileList = new ArrayList<SysUploadFile>();
	
	private int applyHistoryNum;
	
	private int windowHeight;
	
	private int windowWidth;
	
	private boolean readOnly;
	
	private String requestType;
	
	private long urgentLevel = 1;
	
	private String requireTitle;
	
	private String wfRemark;
	private String applyRemark;
	
	private String rollbackNodeId;
	
	private List<SelectItem> rollbackNodeList;
	
	private String applyResult;

	public String nextTransaction(String nodeName) {return null;}

	public String toHtml() { return "";}

	public void validData() throws Exception {}

	public abstract void initWFDataComponent() throws Exception;

	public abstract void initData(Object obj) throws Exception;

	public abstract void loadData(long dataId) throws Exception;

	public abstract long getDataId();

	public abstract void saveData() throws Exception;

	public abstract void dataProcess() throws Exception;

	public String getWfInstanceId() {
		return wfInstanceId;
	}

	public void setWfInstanceId(String wfInstanceId) {
		this.wfInstanceId = wfInstanceId;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public long getAttachmentId() {
		return attachmentId;
	}

	public void setAttachmentId(long attachmentId) {
		this.attachmentId = attachmentId;
	}

	public int getWindowHeight() {
		return windowHeight;
	}

	public void setWindowHeight(int windowHeight) {
		this.windowHeight = windowHeight;
	}

	public int getWindowWidth() {
		return windowWidth;
	}

	public void setWindowWidth(int windowWidth) {
		this.windowWidth = windowWidth;
	}

	public int getAttachFileNum() {
		return this.attachFileList.size();
	}

	public void setAttachFileNum(int attachFileNum) {
		this.attachFileNum = attachFileNum;
	}

	public int getApplyHistoryNum() {
		return applyHistoryNum;
	}

	public void setApplyHistoryNum(int applyHistoryNum) {
		this.applyHistoryNum = applyHistoryNum;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public List<SysUploadFile> getAttachFileList() {
		return attachFileList;
	}

	public void setAttachFileList(List<SysUploadFile> attachFileList) {
		this.attachFileList = attachFileList;
	}

	public long getWfId() {
		return wfId;
	}

	public void setWfId(long wfId) {
		this.wfId = wfId;
	}

	public long getUrgentLevel() {
		return urgentLevel;
	}

	public void setUrgentLevel(long urgentLevel) {
		this.urgentLevel = urgentLevel;
	}

	public String getRequireTitle() {
		return requireTitle;
	}

	public void setRequireTitle(String requireTitle) {
		this.requireTitle = requireTitle;
	}

	public String getWfRemark() {
		return wfRemark;
	}

	public void setWfRemark(String wfRemark) {
		this.wfRemark = wfRemark;
	}

	public String getRollbackNodeId() {
		return rollbackNodeId;
	}

	public void setRollbackNodeId(String rollbackNodeId) {
		this.rollbackNodeId = rollbackNodeId;
	}

	public List<SelectItem> getRollbackNodeList() {
		return rollbackNodeList;
	}

	public void setRollbackNodeList(List<SelectItem> rollbackNodeList) {
		this.rollbackNodeList = rollbackNodeList;
	}

	public String getApplyResult() {
		return applyResult;
	}

	public void setApplyResult(String applyResult) {
		this.applyResult = applyResult;
	}

	public String getApplyRemark() {
		return applyRemark;
	}

	public void setApplyRemark(String applyRemark) {
		this.applyRemark = applyRemark;
	}

}
