package com.cdcollaguazo.bsn.api.feedback;

import com.cdcollaguazo.bsn.api.common.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("feedbacks")
@Tag(name = "Feedback")
public class FeedBackController {

    private final FeedBackService feedBackService;


    public FeedBackController(FeedBackService feedBackService) {
        this.feedBackService = feedBackService;
    }

    @PostMapping
    public ResponseEntity<Integer> saveFeedback(
        @Valid @RequestBody FeedbackRequest request,
        Authentication connectedUser
    ) {
        return ResponseEntity.ok(feedBackService.save(request, connectedUser));
    }

    @GetMapping("book/{book-id}")
    public ResponseEntity<PageResponse<FeedbackResponse>> findAllFeedbacksByBook(
            @PathVariable("book-id") Integer bookId,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(feedBackService.findAllFeedbacksByBook(bookId, page, size, connectedUser));
    }

}
