package com.cdcollaguazo.bsn.api.user;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String keycloakId;

    private String firstName;

    private String lastName;

    private LocalDate dateOfBirth;

    private String email;

}
