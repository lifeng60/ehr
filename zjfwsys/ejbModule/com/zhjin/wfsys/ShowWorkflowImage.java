/**
 * Copyright zhangjin(zhjin@vip.163.com)
 */
package com.zhjin.wfsys;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.repository.DiagramLayout;
import org.activiti.engine.repository.DiagramNode;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;

import com.zhjin.util.ArgMap;
import com.zhjin.util.Utility;

@WebServlet(urlPatterns="/workflow/showworkflowimage")
public class ShowWorkflowImage extends HttpServlet {
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		InputStream iStream = null;

		try {
			response.reset();
			response.setHeader("Pragma","no-cache");
			response.setHeader("Cache-Control","no-cache");
			response.setHeader("Expires","0");
			
	    	ProcessDefinition pdf = WfManager.processEngine.getRepositoryService().createProcessDefinitionQuery().processDefinitionId((String)request.getParameter("wfid")).singleResult();
	    	iStream = WfManager.processEngine.getRepositoryService().getResourceAsStream(pdf.getDeploymentId(), pdf.getDiagramResourceName());

	    	BufferedImage image = ImageIO.read(iStream);	    	

	    	String instanceId = (String)request.getParameter("instanceId");
	    	if (Utility.notEmptyString(instanceId)) {
	    		ProcessInstance processInstance = WfManager.processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId(instanceId).singleResult();
	    		if (processInstance != null) {
	    			Graphics2D g2d = image.createGraphics();
	    			g2d.setColor(Color.RED);
	    			g2d.setStroke(new BasicStroke(2.0f));
	    			
	    			DiagramLayout processDiagramLayout = WfManager.processEngine.getRepositoryService().getProcessDiagramLayout(processInstance.getProcessDefinitionId());
	    			List<DiagramNode> nodes = processDiagramLayout.getNodes();
	    			List<WFInstanceActor> actorList = Utility.getDBUtility().getDataList(
	    					"select * from wfinstanceactor where wfinstanceid = :instanceId union all select * from wfinstanceactorhistory where wfinstanceid = :instanceId", 
	    					WFInstanceActor.class, new ArgMap().add("instanceId", instanceId));
	    			for (WFInstanceActor actor : actorList) {
	    				DiagramNode _n = getDiagramNode(nodes, actor.getNodeId());
	    				if (actor != null) {
	    					g2d.setColor(Color.black);
	    					if (Utility.notEmptyString(actor.getActorLoginName())) {
	    						g2d.drawString(actor.getActorLoginName(), _n.getX().intValue() + 25, _n.getY().intValue() + 18);
	    					}
	    					if (actor.getEndTime() != null) {
	    						g2d.setColor(Color.blue);
	    					} else {
	    						g2d.setColor(Color.red);
	    					}
	    					g2d.drawRoundRect(_n.getX().intValue() , _n.getY().intValue(), _n.getWidth().intValue(), _n.getHeight().intValue(), 20, 20);
	    				}
	    			}
	    		}
	    	}
	    	
	    	ImageIO.write(image, "png", response.getOutputStream()); 
	    	response.getOutputStream().flush();
	    	response.getOutputStream().close();
		} catch (Exception e) {
			System.out.println("ShowImage Error:");
		} finally {
			if (iStream != null) {
				iStream.close();
			}
		}
	}
	
	private DiagramNode getDiagramNode(List<DiagramNode> list, String nodeId) {
		for (DiagramNode a : list) {
			if (a.getId().equals(nodeId)) {
				return a;
			}
		}
		return null;
	}

}
