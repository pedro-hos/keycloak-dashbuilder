package io.pedrohos.keycloak;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;

@PermitAll
@Path("/api")
public class KeycloakAPIProxy {

    @ConfigProperty(name = "api.base-path")
    private String basePath;



    @GET
    @Produces("application/json")
    public String request(@QueryParam("path") String path) throws IOException {
        // TODO:  call the path on the keycloak REST api and return the JSON        
        return Files.readString(Paths.get(basePath, path + ".json"));
        
    }

}
