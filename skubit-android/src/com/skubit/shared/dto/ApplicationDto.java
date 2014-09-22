/**
 * Copyright 2014 Skubit
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.skubit.shared.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ApplicationDto implements Dto {

    /**
	 * 
	 */
    private static final long serialVersionUID = 611848792297726823L;

    private Date createdDate;

    private String name;

    private String selfLink;

    private String vendorId;

    public ApplicationDto() {
    }

    public ApplicationDto(String name, String vendorId) {
        this.name = name;
        this.vendorId = vendorId;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public String getName() {
        return name;
    }

    public String getSelfLink() {
        return selfLink;
    }

    public String getVendorId() {
        return vendorId;
    }

    /**
     * Date the application was initially created
     * 
     * @param createdDate
     */
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSelfLink(String selfLink) {
        this.selfLink = selfLink;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }
}
