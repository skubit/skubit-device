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
public class PurchaseDataDto implements Dto {

    /**
	 * 
	 */
    private static final long serialVersionUID = -1266523373187680699L;

    private String application;

    private String developerPayload;

    private String id;

    private Date orderCreatedDate;

    private String orderId; // needs to be updated somewhere

    private String productId;

    private PurchaseDataStatus purchaseDataStatus = PurchaseDataStatus.NEW;

    private long satoshi;

    private String selfLink;

    private PurchasingType type;

    private Date updatedDate;

    private String userId;

    public String getApplication() {
        return application;
    }

    public String getDeveloperPayload() {
        return developerPayload;
    }

    public String getId() {
        return id;
    }

    public Date getOrderCreatedDate() {
        return orderCreatedDate;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getProductId() {
        return productId;
    }

    public PurchaseDataStatus getPurchaseDataStatus() {
        return purchaseDataStatus;
    }

    public PurchasingType getPurchasingType() {
        return type;
    }

    public long getSatoshi() {
        return satoshi;
    }

    public String getSelfLink() {
        return selfLink;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public void setDeveloperPayload(String developerPayload) {
        this.developerPayload = developerPayload;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setOrderCreatedDate(Date orderCreatedDate) {
        this.orderCreatedDate = orderCreatedDate;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setPurchaseDataStatus(PurchaseDataStatus purchaseDataStatus) {
        this.purchaseDataStatus = purchaseDataStatus;
    }

    public void setPurchasingType(PurchasingType type) {
        this.type = type;
    }

    public void setSatoshi(long satoshi) {
        this.satoshi = satoshi;
    }

    public void setSelfLink(String selfLink) {
        this.selfLink = selfLink;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
