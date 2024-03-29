package ru.dubovitsky.memorush.security.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SuccessfulAuthenticationResponse {

    private String token;
    private String username;

}
