package com.cdcollaguazo.bsn.api.book;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface BookRepository extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> {

    @Query("""
            SELECT book
            FROM Book book
            WHERE book.archived = false
            AND book.shareable = true
            AND book.owner.keycloakId != :userId
            AND lower(book.title) LIKE lower(concat('%', :searchTerm, '%'))
            """)
    Page<Book> findAllDisplayableBooks(Pageable pageable, String searchTerm, String userId);

    @Query("""
            SELECT book
            FROM User user
            JOIN user.likedBooks book
            WHERE user.keycloakId = :userKeycloakId
            """)
    Page<Book> findLikedBooksByUser(Pageable pageable, String userKeycloakId);

}
