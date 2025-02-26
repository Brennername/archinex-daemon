package com.danielremsburg.archinex.plan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class DataTransformationAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(DataTransformationAction.class);

    private final DataTransformer transformer;

    public DataTransformationAction(DataTransformer transformer) {
        this.transformer = transformer;
    }

    @Override
    public void execute(UUID uuid, byte[] data, Map<String, String> metadata) throws IOException {
        transformAndProcess(uuid, data, (transformedData) -> {
            if (transformedData != null) {
                logger.info("DataTransformationAction executed successfully for UUID: {}", uuid);
            }
        });
    }

    public void transformAndProcess(UUID uuid, byte[] data, Consumer<byte[]> dataConsumer) throws IOException {
        try {
            byte[] transformedData = transformer.transform(data);
            dataConsumer.accept(transformedData);
        } catch (IOException e) {
            logger.error("Error transforming data for UUID: {}", uuid, e);
            throw e;
        }
    }

    public interface DataTransformer {
        byte[] transform(byte[] data) throws IOException;
    }

    public static class ReverseTransformer implements DataTransformer {
        @Override
        public byte[] transform(byte[] data) throws IOException {
            byte[] reversed = new byte[data.length];
            for (int i = 0; i < data.length; i++) {
                reversed[i] = data[data.length - 1 - i];
            }
            return reversed;
        }
    }
}