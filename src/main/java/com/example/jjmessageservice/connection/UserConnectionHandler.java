package com.example.jjmessageservice.connection;

import austral.ingsis.jjpostservice.dto.UserDto;
import austral.ingsis.jjpostservice.exception.AuthenticationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Component
public class UserConnectionHandler {
    @Value("${users_service.host}")
    private String usersServiceHost;

    @Value("${users_service.port}")
    private String usersServicePort;

    public UserDto getTokenUserInformation() {
        return (UserDto) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public UserDto getUserInfoFromId(Long userId) {
        RestTemplate restTemplate = new RestTemplate();
        final String url = "http://" + usersServiceHost + ":" + usersServicePort + "/api/users/" + userId;

        URI getUserUri = null;
        try {
            getUserUri = new URI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", SecurityContextHolder.getContext().getAuthentication().getCredentials().toString());
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<UserDto> response = restTemplate.exchange(getUserUri, HttpMethod.GET, httpEntity, UserDto.class);
        if (response.getStatusCodeValue() != 200) throw new AuthenticationException("Authentication Server couldn't authenticate jwt");

        return response.getBody();
    }

    public List<UserDto> getFollowedUsersById(Long id) throws URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();
        final String url = "http://" + usersServiceHost + ":" + usersServicePort + "/api/followers/" + id;
        URI getUserUri = new URI(url);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", SecurityContextHolder.getContext().getAuthentication().getCredentials().toString());
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<List<UserDto>> response = restTemplate.exchange(getUserUri, HttpMethod.GET, httpEntity, new ParameterizedTypeReference<>() {});
        if (response.getStatusCodeValue() != 200) throw new AuthenticationException("Authentication Server couldn't authenticate jwt");

        return response.getBody();
    }

}

