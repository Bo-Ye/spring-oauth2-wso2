package com.boye.spring;

import org.apache.log4j.Logger;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;


public class APIManagerClient {
    private static final Logger logger = Logger.getLogger(APIManagerClient.class);
    private final static String TOKEN_URL = "https://localhost:8243/token";
    private final static String CLIENT_ID = "0d6YBfUzZfvDwxlQ768Cjl0voesa";  //substitute yours for it
    private final static String CLIENT_SECRET = "wfprQBED0MUiufJnUSTZVOwFxgoa"; //substitute yours for it
    private final static String USERNAME = "admin"; //substitute yours for it
    private final static String PASSWORD = "admin"; //substitute yours for it
    private final static String API_URL = "https://localhost:8243/httpbin/1.0/ip";

    public static void main(String[] args) throws NoSuchAlgorithmException, KeyManagementException {
        new APIManagerClient().callAPI();
    }

    private void setNoSSL(OAuth2RestTemplate oAuth2RestTemplate) throws KeyManagementException, NoSuchAlgorithmException {
        //request factory
        ClientHttpRequestFactory requestFactory = new SSLContextRequestFactory();
        oAuth2RestTemplate.setRequestFactory(requestFactory);
        //provider
        ResourceOwnerPasswordAccessTokenProvider provider = new ResourceOwnerPasswordAccessTokenProvider();
        provider.setRequestFactory(requestFactory);
        oAuth2RestTemplate.setAccessTokenProvider(provider);
    }

    private void setInterceptor(OAuth2RestTemplate oAuth2RestTemplate) {
        //set interceptors
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new OAuth2RequestInterceptor());
        oAuth2RestTemplate.setInterceptors(interceptors);
    }

    private void callAPI() throws KeyManagementException, NoSuchAlgorithmException {
        ResourceOwnerPasswordResourceDetails resource = new ResourceOwnerPasswordResourceDetails();
        resource.setAccessTokenUri(TOKEN_URL);
        resource.setClientId(CLIENT_ID);
        resource.setClientSecret(CLIENT_SECRET);
        resource.setGrantType("password");
        resource.setUsername(USERNAME);
        resource.setPassword(PASSWORD);
        //create rest template
        OAuth2RestTemplate oAuth2RestTemplate = new OAuth2RestTemplate(resource);
        this.setNoSSL(oAuth2RestTemplate); //to ignore ssl
        this.setInterceptor(oAuth2RestTemplate); // the critical trick to workaround OAuth2 token issue.
        String result = oAuth2RestTemplate.getForObject(API_URL, String.class);
        logger.info("Result is " + result);
    }
}
