package com.scarescale.batch.movieupdate;

import org.junit.jupiter.api.Test;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.tools.TmdbException;

class MovieUpdateJobTest {

    @Test
    void insertsAllMovies() {
        TmdbApi api = mock(TmdbApi.class);
        PostgresController controller = mock(PostgresController.class);
        MovieIterator iterator = mock(MovieIterator.class);

        DBMovie m1 = mock(DBMovie.class);
        DBMovie m2 = mock(DBMovie.class);

        when(iterator.stream()).thenReturn(Stream.of(m1, m2));

        MovieUpdateJob job = new MovieUpdateJob(api, controller, iterator);

        job.run();

        verify(controller).insertMovie(m1);
        verify(controller).insertMovie(m2);
    }

    @Test
    void ignoresNullMovies() {
        TmdbApi api = mock(TmdbApi.class);
        PostgresController controller = mock(PostgresController.class);
        MovieIterator iterator = mock(MovieIterator.class);

        DBMovie m = mock(DBMovie.class);

        when(iterator.stream()).thenReturn(Stream.of(null, m));

        MovieUpdateJob job = new MovieUpdateJob(api, controller, iterator);

        job.run();

        verify(controller).insertMovie(m);
        verify(controller, never()).insertMovie(null);
    }

    @Test
    void wrapsTmdbException() {
        TmdbApi api = mock(TmdbApi.class);
        PostgresController controller = mock(PostgresController.class);

        MovieIterator iterator = mock(MovieIterator.class);
        when(iterator.stream()).thenThrow(new RuntimeException(new TmdbException("fail")));

        MovieUpdateJob job = new MovieUpdateJob(api, controller, iterator);

        assertThrows(RuntimeException.class, job::run);
    }
}