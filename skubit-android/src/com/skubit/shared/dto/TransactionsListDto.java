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

@JsonInclude(Include.NON_NULL)
public class TransactionsListDto extends DtoList<TransactionDto> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5116664622413710302L;

	@Override
	public String toString() {
		return "TransactionsListDto [getNextLink()=" + getNextLink()
				+ ", getPreviousLink()=" + getPreviousLink()
				+ ", getCurrentItemCount()=" + getCurrentItemCount()
				+ ", getItems()=" + getItems() + ", getClass()=" + getClass()
				+ ", hashCode()=" + hashCode() + ", toString()="
				+ super.toString() + "]";
	}

}
