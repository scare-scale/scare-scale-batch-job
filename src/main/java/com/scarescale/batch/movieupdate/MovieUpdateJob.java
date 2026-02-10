package com.scarescale.batch.movieupdate;

import com.google.auto.service.AutoService;
import com.scarescale.batch.BatchJob;

import java.util.Objects;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.tools.TmdbException;

@AutoService(BatchJob.class)
public class MovieUpdateJob implements BatchJob {

    public void run() {
        final String tmdbApiKey = System.getenv("TMDB_API_KEY");
        final String supabaseSecret = System.getenv("SUPABASE_SECRET_KEY");
        final String supabaseProjectId = System.getenv("SUPABASE_PROJECT_ID");

        try {
            final TmdbApi api = new TmdbApi(tmdbApiKey);
            final PostgresController controller = new PostgresController(supabaseProjectId, supabaseSecret);

            final MovieIterator movieIterator = new MovieIterator(api);

            long insertedCount = movieIterator.stream()
                    .filter(Objects::nonNull)
                    .peek(controller::insertMovie)
                    .count();

            System.out.println("Found " + insertedCount + " horror movies.");
        } catch (TmdbException e) {
            throw new RuntimeException(e);
        }
    }

}
