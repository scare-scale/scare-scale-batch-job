package com.scarescale.batch.movieupdate;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PostgresController {
    private final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String MOVIES_TABLE_NAME = "movies";

    private final String apiKey;
    private final String supabaseUrl;

    public PostgresController(String projectId, String apiKey) {
        this.apiKey = apiKey;
        this.supabaseUrl = String.format("https://%s.supabase.co/rest/v1/", projectId);
    }

    public void insertMovie(DBMovie movie) {
        try {
            final String json = OBJECT_MAPPER.writeValueAsString(movie);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(supabaseUrl + MOVIES_TABLE_NAME))
                    .header("apikey", this.apiKey)
                    .header("Content-Type", "application/json")
                    .header("Prefer", "return=representation")
                    .header("Prefer", "resolution=merge-duplicates,return=representation")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (!(response.statusCode() >= 200 && response.statusCode() < 300)) {
                throw new RuntimeException("Error " + response.statusCode() + ": " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
