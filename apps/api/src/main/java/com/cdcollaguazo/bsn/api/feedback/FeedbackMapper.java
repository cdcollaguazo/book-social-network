package com.cdcollaguazo.bsn.api.feedback;

import com.cdcollaguazo.bsn.api.book.Book;
import com.cdcollaguazo.bsn.api.user.User;
import com.cdcollaguazo.bsn.api.user.UserMapper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Service
public class FeedbackMapper {

    private final UserMapper userMapper;

    public FeedbackMapper( UserMapper userMapper ) {
        this.userMapper = userMapper;
    }

    public Feedback toFeedback(FeedbackRequest feedbackRequest, Book book, User user) {
        return Feedback.builder()
                .note(feedbackRequest.note())
                .comment(feedbackRequest.comment())
                .book(book)
                .user(user)
                .build();
    }

    public FeedbackResponse toFeedbackResponse(Feedback feedback, String id) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy");
        String dateFormatted = feedback.getCreatedDate().format(formatter);
        return FeedbackResponse.builder()
                .note(feedback.getNote())
                .comment(feedback.getComment())
                .ownFeedback(Objects.equals(feedback.getUser().getKeycloakId(), id))
                .user( userMapper.toUserResponse( feedback.getUser() ) )
                .createdDate( dateFormatted )
                .build();
    }
}
