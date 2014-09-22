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
public class SkuDetailsDto implements Dto {

    /**
	 * 
	 */
    private static final long serialVersionUID = 2622661535056107903L;

    private String application;

    private String description;

    private String productId;

    private ProductState productState = ProductState.INACTIVE;

    private PurchaseDataStatus purchaseDataStatus;

    private ProductRecurrence recurrence;// required if subscription

    private long satoshi;

    private String selfLink;

    private String title;

    private PurchasingType type;

    private Date updatedDate;

    private String vendorId;

    public String getApplication() {
        return application;
    }

    public String getDescription() {
        return description;
    }

    public String getProductId() {
        return productId;
    }

    public ProductState getProductState() {
        return productState;
    }

    public PurchaseDataStatus getPurchaseDataStatus() {
        return purchaseDataStatus;
    }

    public ProductRecurrence getRecurrence() {
        return recurrence;
    }

    public long getSatoshi() {
        return satoshi;
    }

    public String getSelfLink() {
        return selfLink;
    }

    public String getTitle() {
        return title;
    }

    public PurchasingType getType() {
        return type;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setProductState(ProductState productState) {
        this.productState = productState;
    }

    public void setPurchaseDataStatus(PurchaseDataStatus purchaseDataStatus) {
        this.purchaseDataStatus = purchaseDataStatus;
    }

    public void setRecurrence(ProductRecurrence recurrence) {
        this.recurrence = recurrence;
    }

    public void setSatoshi(long satoshi) {
        this.satoshi = satoshi;
    }

    public void setSelfLink(String selfLink) {
        this.selfLink = selfLink;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setType(PurchasingType productType) {
        this.type = productType;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

}
