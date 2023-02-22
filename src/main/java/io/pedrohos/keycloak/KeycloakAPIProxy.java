package io.pedrohos.keycloak;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import io.quarkus.oidc.AccessTokenCredential;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;

@PermitAll
@Path("/api")
public class KeycloakAPIProxy {
    
    private static final Logger LOG = Logger.getLogger(KeycloakAPIProxy.class);

    @ConfigProperty(name = "api.keycloak.base-url")
    private String keycloakBaseUrl;
    
    @Inject
    AccessTokenCredential accessTokenCredential;

    @GET
    @Produces("application/json")
    public String request(@QueryParam("path") String path) throws IOException {
        return retrieveRestInfo(path);
    }

    private String retrieveRestInfo(final String path) {
        
        StringBuilder body = new StringBuilder();
        
        try {

            URL url = new URL(keycloakBaseUrl + path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + accessTokenCredential.getToken());

            if (conn.getResponseCode() != 200) {
                LOG.error("Failed : HTTP error code : " + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            String output;
            
            while ((output = br.readLine()) != null) {
                body.append(output);
            }

            conn.disconnect();

        } catch (MalformedURLException e) {
            LOG.error("Failed during retrieve REST API: ", e);
        } catch (IOException e) {
            LOG.error("Failed during retrieve REST API : ", e);
        }
        
        return body.toString();
        
    }

}
