package com.cdcollaguazo.bsn.api.book;

import com.cdcollaguazo.bsn.api.common.BaseEntity;
import com.cdcollaguazo.bsn.api.feedback.Feedback;
import com.cdcollaguazo.bsn.api.history.BookTransactionHistory;
import com.cdcollaguazo.bsn.api.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Book extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "book_seq")
    @SequenceGenerator(
            name = "book_seq",
            sequenceName = "book_seq",
            allocationSize = 25
    )
    private Integer id;

    private String title;

    private String authorName;

    private String isbn;

    @Column(length = 1025)
    private String synopsis;

    private String bookCover;

    private boolean archived;

    private boolean shareable;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "book")
    private List<Feedback> feedbacks;

    @OneToMany(mappedBy = "book")
    private List<BookTransactionHistory> histories;

    @ManyToMany(mappedBy = "likedBooks")
    private Set<User> userLikes = new HashSet<>();

    @Transient
    public double getRate() {
        if(feedbacks == null || feedbacks.isEmpty()){
            return 0.0;
        }
        double rate = this.feedbacks.stream()
                .mapToDouble(Feedback::getNote)
                .average()
                .orElse(0.0);
        return Math.round(rate * 10.0) / 10.0;
    }

}
