package com.cdcollaguazo.bsn.api.book;

import org.springframework.data.jpa.domain.Specification;

public class BookSpecification {

    public static Specification<Book> withOwnerId(String ownerId){
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("owner").get("keycloakId"), ownerId);
    }

}
