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
public class TransactionDto implements Dto {

    /**
     * 
     */
    private static final long serialVersionUID = -5405919723957516557L;

    private String address;

    private String amount;// BTC

    private String bitcoinTransactionId;

    private Date createdDate;

    private long fee;

    private String note;

    private String purchaseToken;

    private String selfLink;

    private TransactionState transactionState;

    private TransactionType transactionType;

    private Date updatedDate;
    
    private String userId;

    public String getAddress() {
        return address;
    }

    public String getAmount() {
        return amount;
    }

    public String getBitcoinTransactionId() {
        return bitcoinTransactionId;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public long getFee() {
        return fee;
    }

    public String getNote() {
        return note;
    }

    public String getPurchaseToken() {
        return purchaseToken;
    }

    public String getSelfLink() {
        return selfLink;
    }

    public TransactionState getTransactionState() {
        return transactionState;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setBitcoinTransactionId(String bitcoinTransactionId) {
        this.bitcoinTransactionId = bitcoinTransactionId;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public void setFee(long fee) {
        this.fee = fee;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setPurchaseToken(String purchaseToken) {
        this.purchaseToken = purchaseToken;
    }

    public void setSelfLink(String selfLink) {
        this.selfLink = selfLink;
    }

    public void setTransactionState(TransactionState transactionState) {
        this.transactionState = transactionState;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "TransactionDto [selfLink=" + selfLink + ", address=" + address
                + ", amount=" + amount + ", bitcoinTransactionId="
                + bitcoinTransactionId + ", createdDate=" + createdDate
                + ", note=" + note + ", purchaseToken=" + purchaseToken
                + ", transactionState=" + transactionState
                + ", transactionType=" + transactionType + ", updatedDate="
                + updatedDate + ", userId=" + userId + "]";
    }

}

