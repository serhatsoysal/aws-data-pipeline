package com.datapipeline.lambda.handler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class FileProcessorHandlerTest {

    @Test
    void testHandlerInstantiation() {
        FileProcessorHandler handler = new FileProcessorHandler();
        assertNotNull(handler);
    }
}

