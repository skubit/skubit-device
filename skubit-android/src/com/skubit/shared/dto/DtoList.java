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

import java.util.ArrayList;

public abstract class DtoList<T> implements Dto {

    /**
	 * 
	 */
    private static final long serialVersionUID = -3982851172980416150L;

    private int currentItemCount;

    private ArrayList<T> items;

    private String nextLink;

    private String previousLink;

    public int getCurrentItemCount() {
        return items.size();
    }

    public ArrayList<T> getItems() {
        return items;
    }

    /*
     * Link to go to the next page of items
     */
    public String getNextLink() {
        return nextLink;
    }

    /**
     * Link to go to the previous page of items
     * 
     * @return
     */
    public String getPreviousLink() {
        return previousLink;
    }

    public void setItems(ArrayList<T> items) {
        this.items = items;
    }

    /**
     * Do not include in POST or PUT
     * 
     * @param nextLink
     */
    public void setNextLink(String nextLink) {
        this.nextLink = nextLink;
    }

    /**
     * Do not include in POST or PUT
     * 
     * @param previousLink
     */
    public void setPreviousLink(String previousLink) {
        this.previousLink = previousLink;
    }
}
