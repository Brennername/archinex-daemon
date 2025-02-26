package com.danielremsburg.archinex.plan;

import com.danielremsburg.archinex.storage.StorageSystem;

public class PlanFactory {

    private final StorageSystem storageSystem;

    public PlanFactory(StorageSystem storageSystem) {
        this.storageSystem = storageSystem;
    }

    public Plan createStorePlan() {
        Plan plan = new Plan();
        plan.addAction(new StoreAction(storageSystem));
        return plan;
    }

    public Plan createRetrievePlan() {
        Plan plan = new Plan();
        plan.addAction(new RetrieveAction(storageSystem));
        return plan;
    }

    public Plan createDeletePlan() {
        Plan plan = new Plan();
        plan.addAction(new DeleteAction(storageSystem));
        return plan;
    }

    public Plan createStoreAndRetrievePlan() {
        Plan plan = new Plan();
        plan.addAction(new StoreAction(storageSystem));
        plan.addAction(new RetrieveAction(storageSystem));
        return plan;
    }

    public Plan createComplexPlan() {
        Plan plan = new Plan();
        plan.addAction(new StoreAction(storageSystem));
        return plan;
    }
}