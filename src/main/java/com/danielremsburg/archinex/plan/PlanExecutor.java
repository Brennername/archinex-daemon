package com.danielremsburg.archinex.plan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

public class PlanExecutor {

    private static final Logger logger = LoggerFactory.getLogger(PlanExecutor.class);

    private final ExecutorService executorService;

    public PlanExecutor(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void executePlan(Plan plan, UUID uuid, byte[] data, Map<String, String> metadata) {
        executorService.submit(() -> {
            try {
                plan.execute(uuid, data, metadata);
                logger.info("Plan executed successfully for UUID: {}", uuid);
            } catch (IOException e) {
                logger.error("Error executing plan for UUID: {}", uuid, e);
                // Log the error and potentially implement a retry mechanism
                logger.error("Retrying plan execution for UUID: {}", uuid);
                try {
                    plan.execute(uuid, data, metadata); // Retry once
                    logger.info("Plan execution retried successfully for UUID: {}", uuid);
                } catch (IOException retryException) {
                    logger.error("Plan execution failed after retry for UUID: {}", uuid, retryException);
                }
            }
        });
    }
}