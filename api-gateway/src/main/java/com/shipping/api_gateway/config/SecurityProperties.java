package com.shipping.api_gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "gateway.security")
public class SecurityProperties {
    private List<String> publicPaths = new ArrayList<>();
}
