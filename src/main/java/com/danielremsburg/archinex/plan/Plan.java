package com.danielremsburg.archinex.plan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Plan {

    private final List<Action> actions = new ArrayList<>();

    public void addAction(Action action) {
        actions.add(action);
    }

    public void execute(UUID uuid, byte[] data, Map<String, String> metadata) throws IOException {
        for (Action action : actions) {
            action.execute(uuid, data, metadata);
        }
    }

    public List<Action> getActions() {
        return new ArrayList<>(actions); // Return a copy
    }
}