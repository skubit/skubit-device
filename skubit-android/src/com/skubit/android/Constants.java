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

package com.skubit.android;

public class Constants {

    public static final boolean IS_PRODUCTION = false;

    public static final boolean LOG_LEVEL_FULL = !IS_PRODUCTION;

    public static final String SHARED_PREFERENCE = "com.skubit.shared";

    public static final String SKUBIT_AUTH = IS_PRODUCTION ? "https://catalog.skubit.com/rest"
            : "https://catalog.skubit.net/rest";

    public static final String SKUBIT_CATALOG = IS_PRODUCTION ? "https://catalog.skubit.com/rest/v1"
            : "https://catalog.skubit.net/rest/v1";

}
