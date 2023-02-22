package io.pedrohos.keycloak;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import io.quarkus.oidc.AccessTokenCredential;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;

@PermitAll
@Path("/api")
public class KeycloakAPIProxy {

    private static final Logger LOG = Logger.getLogger(KeycloakAPIProxy.class);

    @ConfigProperty(name = "api.keycloak.base-url")
    private String keycloakBaseUrl;

    @ConfigProperty(name = "quarkus.oidc.auth-server-url")
    private String spiBaseUrl;

    @Inject
    AccessTokenCredential accessTokenCredential;

    @GET
    @Produces("application/json")
    public String api(@QueryParam("path") String path) throws IOException {
        var baseUrl = keycloakBaseUrl.endsWith("/") ? keycloakBaseUrl : keycloakBaseUrl + "/";
        return getInfo(baseUrl, path, true);
    }

    @GET
    @Path("health")
    @Produces("application/json")
    public String health() throws IOException {
        var baseUrl = spiBaseUrl.endsWith("/") ? spiBaseUrl : spiBaseUrl + "/";
        return getInfo(baseUrl, "health/check", false);
    }

    @GET
    @Path("metrics")
    @Produces("text/plain")
    public String metrics() throws IOException {
        var baseUrl = spiBaseUrl.endsWith("/") ? spiBaseUrl : spiBaseUrl + "/";
        return getInfo(baseUrl, "metrics", false);
    }

    private String getInfo(String basePath, final String path, boolean auth) {

        try {
            var url = new URL(basePath + path);
            var conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            if (auth) {
                conn.setRequestProperty("Authorization", "Bearer " + accessTokenCredential.getToken());
            }
            if (conn.getResponseCode() != 200) {
                LOG.error("Failed : HTTP error code : " + conn.getResponseCode());
                throw new WebApplicationException("Error calling Keycloak: " + conn.getResponseCode());
            }

            var result = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            conn.disconnect();
            return result;

        } catch (IOException e) {
            LOG.error("Failed during retrieve REST API: ", e);
            throw new WebApplicationException("Failed during retrieve REST API", e);
        }

    }

}
