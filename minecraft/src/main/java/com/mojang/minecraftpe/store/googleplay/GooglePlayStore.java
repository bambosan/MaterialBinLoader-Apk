package com.mojang.minecraftpe.store.googleplay;

import com.mojang.minecraftpe.MainActivity;
import com.mojang.minecraftpe.store.ExtraLicenseResponseData;
import com.mojang.minecraftpe.store.Store;
import com.mojang.minecraftpe.store.StoreListener;

public class GooglePlayStore implements Store {
    MainActivity mActivity;
    StoreListener mListener;

    public GooglePlayStore(MainActivity activity, String licenseKey, StoreListener listener) {
        mActivity = activity;
        mListener = listener;
        mListener.onStoreInitialized(true);
    }

    public String getStoreId() {
        return "android.googleplay";
    }

    public boolean hasVerifiedLicense() {
        return true;
    }

    public void queryProducts(String[] productIds) {
    }

    public void acknowledgePurchase(String receipt, String productType) {
    }

    public void queryPurchases() {
    }

    public String getProductSkuPrefix() {
        return "";
    }

    public String getRealmsSkuPrefix() {
        return "";
    }

    public boolean receivedLicenseResponse() {
        return true;
    }

    public void destructor() {
    }

    @Override
    public ExtraLicenseResponseData getExtraLicenseData() {
        return new ExtraLicenseResponseData(0L, 0L, 0L);
    }

    public void purchase(String productId, boolean isSubscription, String payload) {
    }

    public void purchaseGame() {
    }
}