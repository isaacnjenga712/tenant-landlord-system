package com.apex.mpesa.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DarajaConfig {

    @Value("${mpesa.daraja.consumer-key:}")
    private String consumerKey;

    @Value("${mpesa.daraja.consumer-secret:}")
    private String consumerSecret;

    @Value("${mpesa.daraja.shortcode:174379}")
    private String shortcode;

    @Value("${mpesa.daraja.passkey:}")
    private String passkey;

    @Value("${mpesa.daraja.callback-url:https://example.com/callback}")
    private String callbackUrl;

    @Value("${mpesa.daraja.base-url:https://sandbox.safaricom.co.ke}")
    private String baseUrl;

    @Value("${mpesa.daraja.oauth-url:/oauth/v1/generate?grant_type=client_credentials}")
    private String oauthPath;

    @Value("${mpesa.daraja.stk-push-url:/mpesa/stkpush/v1/processrequest}")
    private String stkPushPath;

    // Core getters
    public String getConsumerKey() { return consumerKey; }
    public String getConsumerSecret() { return consumerSecret; }
    public String getShortcode() { return shortcode; }
    public String getPasskey() { return passkey; }
    public String getCallbackUrl() { return callbackUrl; }
    public String getBaseUrl() { return baseUrl; }

    // Uppercase variants - your code may use any
    public String getShortCode() { return shortcode; }
    public String getPassKey() { return passkey; }

    // OAuth URLs - ALL naming styles
    public String getOauthUrl() { return baseUrl + oauthPath; }
    public String getOauthPath() { return oauthPath; }
    public String getOAuthUrl() { return getOauthUrl(); }
    public String getOauthEndpoint() { return getOauthUrl(); }

    // STK URLs - ALL naming styles - THIS FIXES YOUR ERROR
    public String getStkPushUrl() { return baseUrl + stkPushPath; }
    public String getStkpushUrl() { return getStkPushUrl(); }
    public String getStkPushPath() { return stkPushPath; }
    public String getStkpushPath() { return stkPushPath; }
    public String getStkEndpoint() { return getStkPushUrl(); }
    public String getStkPushEndpoint() { return getStkPushUrl(); }
    public String getStkpushEndpoint() { return getStkPushUrl(); }
    public String getSTKPushUrl() { return getStkPushUrl(); }

    public String getTransactionType() { return "CustomerPayBillOnline"; }
}