/**
 * Copyright zhangjin(zhjin@vip.163.com)
 */
package com.zhjin.wfsys;

import java.io.Serializable;

public class WfAttachFilePK implements Serializable {

	private String wfInstanceId;

	private long fileId;

	@Override
	public boolean equals(Object obj) {
		WfAttachFilePK pk = (WfAttachFilePK)obj;
		return this.wfInstanceId != null && this.wfInstanceId.equals(pk.getWfInstanceId()) && this.fileId != 0 && this.fileId == pk.getFileId();
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
