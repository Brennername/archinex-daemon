package com.danielremsburg.archinex.plan;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public interface Action {

    void execute(UUID uuid, byte[] data, Map<String, String> metadata) throws IOException;

}