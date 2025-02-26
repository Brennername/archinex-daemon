package com.danielremsburg.archinex.journal;

import java.time.Instant;

public interface Journal {

    void log(String message);

    void log(String message, Instant timestamp);

}