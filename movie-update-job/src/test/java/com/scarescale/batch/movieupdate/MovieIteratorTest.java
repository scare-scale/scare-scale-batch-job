package com.scarescale.batch.movieupdate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbDiscover;
import info.movito.themoviedbapi.model.core.Movie;
import info.movito.themoviedbapi.model.core.MovieResultsPage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

class MovieIteratorTest {
    @Mock
    private TmdbApi mockTmdbApi;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testMovieIteratorInitialization() {
        assertNotNull(mockTmdbApi);
    }

    @Test
    void returnsValidMoviesOnly() throws Exception {
        TmdbApi api = mock(TmdbApi.class);
        TmdbDiscover discover = mock(TmdbDiscover.class);

        when(api.getDiscover()).thenReturn(discover);

        Movie valid = mock(Movie.class);
        when(valid.getReleaseDate()).thenReturn("2024-01-01");
        when(valid.getPosterPath()).thenReturn("/poster.jpg");
        when(valid.getOverview()).thenReturn("A scary movie");
        when(valid.getPopularity()).thenReturn(10.0);
        when(valid.getGenreIds()).thenReturn(List.of(27)); // horror

        MovieResultsPage page = mock(MovieResultsPage.class);
        when(page.getTotalPages()).thenReturn(1);
        when(page.getResults()).thenReturn(List.of(valid));

        when(discover.getMovie(any())).thenReturn(page);

        MovieIterator iterator = new MovieIterator(api);

        DBMovie result = iterator.next();

        assertNotNull(result);
        assertEquals(valid.getId(), result.getId());
    }

    @Test
    void filtersOutMoviesWithMissingFields() throws Exception {
        TmdbApi api = mock(TmdbApi.class);
        TmdbDiscover discover = mock(TmdbDiscover.class);

        when(api.getDiscover()).thenReturn(discover);

        Movie missingPoster = mock(Movie.class);
        when(missingPoster.getReleaseDate()).thenReturn("2024-01-01");
        when(missingPoster.getPosterPath()).thenReturn(null);
        when(missingPoster.getOverview()).thenReturn("desc");
        when(missingPoster.getPopularity()).thenReturn(10.0);
        when(missingPoster.getGenreIds()).thenReturn(List.of(27));

        Movie valid = mock(Movie.class);
        when(valid.getReleaseDate()).thenReturn("2024-01-01");
        when(valid.getPosterPath()).thenReturn("/poster.jpg");
        when(valid.getOverview()).thenReturn("desc");
        when(valid.getPopularity()).thenReturn(10.0);
        when(valid.getGenreIds()).thenReturn(List.of(27));

        MovieResultsPage page = mock(MovieResultsPage.class);
        when(page.getTotalPages()).thenReturn(1);
        when(page.getResults()).thenReturn(List.of(missingPoster, valid));

        when(discover.getMovie(any())).thenReturn(page);

        MovieIterator iterator = new MovieIterator(api);

        DBMovie result = iterator.next();

        assertNotNull(result);
        assertEquals(valid.getPosterPath(), result.getPosterPath());
    }

    @Test
    void filtersOutComedyMovies() throws Exception {
        TmdbApi api = mock(TmdbApi.class);
        TmdbDiscover discover = mock(TmdbDiscover.class);

        when(api.getDiscover()).thenReturn(discover);

        // Comedy movie (should be filtered out)
        Movie comedy = mock(Movie.class);
        when(comedy.getReleaseDate()).thenReturn("2024-01-01");
        when(comedy.getPosterPath()).thenReturn("/poster.jpg");
        when(comedy.getOverview()).thenReturn("desc");
        when(comedy.getPopularity()).thenReturn(10.0);
        when(comedy.getGenreIds()).thenReturn(List.of(35)); // comedy genre

        // Valid horror movie (should be returned)
        Movie horror = mock(Movie.class);
        when(horror.getReleaseDate()).thenReturn("2024-01-01");
        when(horror.getPosterPath()).thenReturn("/poster.jpg");
        when(horror.getOverview()).thenReturn("desc");
        when(horror.getPopularity()).thenReturn(10.0);
        when(horror.getGenreIds()).thenReturn(List.of(27)); // horror genre

        MovieResultsPage page = mock(MovieResultsPage.class);
        when(page.getTotalPages()).thenReturn(1);
        when(page.getResults()).thenReturn(List.of(comedy, horror));

        when(discover.getMovie(any())).thenReturn(page);

        MovieIterator iterator = new MovieIterator(api);

        // Collect all results
        List<DBMovie> results = iterator.stream().toList();

        // Assertions
        assertEquals(1, results.size());
        DBMovie onlyMovie = results.getFirst();

        // Ensure the comedy movie was filtered out by checking the ID/title/etc.
        assertEquals(horror.getId(), onlyMovie.getId());
    }

    @Test
    void returnsNullWhenNoValidMovies() throws Exception {
        TmdbApi api = mock(TmdbApi.class);
        TmdbDiscover discover = mock(TmdbDiscover.class);

        when(api.getDiscover()).thenReturn(discover);

        Movie invalid = mock(Movie.class);
        when(invalid.getReleaseDate()).thenReturn(null);

        MovieResultsPage page = mock(MovieResultsPage.class);
        when(page.getTotalPages()).thenReturn(1);
        when(page.getResults()).thenReturn(List.of(invalid));

        when(discover.getMovie(any())).thenReturn(page);

        MovieIterator iterator = new MovieIterator(api);

        DBMovie result = iterator.next();

        assertNull(result);
    }

    @Test
    void handlesMultiplePages() throws Exception {
        TmdbApi api = mock(TmdbApi.class);
        TmdbDiscover discover = mock(TmdbDiscover.class);

        when(api.getDiscover()).thenReturn(discover);

        Movie invalid = mock(Movie.class);
        when(invalid.getReleaseDate()).thenReturn(null);

        Movie valid = mock(Movie.class);
        when(valid.getReleaseDate()).thenReturn("2024-01-01");
        when(valid.getPosterPath()).thenReturn("/poster.jpg");
        when(valid.getOverview()).thenReturn("desc");
        when(valid.getPopularity()).thenReturn(10.0);
        when(valid.getGenreIds()).thenReturn(List.of(27));

        MovieResultsPage page1 = mock(MovieResultsPage.class);
        when(page1.getTotalPages()).thenReturn(2);
        when(page1.getResults()).thenReturn(List.of(invalid));

        MovieResultsPage page2 = mock(MovieResultsPage.class);
        when(page2.getTotalPages()).thenReturn(2);
        when(page2.getResults()).thenReturn(List.of(valid));

        when(discover.getMovie(any()))
                .thenReturn(page1)
                .thenReturn(page2);

        MovieIterator iterator = new MovieIterator(api);

        DBMovie result = iterator.next();

        assertNotNull(result);
        assertEquals(valid.getPosterPath(), result.getPosterPath());
    }
}

