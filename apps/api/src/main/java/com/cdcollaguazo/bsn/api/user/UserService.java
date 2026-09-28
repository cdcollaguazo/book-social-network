package com.cdcollaguazo.bsn.api.user;

import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public void saveUser(UserRequest userRequest) {
        userRepository.save(userMapper.toUser(userRequest));
    }

}
