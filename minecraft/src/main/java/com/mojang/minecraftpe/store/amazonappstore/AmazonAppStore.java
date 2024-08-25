package com.mojang.minecraftpe.store.amazonappstore;

import android.content.Context;

import com.mojang.minecraftpe.store.ExtraLicenseResponseData;
import com.mojang.minecraftpe.store.Store;
import com.mojang.minecraftpe.store.StoreListener;

public class AmazonAppStore implements Store {
    StoreListener mListener;
    private boolean mForFireTV;

    public AmazonAppStore(Context context, StoreListener listener) {
        mListener = listener;
    }

    public AmazonAppStore(Context context, StoreListener listener, boolean forFireTV) {
        mListener = listener;
        mForFireTV = forFireTV;
    }

    public String getStoreId() {
        return "android.amazonappstore";
    }

    public boolean hasVerifiedLicense() {
        return true;
    }

    public boolean receivedLicenseResponse() {
        return true;
    }

    public void queryProducts(String[] productIds) {
    }

    public void acknowledgePurchase(String receipt, String productType) {
    }

    public void queryPurchases() {
    }

    public String getProductSkuPrefix() {
        return mForFireTV ? "firetv." : "";
    }

    public String getRealmsSkuPrefix() {
        return mForFireTV ? "firetv." : "";
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