package com.cdcollaguazo.bsn.api.feedback;

import jakarta.validation.constraints.*;

public record FeedbackRequest(
        @Positive(message = "Note must be positive")
        @Min(value = 0, message = "Note can't be less than 0")
        @Max(value = 5, message = "Note can't be more than 5")
        Double note,

        @NotNull(message = "Comment can't be null")
        @NotEmpty(message = "Comment can't be empty")
        @NotBlank(message = "Comment can't be blank")
        String comment,

        @NotNull(message = "BookId can't be null")
        Integer bookId
) {
}
