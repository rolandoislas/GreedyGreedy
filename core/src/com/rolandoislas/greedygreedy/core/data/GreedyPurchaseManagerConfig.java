package com.rolandoislas.greedygreedy.core.data;

import com.badlogic.gdx.pay.Offer;
import com.badlogic.gdx.pay.OfferType;
import com.badlogic.gdx.pay.PurchaseManagerConfig;

public class GreedyPurchaseManagerConfig extends PurchaseManagerConfig {
    public static final String PURCHASE_POINTS_200 = "points200";
    public static final String PURCHASE_POINTS_600 = "points600";
    public static final String PURCHASE_POINTS_1000 = "points1000";

    public GreedyPurchaseManagerConfig() {
        addOffer(new Offer().setType(OfferType.CONSUMABLE).setIdentifier(PURCHASE_POINTS_200));
        addOffer(new Offer().setType(OfferType.CONSUMABLE).setIdentifier(PURCHASE_POINTS_600));
        addOffer(new Offer().setType(OfferType.CONSUMABLE).setIdentifier(PURCHASE_POINTS_1000));
        addStoreParam(PurchaseManagerConfig.STORE_NAME_ANDROID_GOOGLE, Constants.BILLING_KEY_GOOGLE);
    }
}
