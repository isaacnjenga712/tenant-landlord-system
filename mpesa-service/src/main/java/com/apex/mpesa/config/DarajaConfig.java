package com.apex.mpesa.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DarajaConfig {

    @Value("${daraja.consumer.key:}")
    private String consumerKey;

    @Value("${daraja.consumer.secret:}")
    private String consumerSecret;

    @Value("${daraja.shortcode:174379}")
    private String shortcode;

    @Value("${daraja.passkey:}")
    private String passkey;

    @Value("${daraja.callback.url:}")
    private String callbackUrl;

    @Value("${daraja.base.url:https://sandbox.safaricom.co.ke}")
    private String baseUrl;

    @Value("${daraja.oauth.url:${daraja.base.url}/oauth/v1/generate?grant_type=client_credentials}")
    private String oauthUrl;

    @Value("${daraja.stkpush.url:${daraja.base.url}/mpesa/stkpush/v1/processrequest}")
    private String stkPushUrl;

    public String getConsumerKey() { return consumerKey; }
    public String getConsumerSecret() { return consumerSecret; }
    public String getShortcode() { return shortcode; }
    public String getShortCode() { return shortcode; }
    public String getPasskey() { return passkey; }
    public String getPassKey() { return passkey; }
    public String getCallbackUrl() { return callbackUrl; }
    public String getBaseUrl() { return baseUrl; }

    public String getOauthUrl() { return oauthUrl; }
    public String getOAuthUrl() { return oauthUrl; }
    public String getOauthPath() { return oauthUrl; }
    public String getOauthEndpoint() { return oauthUrl; }

    public String getStkPushUrl() { return stkPushUrl; }
    public String getStkpushUrl() { return stkPushUrl; }
    public String getSTKPushUrl() { return stkPushUrl; }
    public String getStkPushPath() { return stkPushUrl; }
    public String getStkpushPath() { return stkPushUrl; }
    public String getStkEndpoint() { return stkPushUrl; }
    public String getStkPushEndpoint() { return stkPushUrl; }
    public String getStkpushEndpoint() { return stkPushUrl; }

    public String getTransactionType() { return "CustomerPayBillOnline"; }
}