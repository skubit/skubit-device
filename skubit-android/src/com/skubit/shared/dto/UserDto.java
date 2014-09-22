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

public class UserDto implements Dto {

    /**
	 * 
	 */
    private static final long serialVersionUID = 4548823909918422169L;

    private String contactWebsite;

    private String depositAddress;

    private String email;

    private String logoutUrl;

    private String payoutAddress;

    private String subject;

    private String userId;

    private String userName;

    public UserDto() {
    }

    public UserDto(String userId, String email, String userName,
            String logoutUrl) {
        this.userId = userId;
        this.email = email;
        this.logoutUrl = logoutUrl;
        this.userName = userName;
    }

    public String getContactWebsite() {
        return contactWebsite;
    }

    public String getDepositAddress() {
        return depositAddress;
    }

    public String getEmail() {
        return email;
    }

    public String getLogoutUrl() {
        return logoutUrl;
    }

    public String getPayoutAddress() {
        return payoutAddress;
    }

    public String getSubject() {
        return subject;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setContactWebsite(String contactWebsite) {
        this.contactWebsite = contactWebsite;
    }

    public void setDepositAddress(String depositAddress) {
        this.depositAddress = depositAddress;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setLogoutUrl(String logoutUrl) {
        this.logoutUrl = logoutUrl;
    }

    public void setPayoutAddress(String payoutAddress) {
        this.payoutAddress = payoutAddress;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
