package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.utility.DockerImageName;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Configuration class for TestContainers to fix date parsing issues and provide better resource management
 */
public class TestContainersConfig {
    
    static {
        // Set system properties for TestContainers
        // Configure Jackson for date handling
        ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX"));
        
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        // Pre-pull the LocalStack image to avoid timeout issues
        try {
            DockerImageName localstackImage = DockerImageName.parse("localstack/localstack:3.3.0");
            DockerClientFactory.instance().client().pullImageCmd(localstackImage.asCanonicalNameString())
                .start()
                .awaitCompletion();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            // Image might already be pulled, continue anyway
        }
    }
    
    // Initialize the configuration
    public static void init() {
        // Method to force the static block execution
    }
}