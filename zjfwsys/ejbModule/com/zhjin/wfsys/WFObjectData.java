/**
 * Copyright zhangjin(zhjin@vip.163.com)
 */
package com.zhjin.wfsys;

import com.zhjin.base.EntityHasIdBase;
import com.zhjin.base.entity.OperateDataBase;
import com.zhjin.sys.entity.ViewObjectProperty;
import com.zhjin.sys.manager.ObjectEditData;
import com.zhjin.util.ArgMap;
import com.zhjin.util.EUser;
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
	public void initWFDataComponent() throws Exception {

       	WFNodeProperty nodeProperty = Utility.getDBUtility().getEntity("select * from wfnodeproperty where wfid = :wfId and nodeid = :nodeId",
       			WFNodeProperty.class,
       			new ArgMap().add("wfId", this.getWfId())
       			.add("nodeId", this.getNodeId()));

       	Utility.getObjectEditManager().initObjectEditData(this.getObjectData(), this.getObjectData().getEditData(), "", "", "Title",
       			(nodeProperty == null ? "" : nodeProperty.getReadOnlyColumns()), 
       			(nodeProperty == null ? "" : nodeProperty.getHideColumns()));
       	
       	if (nodeProperty != null && Utility.notEmptyString(nodeProperty.getApplyURL()) && !Utility.notEmptyString(this.getUrl())) {
       		this.setUrl(nodeProperty.getApplyURL());
       	}
       	if (this.getWindowWidth() == 0 || this.getWindowHeight() == 0) {
       		WorkflowDefine wfd = Utility.getDBUtility().getEntity(WorkflowDefine.class, this.getWfId());
           	if (Utility.notEmptyString(wfd.getUrl()) && !Utility.notEmptyString(this.getUrl())) {
           		this.setUrl(wfd.getUrl());
           	}
       	}
       	
	}

	@Override
	public void initData(Object obj) throws Exception {
		ViewObjectProperty vop = Utility.getDBUtility().getEntity(ViewObjectProperty.class, this.getObjectName());
		this.getObjectData().setEditData(Class.forName(vop.getObjectClassName()).newInstance());
		if (this.getObjectData().getEditData() instanceof OperateDataBase) {
			OperateDataBase _op = (OperateDataBase)this.getObjectData().getEditData();
			EUser _u = Utility.getDBUtility().getEntity("select * from euserview where id = :userId", 
					EUser.class, new ArgMap().add("userId", Utility.getSysUtil().getUser().getUserId()));
			_op.setOperEmpId(_u.getId());
			_op.setOperLoginName(_u.getLoginName());
			_op.setOperName(_u.getName());
			_op.setDepId(_u.getDepId());
		}
	}

	@Override
	public long getDataId() {
		return ((EntityHasIdBase)this.getObjectData().getEditData()).getId();
	}

}
