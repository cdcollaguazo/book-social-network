package com.cdcollaguazo.bsn.api.history;

import com.cdcollaguazo.bsn.api.book.Book;
import com.cdcollaguazo.bsn.api.common.BaseEntity;
import com.cdcollaguazo.bsn.api.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class BookTransactionHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "book_transaction_history_seq")
    @SequenceGenerator(
            name = "book_transaction_history_seq",
            sequenceName = "book_transaction_history_seq",
            allocationSize = 25
    )
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    private boolean returned;

    private boolean returnApproved;

}
