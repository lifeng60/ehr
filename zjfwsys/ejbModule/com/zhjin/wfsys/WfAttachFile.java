/**
 * Copyright zhangjin(zhjin@vip.163.com)
 */
package com.zhjin.wfsys;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

import com.zhjin.base.EntityBase;

@Entity
@IdClass(value = WfAttachFilePK.class)
public class WfAttachFile extends EntityBase {

	@Id
	@Column(length=40)
	private String wfInstanceId;
	
	@Id
	private long fileId;
	
	@Override
	public String getRowKey() {
		return this.wfInstanceId + ":" + fileId;
	}

	public String getWfInstanceId() {
		return wfInstanceId;
	}

	public void setWfInstanceId(String wfInstanceId) {
		this.wfInstanceId = wfInstanceId;
	}

	public long getFileId() {
		return fileId;
	}

	public void setFileId(long fileId) {
		this.fileId = fileId;
	}

}
