package com.danielremsburg.archinex.cache;

import java.util.UUID;

public interface Cache {

    byte[] get(UUID uuid);

    void put(UUID uuid, byte[] data);

    void remove(UUID uuid);

}