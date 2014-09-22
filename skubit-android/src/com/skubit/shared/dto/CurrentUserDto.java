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

public class CurrentUserDto implements Dto {

    private static final long serialVersionUID = 2356552659273942875L;

    private Boolean loggedIn;

    private UserDto user;

    public CurrentUserDto() {
    }

    public CurrentUserDto(boolean loggedIn, UserDto user) {
        this.loggedIn = loggedIn;
        this.user = user;
    }

    public UserDto getUser() {
        return user;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

}
