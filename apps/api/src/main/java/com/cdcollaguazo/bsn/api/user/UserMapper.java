package com.cdcollaguazo.bsn.api.user;

import org.springframework.stereotype.Service;

@Service
public class UserMapper {

    public User toUser(UserRequest userRequest ) {
        return User.builder()
                .keycloakId(userRequest.keycloakId())
                .firstName(userRequest.firstName())
                .lastName(userRequest.lastName())
                .email(userRequest.email())
                .build();
    }

    public UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .keycloakId( user.getKeycloakId() )
                .firstName( user.getFirstName() )
                .lastName( user.getLastName() )
                .email( user.getEmail() )
                .dateOfBirth( user.getDateOfBirth() )
                .build();
    }

}
