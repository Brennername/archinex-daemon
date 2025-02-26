package com.danielremsburg.archinex.plan;

import com.danielremsburg.archinex.metadata.FileMetadata;

public interface Decision {

    Plan choosePlan(FileMetadata metadata);

}