/**
 * Copyright zhangjin(zhjin@vip.163.com)
 */
package com.zhjin.wfsys.test;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.zhjin.base.entity.OperateDataBase;

@Entity
public class WfTestDataEntry extends OperateDataBase {
	@Column
	private int i;
	
	@Column
	private String s;
	
	@Column
	private Date d;
	
	@Column
	private boolean b;
	
	public int getI() {
		return i;
	}
	public void setI(int i) {
		this.i = i;
	}
	public String getS() {
		return s;
	}
	public void setS(String s) {
		this.s = s;
	}
	public Date getD() {
		return d;
	}
	public void setD(Date d) {
		this.d = d;
	}
	public boolean isB() {
		return b;
	}
	public void setB(boolean b) {
		this.b = b;
	}

}
