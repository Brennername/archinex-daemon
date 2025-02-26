package com.danielremsburg.archinex.plan;

import com.danielremsburg.archinex.storage.Storage;

public class PlanFactory {

    private final Storage storage;

    public PlanFactory(Storage storage) {
        this.storage = storage;
    }

    public Plan createStorePlan() {
        Plan plan = new Plan();
        plan.addAction(new StoreAction(storage));
        return plan;
    }

    public Plan createRetrievePlan() {
        Plan plan = new Plan();
        plan.addAction(new RetrieveAction(storage));
        return plan;
    }

    public Plan createDeletePlan() {
        Plan plan = new Plan();
        plan.addAction(new DeleteAction(storage));
        return plan;
    }

    public Plan createStoreAndRetrievePlan() {
        Plan plan = new Plan();
        plan.addAction(new StoreAction(storage));
        plan.addAction(new RetrieveAction(storage));
        return plan;
    }

    public Plan createComplexPlan() {
        Plan plan = new Plan();
        plan.addAction(new StoreAction(storage));
        return plan;
    }
}
