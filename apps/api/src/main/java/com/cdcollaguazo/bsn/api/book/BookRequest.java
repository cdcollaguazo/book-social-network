package com.cdcollaguazo.bsn.api.book;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record BookRequest(
    Integer id,
    @NotNull(message = "Title can't be null")
    @NotEmpty(message = "Title can't be empty")
    String title,
    @NotNull(message = "Author name can't be null")
    @NotEmpty(message = "Author name can't be empty")
    String authorName,
    @NotNull(message = "Isbn can't be null")
    @NotEmpty(message = "Isbn can't be empty")
    String isbn,
    @NotNull(message = "Synopsis can't be null")
    @NotEmpty(message = "Synopsis can't be empty")
    String synopsis,
    boolean shareable
){
}
