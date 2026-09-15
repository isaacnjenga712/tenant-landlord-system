package com.apex.mpesa.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "daraja")
public class DarajaConfig {
	
	private String consumerKey;
    private String consumerSecret;
    private String passkey;
    private String shortcode;
    private String callbackUrl;
    private String baseUrl;
    private String oauthUrl;
    private String stkpushUrl;

}
