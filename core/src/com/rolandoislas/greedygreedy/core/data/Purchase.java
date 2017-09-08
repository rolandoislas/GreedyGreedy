package com.rolandoislas.greedygreedy.core.data;

import com.google.gson.JsonElement;

public class Purchase {
    private int purchase_type;
    private int item_id;

    public int getPurchaseType() {
        return purchase_type;
    }

    public int getItemId() {
        return item_id;
    }

    public void setItemId(int itemId) {
        this.item_id = itemId;
    }

    public void setPurchaseType(int purchaseType) {
        this.purchase_type = purchaseType;
    }

    public enum Type {
        ICON
    }
}
