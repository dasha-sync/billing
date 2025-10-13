package api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "billing.weights")
public record BillingWeightsProperties(double request, double cpu, double network, double memory) {}

