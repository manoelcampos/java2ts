package io.github.manoelcampos.java2ts.sample.dto;

import java.util.List;

/**
 * A generic page of results returned by a REST API.
 * @param content the items in the page
 * @param page the page number, starting from 0
 * @param totalElements the total number of items in all pages
 * @param <T> the type of the items
 */
public record PageResponse<T>(List<T> content, int page, long totalElements) {
}
