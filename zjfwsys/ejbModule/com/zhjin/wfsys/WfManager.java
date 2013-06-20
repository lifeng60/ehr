/**
 * Copyright zhangjin(zhjin@vip.163.com)
 */
package com.zhjin.wfsys;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.zip.ZipInputStream;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.imageio.ImageIO;
import javax.inject.Named;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.ReadOnlyProcessDefinition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import com.zhjin.base.entity.SysLargeText;
import com.zhjin.base.entity.SysUploadFile;
import com.zhjin.context.ConvManager;
import com.zhjin.sys.entity.WindowDefine;
import com.zhjin.sys.manager.ObjectEditManager;
import com.zhjin.sys.manager.TableManager;
import com.zhjin.sys.window.WindowData;
import com.zhjin.sys.window.WindowManager;
import com.zhjin.util.AppException;
import com.zhjin.util.ArgMap;
import com.zhjin.util.Audit;
import com.zhjin.util.BeanBase;
import com.zhjin.util.EUser;
import com.zhjin.util.FileUploadData;
import com.zhjin.util.SysUtil;
import com.zhjin.util.Utility;

/**
 * Session Bean implementation class WFManager
 */
@Named
@Stateless
@LocalBean
public class WfManager extends BeanBase {

	public static String WFDIALOG_DATA_STRING = "WFObjectData";
	public static String WFDIALOG_CONTROLDATA_STRING = "WFControlData";
	
	@EJB
	private TableManager tableManager;

	@EJB
	private ObjectEditManager objectEditManager;
	
	@EJB
	private WindowManager windowManager;

	@EJB
	private SysUtil sysUtil;

    /**
     * Default constructor.
     */
    public WfManager() {
    }

    public Object getWFData() throws Exception {
    	return null;
    }

    public String saveWFData() throws Exception {
    	return null;
    }

    public void openMyRequestWindow() throws Exception {
    	List<WorkflowDefine> wfList = dbUtility.getDataList("select * "
    	  + "from workflowdefine a, (select rownum rn, strId from workflowtypecode order by indexno, label) b "
    	  + "where a.enabled = 1 and a.visiabled = 1 "
    	  + "      and a.alwayshave = 1 and a.wfType = b.strid and a.wfType != 'SYSTEM'"
    	  + "order by b.rn, a.indexno, a.wfnamecn ", WorkflowDefine.class, null);

    	List<WorkflowDefine> actList = new ArrayList<WorkflowDefine>();
    	for (WorkflowDefine wf : wfList) {
    		try {
    			if (Utility.notEmptyString(wf.getVisiableEL())) {
    				if (!(Boolean)Utility.getELValue(wf.getVisiableEL())) {
    					continue;
    				}
    			}
    			if (Utility.notEmptyString(wf.getAlwaysHaveEL())) {
    				if (!(Boolean)Utility.getELValue(wf.getAlwaysHaveEL())) {
    					continue;
    				}
    			}
    			if (Utility.notEmptyString(wf.getEmpQueryString())) {
    				if (!dbUtility.exists("select * from euser where id = :empId and :empId in (" 
    						+ (String)Utility.getELValue(wf.getEmpQueryString() + ")"), new ArgMap().add("empId", this.getUser().getUserId()))) {
    					continue;
    				}
    			}
    		} catch (Exception ex) {
    			ex.printStackTrace();
    			continue;
    		}
    		actList.add(wf);
    	}
    	
		List<SelectItem> aList = new ArrayList<SelectItem>();
		String _wfType = null;
		List<WorkflowDefine> _wl = null;
		for (WorkflowDefine act : actList) {
			if (act.getWfType().equals(_wfType)) {
				_wl.add(act);
			} else {
				WorkflowTypeCode _wtc = dbUtility.getEntity("select * from workflowtypecode where strid = :strId", 
						WorkflowTypeCode.class, new ArgMap().add("strId", act.getWfType()));
				SelectItem _item = new SelectItem();
				_item.setLabel(_wtc.getLabel());
				_item.setValue(new ArrayList<WorkflowDefine>());
				_wl = (ArrayList<WorkflowDefine>)_item.getValue();
				_wl.add(act);
				_wfType = act.getWfType();
				aList.add(_item);
			}
		}
		
		this.getWindowData().getObjMap().put("wfList", aList);
    }
    
    public void deployWorkflow(FileUploadData fData, UploadedFile uFile) throws Exception {

    	RepositoryService rs = WFUtil.processEngine.getRepositoryService();

    	Deployment deploy = rs.createDeployment().addZipInputStream(new ZipInputStream(uFile.getInputstream())).deploy();

    	WorkflowDefine wfd = (WorkflowDefine)fData.getParentData();

    	ProcessDefinition pdf = rs.createProcessDefinitionQuery().deploymentId(deploy.getId()).singleResult();

    	InputStream imageInputStream = rs.getResourceAsStream(pdf.getDeploymentId(), pdf.getDiagramResourceName());
    	BufferedImage image = ImageIO.read(imageInputStream);
    	wfd.setImageWidth(image.getWidth());
    	wfd.setImageHeight(image.getHeight());
    	imageInputStream.close();

    	wfd.setWfKey(pdf.getKey());
    	wfd.setWfName(pdf.getName());
    	wfd.setDeployId(deploy.getId());
    	wfd.setDefineId(pdf.getId());

    	// 初始化流程节点
		try {

			ReadOnlyProcessDefinition procDef = ((RepositoryServiceImpl)rs).getDeployedProcessDefinition(pdf.getId());

			HashMap<String, Object> arg = new HashMap<String, Object>();
			StringBuffer _str = new StringBuffer();
			_str.append("(' '");

			int _nodeIndexNo = 0;

			for (PvmActivity act : procDef.getActivities()) {
				arg.clear();
				arg.put("wfId", wfd.getId());
				String _nodeName = (String)((ActivityImpl)act).getProperty("name");
				String _nodeId = ((ActivityImpl)act).getId();
				arg.put("nodeId", _nodeId);

				WFNodeProperty _node = dbUtility.getEntity("select * from wfnodeproperty where wfId = :wfId and nodeid = :nodeId",
						WFNodeProperty.class, arg);
				if (_node == null) {
					_node = new WFNodeProperty();
					_node.setWfId(wfd.getId());
					_node.setNodeName(_nodeName);
					_node.setNodeId(_nodeId);
					//_node.setNodeType(act.getType());

					if (_nodeName.equalsIgnoreCase("end")) {
						_node.setIndexNo(100000);
					} else {
						_nodeIndexNo = _nodeIndexNo + 10;
						_node.setIndexNo(_nodeIndexNo);
					}

				} else {
					_nodeIndexNo = _node.getIndexNo();
				}

				dbUtility.update(_node);
				_str.append(", '" + _nodeId + "'");
			}
			_str.append(")");
			dbUtility.executeUpdate("delete wfnodeproperty where wfid = " + Long.toString(wfd.getId()) + " and nodeid not in " + _str.toString(),
					new HashMap<String, Object>());

		} catch (Exception ex) {
			throw ex;
		} finally {
	    	if (uFile.getInputstream() != null) {
	    		uFile.getInputstream().close();
	    	}
	    }

    	dbUtility.update(wfd);

    }

    public void initWorkflowImage(Object obj) throws Exception {

    	WorkflowDefine wfd = dbUtility.getEntity(WorkflowDefine.class, ((WorkflowDefine)obj).getId());
    	this.getWindowData().getObjMap().put("workflowdefid", wfd.getDefineId());
    	Utility.openWindow("/workflow/workflowimage.jsf?wid=" + ConvManager.getCurrentConvId(), "workflowimageview", wfd.getImageWidth() - 3, wfd.getImageHeight() - 3);

    }

    public void cancelWFInstance(Object obj) throws Exception {

    	WFInstance instance = (WFInstance)obj;
    	ProcessInstance processInstance = WFUtil.processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId(instance.getWfInstanceId()).singleResult();

    	if (processInstance != null && !processInstance.isEnded()) {

    		WFUtil.processEngine.getRuntimeService().deleteProcessInstance(instance.getWfInstanceId(), "管理员终止!");

    	}

    	if (!instance.isFinish()) {
    		instance.setEndStatus("管理员终止!");
    		instance.setEndTime(new Date());
    		instance.setFinish(true);
    		dbUtility.update(instance, true);

    		WFInstanceActor actor = new WFInstanceActor();
    		actor.setActorEmpId(this.getUser().getUserId());
    		actor.setActorLoginName(this.getUser().getLoginName());
    		actor.setActorName(this.getUser().getName());
    		actor.setBeginTime(new Date());
    		actor.setEndTime(new Date());
    		actor.setRealActorEmpId(this.getUser().getUserId());
    		actor.setRealActorLoginName(this.getUser().getLoginName());
    		actor.setRealActorName(this.getUser().getName());
    		actor.setWfInstanceId(instance.getWfInstanceId());
    		actor.setNodeId("终止");
    		actor.setNodeName("终止");
    		dbUtility.save(actor, true);

    		tableManager.tablefreshbuttonclick();
    	}

    	instanceFinishToHistory(instance);

    }

    private void instanceFinishToHistory(WFInstance instance) throws Exception {

    	dbUtility.executeUpdate("insert into wfinstancehistory select * from wfinstance where wfinstanceid = :instanceId", new ArgMap().add("instanceId", instance.getWfInstanceId()));
    	dbUtility.executeUpdate("insert into wfinstanceactorhistory select * from wfinstanceactor where wfinstanceid = :instanceId", new ArgMap().add("instanceId", instance.getWfInstanceId()));
    	dbUtility.executeUpdate("delete wfinstance where wfinstanceid = :instanceId", new ArgMap().add("instanceId", instance.getWfInstanceId()));
    	dbUtility.executeUpdate("delete wfinstanceactor where wfinstanceid = :instanceId", new ArgMap().add("instanceId", instance.getWfInstanceId()));

    }

    public void initWFInstanceImage(WindowData parentWindowData, Object obj, WindowData windowData) throws Exception {

    	windowData.setInData(obj);
    	WFInstance wfInstance = (WFInstance)obj;
    	WorkflowDefine wfd = dbUtility.getEntity(WorkflowDefine.class, wfInstance.getWfId());
    	windowData.getObjMap().put("workflowdefid", wfInstance.getWfDefineId());

    	ProcessInstance processInstance = WFUtil.processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId(wfInstance.getWfInstanceId()).singleResult();

    	// 流程图
    	if (processInstance != null && !processInstance.isEnded()) {

    		ProcessDefinitionEntity def = (ProcessDefinitionEntity)((RepositoryServiceImpl)WFUtil.processEngine.getRepositoryService()).getDeployedProcessDefinition(wfd.getDefineId());



	    	TreeSet<String> _actNodeSet = new TreeSet<String>();
	    	for (String _nn : WFUtil.processEngine.getRuntimeService().getActiveActivityIds(wfInstance.getWfInstanceId())) {
	    		_actNodeSet.add(_nn);
	    	}

	    	List<ActivityImpl> actNode = new ArrayList<ActivityImpl>();

	    	List<ActivityImpl> activitiList = def.getActivities();
	    	for (ActivityImpl act : activitiList) {
	    		if (_actNodeSet.contains(act.getId())) {
	    			actNode.add(act);
	    		}
	    	}
	    	windowData.getObjMap().put("actNode", actNode);
	    	windowData.getObjMap().put("changeActor", true);

    	} else {

    		windowData.getObjMap().put("actNode", new ArrayList<ActivityImpl>());
    		windowData.getObjMap().put("changeActor", false);

    	}

    }
    
    public void newWorkFlow(WFDataBase wfData, List<Long> fileIdList, long empId) throws Exception {

    	EUser user = null;
    	if (wfData != null) {
    		wfData.validData();
    	}
    	
    	if (empId > 0) {
    		user = dbUtility.getEntity("select * from euserview where id = :id", EUser.class, new ArgMap().add("id", empId));
    	}

    	WorkflowDefine wfd = dbUtility.getEntity(WorkflowDefine.class, wfData.getWfId());

    	RuntimeService runtime = WFUtil.processEngine.getRuntimeService();

    	ProcessInstance processInstance = runtime.startProcessInstanceByKey(wfd.getWfKey());

    	WFInstance instance = new WFInstance();
    	instance.setBeginTime(new Date());
    	instance.setEnabled(true);
    	instance.setFinish(false);
    	instance.setWfId(wfd.getId());
    	instance.setWfInstanceId(processInstance.getId());
    	instance.setWfName(wfd.getWfName());
    	instance.setWfNameCN(wfd.getWfNameCN());
    	instance.setWfDefineId(wfd.getDefineId());
    	instance.setUrgentLevel(wfData.getUrgentLevel());
    	instance.setReqTitle(wfData.getRequireTitle());
    	instance.setRemark(wfData.getWfRemark());
    	if (user != null) {
    		instance.setReqEmpId(empId);
    		instance.setReqLoginName(user.getLoginName());
    		instance.setReqName(user.getName());
    		instance.setReqDepId(user.getDepId());
    	}

    	WFInstanceActor actor = new WFInstanceActor();
    	actor.setActorEmpId(empId);
    	actor.setRealActorEmpId(empId);
    	if (user != null) {
    		actor.setActorLoginName(user.getLoginName());
    		actor.setActorName(user.getName());
    		actor.setRealActorLoginName(user.getLoginName());
    		actor.setRealActorName(user.getName());
    	}
    	actor.setBeginTime(new Date());
    	actor.setEndTime(actor.getBeginTime());
    	actor.setNodeId(wfData.getNodeId());
    	actor.setNodeName("开始");
    	actor.setApplyResult("申请");
    	actor.setWfInstanceId(processInstance.getId());
    	dbUtility.save(actor, true);

		wfData.saveData();

    	instance.setDataId(wfData.getDataId());
    	
    	for (long fileid : fileIdList) {
    		WfAttachFile _f = new WfAttachFile();
    		_f.setWfInstanceId(instance.getWfInstanceId());
    		_f.setFileId(fileid);
    		dbUtility.save(_f);
    	}
    	initActivityNode(processInstance, empId, wfData.getNodeId());

    	dbUtility.save(instance);

    }

    private void initActivityNode(ProcessInstance processInstance, long preEmpId, String preNodeId) throws Exception {

    	RuntimeService runtime = WFUtil.processEngine.getRuntimeService();

    	ProcessDefinition processDefine = WFUtil.processEngine.getRepositoryService().createProcessDefinitionQuery().processDefinitionId(processInstance.getProcessDefinitionId()).singleResult();

    	WorkflowDefine wfd = dbUtility.getEntity("select * from workflowdefine where wfkey = :wfKey", WorkflowDefine.class, new ArgMap().add("wfKey", processDefine.getKey()));

    	for (String _nn : runtime.getActiveActivityIds(processInstance.getId())) {

    		WFInstanceActor actor = dbUtility.getEntity("select * from wfinstanceactor where wfInstanceId = :wfInstanceId and nodeId = :nodeId and endtime is not null ",
    				WFInstanceActor.class, new ArgMap().add("wfInstanceId", processInstance.getProcessInstanceId()).add("nodeId", _nn));

    		if (actor == null) {
    			long actorEmpId = 0;
    			EUser actorUser = null;
    			WFNodeProperty nodeProperty = Utility.getDBUtility().getEntity("select * from wfnodeproperty where wfid = :wfId and nodeid = :nodeId",
    	       			WFNodeProperty.class,
    	       			new ArgMap().add("wfId", wfd.getId())
    	       			.add("nodeId", _nn));
    			if (nodeProperty == null) {
    				throw new AppException("流程节点未定义!");
    			}
    			actor = new WFInstanceActor();
    			actor.setWfInstanceId(processInstance.getProcessInstanceId());
    			actor.setNodeId(_nn);
    			actor.setNodeName(dbUtility.getEntity("select * from wfnodeproperty where wfId = :wfId and nodeId = :nodeId",
    					WFNodeProperty.class, new ArgMap().add("wfId", wfd.getId()).add("nodeId", _nn)).getNodeName());
    			if (Utility.notEmptyString(nodeProperty.getActorEL())) {
    				actorEmpId = (Long)(Utility.getELValue(nodeProperty.getActorEL()));
    			} else if (Utility.notEmptyString(nodeProperty.getActorQueryString())) {
    				actorEmpId = ((BigDecimal)dbUtility.getData((String)Utility.getELValue(nodeProperty.getActorQueryString()), null)).longValue();
    			}
    			if (actorEmpId > 0) {
    				actorUser = dbUtility.getEntity("select * from euserview where id = :id", EUser.class, new ArgMap().add("id", actorEmpId));
    				if (actorUser == null) {
    					throw new Exception("流程节点(" + nodeProperty.getNodeName() + ")未指定审批人");
    				}
    			} else {
    				throw new Exception("流程节点(" + nodeProperty.getNodeName() + ")未指定审批人");
    			}
    			actor.setActorEmpId(actorUser.getId());
    			actor.setActorLoginName(actorUser.getLoginName());
    			actor.setActorName(actorUser.getName());
    			actor.setBeginTime(new Date());
    			actor.setPreEmpId(preEmpId);
    			actor.setPreNodeId(preNodeId);
    			
    			dbUtility.save(actor);

    		}

    	}

    }

    public void testWrokFlow(Object obj) throws Exception {
    }
    
    

    @Audit
    public void changeActor(ActionEvent event) throws Exception {

    	WFInstanceActor actor = ((WFDataDialog)this.getWindowData().getObjMap().get("WFControlData")).getChangeInstanceActor();
    	if (actor == null) {
    		throw new Exception("请选择要修改审批人的节点!");
    	}

    	if (actor.getRealActorEmpId() > 0) {
    		Utility.showAlertMessage("已完成节点不能修改审批人!");
    	} else {
    		Utility.executeJavaScript("actorDialog.show();");
    	}

    }

    @Audit
    public void refreshActor(ActionEvent event) throws Exception {

    	WFInstance wfInstance = (WFInstance)this.getWindowData().getInData();
    	List<WFInstanceActor> _actorList = dbUtility.getDataList("select * from (select * from wfinstanceactor where wfinstanceid = :instanceId " +
    			"union select * from wfinstanceactorhistory where wfinstanceid = :instanceId) a order by begintime", WFInstanceActor.class,
    			new ArgMap().add("instanceId", wfInstance.getWfInstanceId()));
    	this.getWindowData().getObjMap().put("actorList", _actorList);

    }

    public void initApplyWFDialogData(WindowData parentWindowData, Object obj, WindowData windowData) throws Exception {

    	WFInstance instance = (WFInstance)obj;

    	// 未实现流程代理人审批, 增加功能时修改
    	List<WFInstanceActor> actorList = dbUtility.getDataList("select * from wfinstanceactor where endtime is null and realactorempid = 0 and wfinstanceid = :wfinstanceid and actorempid = :actorEmpId",
    			WFInstanceActor.class, new ArgMap().add("wfinstanceid", instance.getWfInstanceId()).add("actorEmpId", this.getUser().getUserId()));

    	WFInstanceActor actor = null;
    	if (actorList.size() > 0) {
    		actor = actorList.get(0);
    	}

    	if (instance.getReqEmpId() == this.getUser().getUserId() || actor == null) {
    		initWFData(instance, "startevent1", instance.getDataId(), true);
    	} else {
    		initWFData(instance, actor.getNodeId(), instance.getDataId(), false);
    	}

    }

    public void initWFData(WFInstance instance, String nodeId, long dataId, boolean readOnly) throws Exception {

    	WorkflowDefine wfd = dbUtility.getEntity(WorkflowDefine.class, instance.getWfId());
    	WFDataBase wfData = (WFDataBase)Class.forName(wfd.getVariableObjectName()).newInstance();

    	if (wfData.getClass() == WFObjectData.class) {
    		((WFObjectData)wfData).setObjectName(wfd.getObjectName());
    	}

    	wfData.setWfInstanceId(instance.getWfInstanceId());
    	wfData.setNodeId(nodeId);
    	wfData.loadData(dataId);

    	if (wfData instanceof WFObjectData) {
    		initWFObjectDialogComponent(wfData);
    	}

    }

    private void initWFObjectDialogComponent(WFDataBase data) throws Exception {
    	WFObjectData wfData = (WFObjectData)data;
       	WFNodeProperty nodeProperty = dbUtility.getEntity("select * from wfnodeproperty where wfid = :wfId and nodeid = :nodeId",
       			WFNodeProperty.class,
       			new ArgMap().add("wfId", dbUtility.getEntity(WFInstance.class, wfData.getWfInstanceId()).getWfId())
       			.add("nodeId", wfData.getNodeId()));
       	objectEditManager.initObjectEditData(wfData.getObjectData(), wfData.getObjectData().getEditData(), "", "", "Title",
       			nodeProperty.getReadOnlyColumns(), nodeProperty.getHideColumns());
       	this.getWindowData().getObjMap().put("WFObjectData", wfData);
    }

    /**
     * 打开流程实例初始化方法
     * @param parentWindowData
     * @param obj
     * @param windowData
     * @throws Exception
     */
    public void initWorkflowDialog(WindowData parentWindowData, Object obj, WindowData windowData) throws Exception {

    	windowData.setInData(obj);
    	WFInstance instance = (WFInstance)obj;
    	WorkflowDefine wfd = dbUtility.getEntity(WorkflowDefine.class, instance.getWfId());
    	WFDataDialog controlData = new WFDataDialog();
    	controlData.setWorkflowDefine(wfd);

    	WFDataBase wfData = (WFDataBase)Class.forName(wfd.getVariableObjectName()).newInstance();

    	windowData.getObjMap().put(WFDIALOG_DATA_STRING, Class.forName(wfd.getVariableObjectName()).newInstance());

    	initApplyWFDialogData(parentWindowData, obj, windowData);
    	initWFInstanceImage(parentWindowData, obj, windowData);

    	// 已批复意见

    	// 流程附件

    	// 审核结果按钮
    	List<SelectItem> applyButtonItems = new ArrayList<SelectItem>();
    	applyButtonItems.add(Utility.getSelectItem("同意", "ACCEPT"));
    	applyButtonItems.add(Utility.getSelectItem("拒绝", "DENY"));
    	applyButtonItems.add(Utility.getSelectItem("驳回", "ROLLBACK"));
    	applyButtonItems.add(Utility.getSelectItem("审阅", "REVIEW"));
    	controlData.setApplyButtonItems(applyButtonItems);

    	// 审批明细
    	List<WFInstanceActor> _actorList = dbUtility.getDataList("select * from (select * from wfinstanceactor where wfinstanceid = :instanceId " +
    			"union select * from wfinstanceactorhistory where wfinstanceid = :instanceId) a order by begintime", WFInstanceActor.class,
    			new ArgMap().add("instanceId", instance.getWfInstanceId()));
    	controlData.setActorHistory(_actorList);

    	// 驳回明细
    	List<SelectItem> rollbackList = new ArrayList<SelectItem>();
    	for (WFInstanceActor act : _actorList) {
    		if (act.getEndTime() != null) {
    			boolean _rollbacknodeExist = false;
    			for (SelectItem a1 : rollbackList) {
    				if (act.getNodeId().equals((String)a1.getValue())) {
    					_rollbacknodeExist = true;
    					break;
    				}
    			}
    			if (!_rollbacknodeExist) {
    				rollbackList.add(Utility.getSelectItem(act.getNodeName(), act.getNodeId()));
    			}
    		}
    	}
    	controlData.setBackNodeList(rollbackList);
    	controlData.setBackNodeId("");

    	// 流程数据
    	windowData.setWindowTitle(wfd.getWfNameCN() + " : " + instance.getWfInstanceId());
    	windowData.setWindowWidth(wfd.getImageWidth() + 3);
    	windowData.setWindowHeight(wfd.getImageHeight() + 32);

    	SysLargeText slt = dbUtility.getEntity(SysLargeText.class, wfd.getDataShow());
    	if (slt.getId() == 0) {
    		windowData.setCustomId(0);
    	} else {
    		windowData.setCustomId(slt.getId());
    		windowData.setCustomVersion(slt.getVersion());
    	}

    	// 流程附件下拉列表

    	windowData.getObjMap().put(WFDIALOG_CONTROLDATA_STRING, controlData);

    	Utility.openWindow("/workflow/wfinstanceimage.jsf?wid=" + ConvManager.getCurrentConvId(), "workflowinstanceimageview",
    			windowData.getWindowWidth(), windowData.getWindowHeight());

    }

    public void openWFDataContent(WindowData parentWindowData, Object obj, WindowData windowData) throws Exception {
    	WorkflowDefine wfd = (WorkflowDefine)obj;
    	SysLargeText slt = null;
    	if (wfd.getDataShow() == 0) {
    		slt = new SysLargeText();
    	} else {
    		slt = dbUtility.getEntity(SysLargeText.class, wfd.getDataShow());
    	}
    	windowData.getObjMap().put("datashow", slt);
    	windowData.setHasCancelButton(true);
    }

    @Audit
    public void saveWFDataContent(ActionEvent event) throws Exception {
    	WorkflowDefine wfd = (WorkflowDefine)this.getWindowData().getInData();
    	SysLargeText slt = (SysLargeText)this.getWindowData().getObjMap().get("datashow");
		slt = dbUtility.update(slt, true);
    	if (wfd.getDataShow() == 0) {
    		wfd.setDataShow(slt.getId());
    		dbUtility.update(wfd);
    	}
    	this.getWindowData().closeWindow();
    }
    
    @Audit
    public void wfRequest(ActionEvent event) throws Exception {
    	long wfId = (Long)event.getComponent().getAttributes().get("wfId");

    	WorkflowDefine wfd = dbUtility.getEntity(WorkflowDefine.class, wfId);
    	WFDataBase dBase = (WFDataBase)Class.forName(wfd.getVariableObjectName()).newInstance();
    	dBase.setWfId(wfId);
    	dBase.setRequestType(WFDataBase.WF_NEW);
    	dBase.setNodeId("startevent1");
    	dBase.initData(null);
    	dBase.initWFDataComponent();
    	if (wfd.getDataShow() > 0) {
    		dBase.setUrl(Utility.baseURL() + "/sys/showdbpage?id=" + wfd.getDataShow() + "&v=" 
    				+ dbUtility.getEntity(SysLargeText.class, wfd.getDataShow()).getVersion());
    	} else {
    		dBase.setUrl("");
    	}
    	WindowDefine wd = new WindowDefine();
    	wd.setWindowTitle("abc");
    	wd.setWindowURL("/workflow/wfrequest.jsf");
    	wd.setWindowType(WindowData.CUSTOM_WINDOW);
    	wd.setWindowWidth(1050);
    	wd.setWindowHeight(700);
    	
    	
    	windowManager.openNewWindow("wf" + wfId, dBase, wd, null, true, "table:refreshtable", ConvManager.CONV_TIMEOUT, false);
    	this.getWindowData().getObjMap().put("wfid", wfd.getDefineId());
    	
    	Utility.executeJavaScript("wfSelectPanel.hide();");
  	
    }
    
    public void initApplyWf() throws Exception {
    	WindowData windowData = this.getWindowData();
    	WFInstance instance = (WFInstance)windowData.getInData();
    	WFInstanceActor actor = dbUtility.getEntity(
    			"select * from wfinstanceactor where wfinstanceid = :instanceId and actorempid = :empId and endtime is null and rownum = 1", 
    			WFInstanceActor.class, new ArgMap().add("instanceId", instance).add("empId", this.getUser().getUserId()));
    	if (actor == null) {
    		throw new AppException("数据已被更改,请刷新表格后重新操作!");
    	}
    	WorkflowDefine wfd = dbUtility.getEntity(WorkflowDefine.class, instance.getWfId());
    	WFDataBase dBase = (WFDataBase)Class.forName(wfd.getVariableObjectName()).newInstance();
    	dBase.setWfId(instance.getWfId());
    	dBase.setNodeId(actor.getNodeId());
    	dBase.setRequestType(WFDataBase.WF_APPLY);
    	dBase.setWfInstanceId(instance.getWfInstanceId());
    	dBase.setReadOnly(false);
    	dBase.setRequireTitle(instance.getReqTitle());
    	dBase.setUrgentLevel(instance.getUrgentLevel());
    	dBase.setWfRemark(instance.getRemark());
    	dBase.loadData(instance.getDataId());
    	dBase.initWFDataComponent();
    	if (wfd.getDataShow() > 0) {
    		dBase.setUrl(Utility.baseURL() + "/sys/showdbpage?id=" + wfd.getDataShow() + "&v=" 
    				+ dbUtility.getEntity(SysLargeText.class, wfd.getDataShow()).getVersion());
    	} else {
    		dBase.setUrl("");
    	}
    	dBase.setAttachFileList(dbUtility.getDataList("select * from sysuploadfile where id in " +
    			"(select fileid from wfattachfile where wfInstanceId = :wfInstanceId)", 
    			SysUploadFile.class, new ArgMap().add("wfInstanceId", instance.getWfInstanceId())));
    	
    	windowData.setInData(dBase);
    	windowData.getObjMap().put("wfid", wfd.getDefineId()); 
    	windowData.setWindowTitle("流程审批:" + instance.getWfInstanceId() + " - " + actor.getNodeName());
    	windowData.setWindowWidth(1050);
    	windowData.setWindowHeight(700);
   	
    }
    
    public void initViewWF() throws Exception {
    	WindowData windowData = this.getWindowData();
    	windowData.setReadOnly(true);
    	WFInstance instance = (WFInstance)windowData.getInData();
    	WorkflowDefine wfd = dbUtility.getEntity(WorkflowDefine.class, instance.getWfId());
    	WFDataBase dBase = (WFDataBase)Class.forName(wfd.getVariableObjectName()).newInstance();
    	dBase.setWfId(instance.getWfId());
    	dBase.setRequestType(WFDataBase.WF_VIEW);
    	dBase.setWfInstanceId(instance.getWfInstanceId());
    	dBase.setReadOnly(true);
    	dBase.setRequireTitle(instance.getReqTitle());
    	dBase.setUrgentLevel(instance.getUrgentLevel());
    	dBase.setWfRemark(instance.getRemark());
    	dBase.setNodeId("");
    	dBase.loadData(instance.getDataId());
    	dBase.initWFDataComponent();
    	if (wfd.getDataShow() > 0) {
    		dBase.setUrl(Utility.baseURL() + "/sys/showdbpage?id=" + wfd.getDataShow() + "&v=" 
    				+ dbUtility.getEntity(SysLargeText.class, wfd.getDataShow()).getVersion());
    	} else {
    		dBase.setUrl("");
    	}
    	dBase.setAttachFileList(dbUtility.getDataList("select * from sysuploadfile where id in " +
    			"(select fileid from wfattachfile where wfInstanceId = :wfInstanceId)", 
    			SysUploadFile.class, new ArgMap().add("wfInstanceId", instance.getWfInstanceId())));
    	
    	windowData.setInData(dBase);
    	windowData.getObjMap().put("wfid", wfd.getDefineId()); 
    	windowData.setWindowTitle("流程查看:" + instance.getWfInstanceId());
    	windowData.setWindowWidth(1050);
    	windowData.setWindowHeight(700);
    }
    
    public void cancelWF(Object obj) throws Exception {
    	WFDataBase db = (WFDataBase)this.getWindowData().getInData();
    	if (db.getRequestType().equals(WFDataBase.WF_NEW)) {
    		for (SysUploadFile suf : db.getAttachFileList()) {
    			dbUtility.delete(suf, true);
    		}
    	}
    }
    
    @Audit
    public void tempSave(ActionEvent event) throws Exception {
    	
    	WFDataBase db = (WFDataBase)this.getWindowData().getInData();
    	db.saveData();
    	db.loadData(db.getDataId());
    	FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("提示:", "信息已保存!"));  
    	Utility.updateComponent(Utility.getComponentId("growl"));
    }
    
    @Audit
    public void submitWf(ActionEvent event) throws Exception {
    	WFDataBase db = (WFDataBase)this.getWindowData().getInData();
    	if (!Utility.notEmptyString(db.getRequireTitle())) {
    		throw new AppException("标题不能为空!");
    	}
    	List<Long> fileList = new ArrayList<Long>();
    	for (SysUploadFile suf : db.getAttachFileList()) {
    		fileList.add(suf.getId());
    	}
    	newWorkFlow(db, fileList, this.getUser().getUserId());
    	this.getWindowData().closeWindow();
    }
    
    @Audit
    public void cancelWf(ActionEvent event) throws Exception {
    	WFDataBase db = (WFDataBase)this.getWindowData().getInData();
    	
    	if (!Utility.notEmptyString(db.getWfInstanceId())) {
    		for (SysUploadFile suf : db.getAttachFileList()) {
    			sysUtil.deleteFile(suf.getId());
    		}
    	}
    	this.getWindowData().closeWindow();
    }
    
    @Audit
    public void uploadAttachFile(FileUploadEvent event) throws Exception {
    	WFDataBase db = (WFDataBase)this.getWindowData().getInData();
    	
    	long fileId = sysUtil.uploadFile(0, "/workflow", event.getFile());
    	db.getAttachFileList().add(dbUtility.getEntity(SysUploadFile.class, fileId));
    	
    	if (Utility.notEmptyString(db.getWfInstanceId())) {
    		WfAttachFile waf = new WfAttachFile();
    		waf.setWfInstanceId(db.getWfInstanceId());
    		waf.setFileId(fileId);
    		dbUtility.save(waf, true);
    	}

    	RequestContext.getCurrentInstance().update(Utility.getComponentId("attachfile"));
    	RequestContext.getCurrentInstance().update(Utility.getComponentId("filelisttable"));
    	
    }
    
    @Audit
    public void deleteAttachFile(ActionEvent event) throws Exception {
    	WFDataBase db = (WFDataBase)this.getWindowData().getInData();
    	
    	long fileId = (Long)event.getComponent().getAttributes().get("fileId");
    	
    	if (Utility.notEmptyString(db.getWfInstanceId())) {
    		WfAttachFilePK pk = new WfAttachFilePK();
    		pk.setWfInstanceId(db.getWfInstanceId());
    		pk.setFileId(fileId);
    		WfAttachFile af = dbUtility.getEntity(WfAttachFile.class, pk);
    		if (af != null) {
    			dbUtility.delete(af, true);
    		}
    	}
    	
    	sysUtil.deleteFile(fileId);
    	
    	List<SysUploadFile> _newFileList = new ArrayList<SysUploadFile>();
    	for (SysUploadFile suf : db.getAttachFileList()) {
    		if (suf.getId() != fileId) {
    			_newFileList.add(suf);
    		}
    	}
    	db.setAttachFileList(_newFileList);
    	RequestContext.getCurrentInstance().update(Utility.getComponentId("filelisttable"));
    	RequestContext.getCurrentInstance().update(Utility.getComponentId("attachfile"));
    	
    }
    
    @Audit
    public void fileNameChange(ValueChangeEvent event) throws Exception {
    	WFDataBase db = (WFDataBase)this.getWindowData().getInData();
    	long fileId = (Long)event.getComponent().getAttributes().get("fileId");
    	
    	event.getComponent().processUpdates(FacesContext.getCurrentInstance());
    	
    	int _pos = 0;
    	for (; _pos < db.getAttachFileList().size(); _pos++) {
    		if (db.getAttachFileList().get(_pos).getId() == fileId) {
    			db.getAttachFileList().set(_pos, dbUtility.update(db.getAttachFileList().get(_pos), true));
    		}
    	}
    	
    	DataTable dt = (DataTable)event.getComponent().findComponent("filelisttable");
    	dt.setEditingRow(false);

    	FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("提示:", "文件名已更新为:" + event.getNewValue()));  
    	Utility.updateComponent(Utility.getComponentId("filelisttable"));
    	Utility.updateComponent(Utility.getComponentId("growl"));
    }
    
    @Audit
    public void downloadAttachFile(ActionEvent event) throws Exception {
    	long fileId = (Long)(event.getComponent().getAttributes().get("fileId"));
    	sysUtil.downloadFile(fileId, "DOWNLOAD");
    }

}
