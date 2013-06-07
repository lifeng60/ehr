/**
 * Copyright zhangjin(zhjin@vip.163.com)
 */
package com.zhjin.wfsys;

import com.zhjin.base.EntityHasIdBase;
import com.zhjin.sys.entity.ViewObjectProperty;
import com.zhjin.sys.manager.ObjectEditData;
import com.zhjin.util.ArgMap;
import com.zhjin.util.Utility;

public class WFObjectData extends WFDataBase {

	private ObjectEditData objectData;

	private String objectName;

	public WFObjectData() {
		super();
		objectData = new ObjectEditData();
	}

	public ObjectEditData getObjectData() {
		return objectData;
	}

	public void setObjectData(ObjectEditData objectData) {
		this.objectData = objectData;
	}

	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	@Override
	public void dataProcess() throws Exception {

	}

	@Override
	public void loadData(long dataId) throws Exception {
		if (dataId == 0) {
			return;
		}
		ViewObjectProperty vop = Utility.getDBUtility().getEntity(ViewObjectProperty.class, this.getObjectName());
		this.getObjectData().setEditData(Utility.getDBUtility().getEntity("select * from " + this.getObjectName() + " where id = :id",
				Class.forName(vop.getObjectClassName()), new ArgMap().add("id", dataId)));
	}

	@Override
	public void saveData() throws Exception {
		this.getObjectData().setEditData(Utility.getDBUtility().update(this.getObjectData().getEditData(), true));
	}

	@Override
	public void initWFDataComponent(long wfId) throws Exception {

       	WFNodeProperty nodeProperty = Utility.getDBUtility().getEntity("select * from wfnodeproperty where wfid = :wfId and nodeid = :nodeId",
       			WFNodeProperty.class,
       			new ArgMap().add("wfId", wfId)
       			.add("nodeId", this.getNodeId()));

       	Utility.getObjectEditManager().initObjectEditData(this.getObjectData(), this.getObjectData().getEditData(), "", "", "Title",
       			nodeProperty.getReadOnlyColumns(), nodeProperty.getHideColumns());
       	
       	if (nodeProperty.getDialogHeight() > 0) {
       		this.setWindowHeight(nodeProperty.getDialogHeight());
       	}
       	if (nodeProperty.getDialogWidth() > 0) {
       		this.setWindowWidth(nodeProperty.getDialogWidth());
       	}
       	if (Utility.notEmptyString(nodeProperty.getApplyURL()) && !Utility.notEmptyString(this.getUrl())) {
       		this.setUrl(nodeProperty.getApplyURL());
       	}
       	if (this.getWindowWidth() == 0 || this.getWindowHeight() == 0) {
       		WorkflowDefine wfd = Utility.getDBUtility().getEntity(WorkflowDefine.class, wfId);
       		if (wfd.getWidth() > 0) {
       			this.setWindowHeight(wfd.getWidth());
       		}
       		if (wfd.getHeight() > 0) {
       			this.setWindowHeight(wfd.getHeight());
       		}
           	if (Utility.notEmptyString(wfd.getUrl()) && !Utility.notEmptyString(this.getUrl())) {
           		this.setUrl(wfd.getUrl());
           	}
       	}
       	if (this.getWindowWidth() == 0 || this.getWindowHeight() == 0) {
       		ViewObjectProperty _vop = Utility.getDBUtility().getEntity(ViewObjectProperty.class, this.getObjectName());
       		if (_vop.getEditWidth() > 0) {
       			this.setWindowWidth(_vop.getEditWidth());
       		}
       		if (_vop.getEditHeight() > 0) {
       			this.setWindowHeight(_vop.getEditHeight());
       		}
       	}
       	
	}

	@Override
	public void initData(Object obj) throws Exception {
		ViewObjectProperty vop = Utility.getDBUtility().getEntity(ViewObjectProperty.class, this.getObjectName());
		this.getObjectData().setEditData(Class.forName(vop.getObjectClassName()).newInstance());
	}

	@Override
	public long getDataId() {
		return ((EntityHasIdBase)this.getObjectData().getEditData()).getId();
	}

}
