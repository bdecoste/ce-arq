package org.jboss.arquillian.ce.cube.oauth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import io.fabric8.utils.Base64Encoder;
import org.jboss.arquillian.ce.httpclient.HttpClient;
import org.jboss.arquillian.ce.httpclient.HttpClientBuilder;
import org.jboss.arquillian.ce.httpclient.HttpRequest;
import org.jboss.arquillian.ce.httpclient.HttpResponse;

/**
 * Created by fspolti on 6/20/16.
 */
public class OauthUtils {
    private static final Logger log = Logger.getLogger(OauthUtils.class.getName());

    private static final String TOKEN_REQUEST_URI = "oauth/token/request?client_id=openshift-challenging-client";
    private static final String OPENSHIFT_URL = "https://localhost:8443";
    private static final String USERNAME = "guest";
    private static final String PASSWORD = "guest";

    public static String getToken(String openshiftUrl, String uid, String pwd) throws Exception {
        String url = openshiftUrl != null ? openshiftUrl : OPENSHIFT_URL;
        String username = uid != null ? uid : USERNAME;
        String password = pwd != null ? pwd : PASSWORD;

        log.info("Issuing a new token for user: " + username);

        HttpRequest request = HttpClientBuilder.doGET(url + "/" + TOKEN_REQUEST_URI);
        request.setHeader("Authorization", "Basic " + Base64Encoder.encode(username + ":" + password));

        HttpClient client = getClient();
        HttpResponse response = client.execute(request);

        StringBuilder result = getResult(new BufferedReader(new InputStreamReader((response.getResponseAsStream()))));

        String token = result.substring(result.indexOf("<code>") + 6, result.indexOf("</code>"));
        log.info("Got token: " + token);

        return token;
    }

    private static StringBuilder getResult(BufferedReader br) throws IOException {
        try {
            StringBuilder result = new StringBuilder();
            String line = "";
            while ((line = br.readLine()) != null) {
                result.append(line);
            }
            return result;
        } finally {
            br.close();
        }
    }

    private static HttpClient getClient() throws Exception {
        return HttpClientBuilder.untrustedConnectionClient();
    }
}