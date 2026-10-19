package com.shopsense.commonservice.util;

public final class KafkaGroups {

    private KafkaGroups() {
    }

    public static final String ORCHESTRATOR_GROUP = "orchestrator-group";
    public static final String PAYMENT_GROUP = "payment-group";
    public static final String CATALOG_GROUP = "catalog-group";
    public static final String INVENTORY_GROUP = "inventory-group";
    public static final String ORDER_GROUP = "order-group";

}
