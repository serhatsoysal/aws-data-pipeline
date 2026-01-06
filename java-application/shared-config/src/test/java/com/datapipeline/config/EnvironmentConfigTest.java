package com.datapipeline.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class EnvironmentConfigTest {

    @Test
    void testGetAwsRegion() {
        assertNotNull(EnvironmentConfig.getAwsRegion());
    }
}

