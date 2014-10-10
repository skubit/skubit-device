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

package com.skubit.android.billing;

import android.os.Parcel;
import android.os.Parcelable;

public class PurchaseData implements Parcelable {

    public static final Parcelable.Creator<PurchaseData> CREATOR = new Parcelable.Creator<PurchaseData>() {
        @Override
        public PurchaseData createFromParcel(Parcel parcel) {
            PurchaseData info = new PurchaseData();
            info.apiVersion = parcel.readInt();
            info.packageName = parcel.readString();
            info.versionCode = parcel.readInt();
            info.sku = parcel.readString();
            info.signatureHash = parcel.readString();

            if (parcel.readByte() == 1) {
                info.developerPayload = parcel.readString();
            }
            info.type = parcel.readString();
            return info;
        }

        @Override
        public PurchaseData[] newArray(int size) {
            return new PurchaseData[size];
        }
    };

    public int apiVersion;

    public String developerPayload;

    public String packageName;

    public String signatureHash;

    public String sku;

    public int versionCode;

    public String type;

    public PurchaseData() {
    }

    public PurchaseData(int apiVersion, String packageName, int versionCode,
            String sku, String signatureHash, String developerPayload, String type) {
        this.apiVersion = apiVersion;
        this.packageName = packageName;
        this.versionCode = versionCode;
        this.signatureHash = signatureHash;
        this.developerPayload = developerPayload;
        this.sku = sku;
        this.type = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "PurchaseData [apiVersion=" + apiVersion + ", packageName="
                + packageName + ", versionCode=" + versionCode
                + ", signatureHash=" + signatureHash + ", developerPayload="
                + developerPayload + ", sku=" + sku + "]";
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(apiVersion);
        parcel.writeString(packageName);
        parcel.writeInt(versionCode);
        parcel.writeString(sku);
        parcel.writeString(signatureHash);
        if (developerPayload != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.developerPayload);
        }
        parcel.writeString(type);
    }
}
