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

import com.fasterxml.jackson.annotation.JsonProperty;

// https://google-styleguide.googlecode.com/svn/trunk/jsoncstyleguide.xml#Top-Level_Reserved_Property_Names
public class ErrorMessage implements Dto {

    /**
	 * 
	 */
    private static final long serialVersionUID = 3699695277368465994L;

    private int code;

    private String message;

    public ErrorMessage() {
    }

    public ErrorMessage(@JsonProperty("code")
    int code,
            @JsonProperty("message")
            String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
