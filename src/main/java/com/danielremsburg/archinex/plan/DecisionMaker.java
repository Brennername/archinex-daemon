package com.danielremsburg.archinex.plan;

import com.danielremsburg.archinex.metadata.FileMetadata;
import com.danielremsburg.archinex.config.ArchinexConfig;

public class DecisionMaker implements Decision {

    private final PlanFactory planFactory;
    private final ArchinexConfig config;

    public DecisionMaker(PlanFactory planFactory, ArchinexConfig config) {
        this.planFactory = planFactory;
        this.config = config;
    }

    @Override
    public Plan choosePlan(FileMetadata metadata) {
        // default 1024 * 1024 = 1G
        long fileSizeThreshold = config.getLongPropertyOrDefault("plan.fileSizeThreshold", 1024 * 1024);

        if (metadata.getSize() > fileSizeThreshold) {
            return planFactory.createComplexPlan();
        } else {
            return planFactory.createStorePlan();
        }
    }
}