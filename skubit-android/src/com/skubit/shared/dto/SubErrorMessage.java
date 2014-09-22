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

public class SubErrorMessage implements Dto {

    /**
	 * 
	 */
    private static final long serialVersionUID = -4220631167013696405L;

    private String domain;

    private String message;

    private String reason;// like a code

    public SubErrorMessage() {
    }

    public SubErrorMessage(String message, String reason, String domain) {
        this.message = message;
        this.reason = reason;
        this.domain = domain;
    }

    public String getDomain() {
        return domain;
    }

    public String getMessage() {
        return message;
    }

    public String getReason() {
        return reason;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "SubErrorMessage [message=" + message + ", reason=" + reason
                + ", domain=" + domain + "]";
    }

}
