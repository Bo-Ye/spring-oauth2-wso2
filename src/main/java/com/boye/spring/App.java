package com.boye.spring;

import org.apache.log4j.Logger;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.*;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;


public class App {
    private static final Logger logger = Logger.getLogger(App.class);

    public static void main(String[] args) throws NoSuchAlgorithmException, KeyManagementException {
        HttpsURLConnection.setDefaultHostnameVerifier(
                new javax.net.ssl.HostnameVerifier() {
                    public boolean verify(final String hostname,
                                          final javax.net.ssl.SSLSession sslSession) {
                        if (hostname.equals("172.20.22.105")) {
                            return true;
                        }
                        return false;
                    }
                });
        ResourceOwnerPasswordResourceDetails resource = new ResourceOwnerPasswordResourceDetails();
        String tokenUrl = "https://localhost:8243/token";
        resource.setAccessTokenUri(tokenUrl);
        resource.setClientId("ByUoEg4Pltfkz5Ntc3KOSJogQ24a");
        resource.setClientSecret("OQvwuxLsBxIMTPhVt4JZyfOItUsa");
        resource.setGrantType("password");
        resource.setUsername("admin");
        resource.setPassword("admin");
        AccessTokenRequest atr = new DefaultAccessTokenRequest();
        //create rest template
        OAuth2RestTemplate oAuth2RestTemplate = new OAuth2RestTemplate(resource, new DefaultOAuth2ClientContext(atr));
        //request factory
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{new DumbX509TrustManager()}, null);
        ClientHttpRequestFactory requestFactory = new SSLContextRequestFactory(sslContext);
        oAuth2RestTemplate.setRequestFactory(requestFactory);
        //provider
        ResourceOwnerPasswordAccessTokenProvider provider = new ResourceOwnerPasswordAccessTokenProvider();
        provider.setRequestFactory(requestFactory);
        oAuth2RestTemplate.setAccessTokenProvider(provider);
        //set interceptors/requestFactory
        ClientHttpRequestInterceptor interceptor = new LoggingRequestInterceptor();
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(interceptor);
        oAuth2RestTemplate.setInterceptors(interceptors);
        oAuth2RestTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(requestFactory));
        //get token
        String token = oAuth2RestTemplate.getAccessToken().getValue();
        System.out.println(token);
        String result = oAuth2RestTemplate.getForObject("https://172.20.22.105:8243/httpbin/1.0/ip", String.class);
        System.out.println(result);
    }

    private static class SSLContextRequestFactory extends SimpleClientHttpRequestFactory {

        private final SSLContext sslContext;

        public SSLContextRequestFactory(final SSLContext sslContext) {
            this.sslContext = sslContext;
        }

        @Override
        protected void prepareConnection(final HttpURLConnection connection, final String httpMethod) throws IOException {
            if (connection instanceof HttpsURLConnection) {
                ((HttpsURLConnection) connection).setSSLSocketFactory(this.sslContext.getSocketFactory());
            }
            super.prepareConnection(connection, httpMethod);
        }
    }

    private static class DumbX509TrustManager implements X509TrustManager {

        public void checkClientTrusted(final java.security.cert.X509Certificate[] chain, final String authType) {

        }

        public void checkServerTrusted(final java.security.cert.X509Certificate[] chain, final String authType) {

        }

        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

    private static final class LoggingRequestInterceptor implements ClientHttpRequestInterceptor {

        private static final Logger logger = Logger.getLogger(LoggingRequestInterceptor.class);

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

            String auth = request.getHeaders().entrySet().stream().filter(entry -> entry.getKey().equals("Authorization")).findAny().get().getValue().get(0);
            request.getHeaders().set("Authorization", auth.replace("bearer", "Bearer"));
            log(request, body);
            ClientHttpResponse response = execution.execute(request, body);


            return response;
        }

        private void log(HttpRequest request, byte[] body) throws IOException {
            request.getHeaders().entrySet().forEach(header -> logger.debug(header.getKey() + " : " + String.join(" ", header.getValue())));
        }
    }
}
