package com.scarescale.batch.movieupdate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.model.core.Movie;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import info.movito.themoviedbapi.tools.TmdbException;
import info.movito.themoviedbapi.tools.builders.discover.DiscoverMovieParamBuilder;

public class MovieIterator implements Iterator<DBMovie> {
    private static final Integer HORROR_GENRE_ID = 27;
    private static final Integer COMEDY_GENRE_ID = 35;
    private static final Integer FAMILY_GENRE_ID = 10751;
    private static final List<Integer> GENRE_ID_FILTER = List.of(COMEDY_GENRE_ID, FAMILY_GENRE_ID);
    private static final Integer MIN_POPULARITY = 5;
    private static final Integer DAYS_IN_PAST = 182;
    private static final Integer DAYS_IN_FUTURE = 365;

    private final DiscoverMovieParamBuilder discoverMovieParamBuilder = new DiscoverMovieParamBuilder()
            .withGenres(List.of(HORROR_GENRE_ID), false)
            .primaryReleaseDateGte(getPastDate())
            .primaryReleaseDateLte(getFutureDate());

    private final TmdbApi tmdbApi;
    private final Integer totalPageCount;
    private Integer currentPageIndex = 1;
    private Iterator<Movie> currentPageIterator;

    public MovieIterator(final TmdbApi tmdbApi) throws TmdbException {
        this.tmdbApi = tmdbApi;
        this.totalPageCount = tmdbApi.getDiscover().getMovie(discoverMovieParamBuilder).getTotalPages();
        nextPage();
    }

    private void nextPage() {
        try {
            final MovieResultsPage nextPage = tmdbApi.getDiscover().getMovie(discoverMovieParamBuilder.page(currentPageIndex++));
            this.currentPageIterator = nextPage.getResults().iterator();
        } catch (TmdbException e) {
            throw new RuntimeException(e);
        }
    }

    private String getFutureDate() {
        final LocalDate future = LocalDate.now().plusDays(MovieIterator.DAYS_IN_FUTURE);
        return future.format(DateTimeFormatter.ofPattern("yyyy-M-d"));
    }

    private String getPastDate() {
        final LocalDate past = LocalDate.now().minusDays(MovieIterator.DAYS_IN_PAST);
        return past.format(DateTimeFormatter.ofPattern("yyyy-M-d"));
    }

    @Override
    public boolean hasNext() {
        return currentPageIndex < totalPageCount || currentPageIterator.hasNext();
    }

    @Override
    public DBMovie next() {
        while (this.hasNext()) {
            // If there are no more movies in the current page, move to the next page
            if (!currentPageIterator.hasNext()) {
                nextPage();
                continue;
            }

            Movie movie = currentPageIterator.next();

            if (movie.getReleaseDate() == null || movie.getReleaseDate().isBlank()) {
                continue;
            }

            if (movie.getPosterPath() == null || movie.getPosterPath().isBlank()) {
                continue;
            }

            if (movie.getOverview() == null || movie.getOverview().isBlank()) {
                continue;
            }

            if (movie.getPopularity() < MIN_POPULARITY) {
                continue;
            }

            // Exclude Comedy (35) and Family (10751)
            if (movie.getGenreIds().stream().anyMatch(GENRE_ID_FILTER::contains)) {
                continue;
            }

            return new DBMovie(movie);
        }

        return null;
    }

    public Stream<DBMovie> stream() {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(this, Spliterator.ORDERED),
                false
        );
    }
}

