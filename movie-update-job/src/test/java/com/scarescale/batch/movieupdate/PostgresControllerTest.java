package com.scarescale.batch.movieupdate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import info.movito.themoviedbapi.model.core.Movie;

class PostgresControllerTest {

    private HttpClient mockClient;
    private HttpResponse<String> mockResponse;
    private PostgresController controller;

    @BeforeEach
    void setUp() {
        mockClient = mock(HttpClient.class);
        mockResponse = mock(HttpResponse.class);

        controller = new PostgresController("project123", "apikey123", mockClient);
    }

    private DBMovie createDbMovie() {
        Movie movie = mock(Movie.class);
        when(movie.getId()).thenReturn(42);
        when(movie.getTitle()).thenReturn("Alien");
        when(movie.getBackdropPath()).thenReturn("/backdrop.jpg");
        when(movie.getPosterPath()).thenReturn("/poster.jpg");
        when(movie.getReleaseDate()).thenReturn("1979-05-25");
        when(movie.getOverview()).thenReturn("In space no one can hear you scream.");

        return new DBMovie(movie);
    }

    @Test
    void insertMovie_sendsCorrectRequest_andSucceedsOn2xx() throws Exception {
        DBMovie movie = createDbMovie();

        when(mockResponse.statusCode()).thenReturn(201);
        when(mockClient.send(
                any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class)
        )).thenReturn(mockResponse);

        controller.insertMovie(movie);

        verify(mockClient).send(any(), any());
    }

    @Test
    void insertMovie_throwsOnNon2xx() throws Exception {
        DBMovie movie = createDbMovie();

        when(mockResponse.statusCode()).thenReturn(500);
        when(mockResponse.body()).thenReturn("Server error");
        when(mockClient.send(
                any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class)
        )).thenReturn(mockResponse);

        assertThrows(RuntimeException.class, () -> controller.insertMovie(movie));
    }

    @Test
    void insertMovie_wrapsIOException() throws Exception {
        DBMovie movie = createDbMovie();

        when(mockClient.send(any(), any())).thenThrow(new IOException("IO fail"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> controller.insertMovie(movie));
        assertTrue(ex.getCause() instanceof IOException);
    }
}