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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * '{ "orderId":"12999763169054705758.1371079406387615",
 * "packageName":"com.example.app", "productId":"exampleSku",
 * "purchaseTime":1345678900000, "purchaseState":0,
 * "developerPayload":"bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ",
 * "purchaseToken":"rojeslcdyyiapnqcynkjyyjh" }'
 */
@JsonInclude(Include.NON_NULL)
public class InAppPurchaseDataDto implements Dto {

    /**
	 * 
	 */
    private static final long serialVersionUID = 5084394931968154572L;

    private String application;

    private String id;

    private String message;

    private String productId;

    private PurchasingType purchasingType;

    private String selfLink;

    private String signature;

    private String userId;

    public String getApplication() {
        return application;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getProductId() {
        return productId;
    }

    public PurchasingType getPurchasingType() {
        return purchasingType;
    }

    public String getSelfLink() {
        return selfLink;
    }

    public String getSignature() {
        return signature;
    }

    public String getUserId() {
        return userId;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setPurchasingType(PurchasingType purchasingType) {
        this.purchasingType = purchasingType;
    }

    public void setSelfLink(String selfLink) {
        this.selfLink = selfLink;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "InAppPurchaseDataDto [id=" + id + ", message=" + message
                + ", signature=" + signature + ", selfLink=" + selfLink + "]";
    }

}
