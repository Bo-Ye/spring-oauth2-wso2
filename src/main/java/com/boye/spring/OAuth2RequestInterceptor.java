package com.boye.spring;

import org.apache.log4j.Logger;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * Created by boy on 3/12/15.
 */
public class OAuth2RequestInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger logger = Logger.getLogger(OAuth2RequestInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        log(request);
        //wso2 api manager is case-sensitive of Bearer keyword while other OAuth2 implementations are not case-sensitive for Bearer keyword
        String auth = request.getHeaders().entrySet().stream().filter(entry -> entry.getKey().equals("Authorization")).findFirst().get().getValue().get(0);
        request.getHeaders().set("Authorization", auth.replace("bearer", "Bearer"));
        return execution.execute(request, body);
    }

    private void log(HttpRequest request) throws IOException {
        request.getHeaders().entrySet().forEach(header -> logger.debug(header.getKey() + " : " + String.join(" ", header.getValue())));
    }
}
