package com.example.jjmessageservice.connection.auth;

import com.example.jjmessageservice.dto.UserDto;
import com.example.jjmessageservice.exception.AuthenticationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AuthTokenFilter extends OncePerRequestFilter {

    @Value("${users_service.host}")
    private String usersServiceHost;

    @Value("${users_service.port}")
    private String usersServicePort;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            if (request.getCookies() == null) throw new AuthenticationException("Cookies should not be null");
            Optional<Cookie> optCookie = Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equals(SecurityConstants.COOKIE_NAME)).findFirst();
            if (optCookie.isEmpty()) throw new AuthenticationException("Cookie should not be empty");
            UserDto userDto = this.validateUserAuthentication(optCookie.get().getValue());

            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("USER"));
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDto, optCookie.get().getValue(), authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e){
            response.sendError(401, "Cannot set user authentication: " + e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);

    }

    private UserDto validateUserAuthentication(String token) throws URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();
        final String url = "http://" + this.usersServiceHost + ":" + this.usersServicePort + "/api/users/me";
        final String cookieHeader = SecurityConstants.COOKIE_NAME+"="+token;
        URI getUserUri = new URI(url);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookieHeader);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<UserDto> response = restTemplate.exchange(getUserUri, HttpMethod.GET, httpEntity, UserDto.class);


        if (response.getStatusCodeValue() != 200) throw new AuthenticationException("Server could not authenticate request");
        return response.getBody();
    }
}

