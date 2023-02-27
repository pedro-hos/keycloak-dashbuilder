package io.pedrohos.keycloak;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Objects;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import io.quarkus.oidc.AccessTokenCredential;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

@Path("/api")
public class KeycloakAPIProxy {

    private static final Logger LOG = Logger.getLogger(KeycloakAPIProxy.class);
    
    @ConfigProperty(name = "api.keycloak.admin-url")
    private String keycloakAdminUrl;

    @ConfigProperty(name = "api.keycloak.metrics-url")
    private String metricsUrl;
    
    @ConfigProperty(name = "api.keycloak.health-url")
    private String healthUrl;

    @Inject
    AccessTokenCredential accessTokenCredential;

    @GET
    @Produces("application/json")
    public String api(@QueryParam("path") String path) throws IOException {
        var baseUrl = keycloakAdminUrl.endsWith("/") ? keycloakAdminUrl : keycloakAdminUrl + "/";
        return getApiInformation(baseUrl, path, true, false);
    }

    @GET
    @Path("health")
    @Produces("application/json")
    public String health() throws IOException {
        return getApiInformation(healthUrl, null, false, true);
    }

    @GET
    @Path("metrics")
    @Produces("text/plain")
    public String metrics() throws IOException {
        return getApiInformation(metricsUrl, null, false, true);
    }
    
    private String getApiInformation(final String basePath, final String path, final boolean auth, final boolean acepptHttpStatusError) {
        
        try (Client client = ClientBuilder.newClient()) {
            var url = Objects.isNull(path) ? basePath : basePath + path;
            final WebTarget target = client.target(url);
            
            var token = "Bearer " + accessTokenCredential.getToken();
            Response response = auth ? target.request().header("Authorization", token).get() : target.request().get();
            
            if(!acepptHttpStatusError && response.getStatus() != HttpURLConnection.HTTP_OK) {
                LOG.error("Failed : HTTP error code : " + response.getStatus());
                throw new WebApplicationException("Error calling" + url + " - " + response.getStatus() + " HTTP Status");
            }
            
            return response.readEntity(String.class);
        }
        
    }

}