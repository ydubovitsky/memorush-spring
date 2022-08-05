package ru.dubovitsky.flashcardsspring.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dubovitsky.flashcardsspring.dto.request.UserUpdateRequestDto;
import ru.dubovitsky.flashcardsspring.model.User;
import ru.dubovitsky.flashcardsspring.model.enums.RoleEnum;
import ru.dubovitsky.flashcardsspring.repository.UserRepository;

import java.util.Set;

@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User getUserByUsername(String name) {
        return userRepository.findByUsername(name)
                .orElseThrow(() -> new RuntimeException(String.format("User with name - %s not found", name)));
    }

    public User addNewUser(User user) {
        boolean present = userRepository.findByUsername(user.getUsername()).isPresent();
        if(present) {
            throw new RuntimeException(String.format("User with name - %s not found", user.getUsername()));
        }
        //TODO В каком месте добавить проверку?
        if(!user.getPassword().equals(user.getPassword2())) {
            throw new RuntimeException("Password doesn't equals password2");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(RoleEnum.USER);
        userRepository.save(user);
        log.info(String.format("New user %s registered", user.getUsername()));
        return user;
    }

    public User updateUser(UserUpdateRequestDto userUpdateRequestDto) {
        User user = userRepository.findByUsername(userUpdateRequestDto.getUsername()).orElseThrow(
                () -> new RuntimeException(
                        String.format("User with name - %s not found", userUpdateRequestDto.getUsername())));
        user.setPassword(userUpdateRequestDto.getPassword());
        user.setEmail(userUpdateRequestDto.getEmail());
        log.info(String.format("User %s updated", user.getUsername()));
        return userRepository.save(user);
    }


}