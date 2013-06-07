/**
 * Copyright zhangjin(zhjin@vip.163.com)
 */
package com.em.ehr.human.entity.person;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.zhjin.base.EntityHasIdBase;

@MappedSuperclass
public class EmployeePersonBaseEntity extends EntityHasIdBase {

	@Column
	private long empId;
	
	@Column(length=255)
	private String remark;

	@Override
	public void insertCheck() throws Exception {
	}

	public long getEmpId() {
		return empId;
	}

	public void setEmpId(long empId) {
		this.empId = empId;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

}
