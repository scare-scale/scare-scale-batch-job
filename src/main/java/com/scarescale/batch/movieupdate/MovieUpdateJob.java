package com.scarescale.batch.movieupdate;

import com.google.auto.service.AutoService;
import com.scarescale.batch.BatchJob;

import java.util.Objects;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.tools.TmdbException;

@AutoService(BatchJob.class)
public class MovieUpdateJob implements BatchJob {

    private final TmdbApi api;
    private final PostgresController controller;
    private final MovieIterator iterator;

    public MovieUpdateJob() throws TmdbException {
        final String tmdbApiKey = System.getenv("TMDB_API_KEY");
        final String supabaseSecret = System.getenv("SUPABASE_SECRET_KEY");
        final String supabaseProjectId = System.getenv("SUPABASE_PROJECT_ID");

        this.api = new TmdbApi(tmdbApiKey);
        this.controller = new PostgresController(supabaseProjectId, supabaseSecret);
        this.iterator = new MovieIterator(api);
    }

    // Test constructor
    MovieUpdateJob(TmdbApi api, PostgresController controller, MovieIterator iterator) {
        this.api = api;
        this.controller = controller;
        this.iterator = iterator;
    }

    @Override
    public void run() {
        long insertedCount = iterator.stream()
                .filter(Objects::nonNull)
                .peek(controller::insertMovie)
                .count();

        System.out.println("Found " + insertedCount + " horror movies.");
    }
}
