package com.skubit.shared.dto;

import java.util.Date;

public class BitcoinAddressDto implements Dto {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5045596303631026548L;

	private String label;
	
	private Date createdDate;
	
	private String address;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
}
