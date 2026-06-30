package id.my.tudemaha.lobos.service;

import id.my.tudemaha.lobos.dto.UserRequest;
import id.my.tudemaha.lobos.exception.DuplicateEmailException;
import id.my.tudemaha.lobos.mapper.UserMapper;
import id.my.tudemaha.lobos.model.User;
import id.my.tudemaha.lobos.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void register(UserRequest userRequest) {
        User newUser = UserMapper.toEntity(userRequest);

        Optional<User> user = userRepository.findByEmail(newUser.getEmail());
        if(user.isPresent()) {
            throw new DuplicateEmailException(newUser.getEmail());
        }

        userRepository.save(newUser);
    }
}
