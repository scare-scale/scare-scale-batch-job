package com.scarescale.batch.movieupdate;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.movito.themoviedbapi.model.core.Movie;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DBMovieTest {

    @Test
    void constructor_populatesFieldsCorrectly() {
        Movie movie = mock(Movie.class);

        when(movie.getId()).thenReturn(42);
        when(movie.getTitle()).thenReturn("Alien");
        when(movie.getBackdropPath()).thenReturn("/backdrop.jpg");
        when(movie.getPosterPath()).thenReturn("/poster.jpg");
        when(movie.getReleaseDate()).thenReturn("1979-05-25");
        when(movie.getOverview()).thenReturn("In space no one can hear you scream.");

        DBMovie dbMovie = new DBMovie(movie);

        assertEquals(42, dbMovie.getId());
        assertEquals("Alien", dbMovie.getTitle());
        assertEquals("/backdrop.jpg", dbMovie.getBackdropPath());
        assertEquals("/poster.jpg", dbMovie.getPosterPath());
        assertEquals("In space no one can hear you scream.", dbMovie.getOverview());

        // Formatted as d/M/yyyy
        assertEquals("25/5/1979", dbMovie.getReleaseDate());
    }

    @Test
    void jsonSerialization_matchesExpectedProperties() throws Exception {
        Movie movie = mock(Movie.class);

        when(movie.getId()).thenReturn(1);
        when(movie.getTitle()).thenReturn("Test Movie");
        when(movie.getBackdropPath()).thenReturn("/back.jpg");
        when(movie.getPosterPath()).thenReturn("/poster.jpg");
        when(movie.getReleaseDate()).thenReturn("2000-01-02");
        when(movie.getOverview()).thenReturn("A test movie.");

        DBMovie dbMovie = new DBMovie(movie);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(dbMovie);

        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"name\":\"Test Movie\""));
        assertTrue(json.contains("\"tmdbBackdropId\":\"/back.jpg\""));
        assertTrue(json.contains("\"tmdbPosterId\":\"/poster.jpg\""));
        assertTrue(json.contains("\"synopsis\":\"A test movie.\""));
        assertTrue(json.contains("\"releaseDate\":\"2/1/2000\""));
    }
}