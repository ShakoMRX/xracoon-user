package io.optimogroup.xracoonuser.xracoonuser.Utils;

import com.fasterxml.jackson.databind.ObjectMapper;
//import io.optimogroup.xracoon.shared.models.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.RestTemplate;


@Slf4j
public class RequestUtils {

    public static <T> ResponseEntity<T> ServiceCall(Logger log, RestTemplate restTemplate, ObjectMapper objectMapper, String action, String url, HttpMethod method,
                                                    HttpEntity<?> requestEntity, ParameterizedTypeReference<T> responseType,
                                                    Object... uriVariables)  {

        log.debug("call api action={} url={} uriVariables={}", action, url, uriVariables);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        HttpHeaders headers = new HttpHeaders();

        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            log.info(jwtAuthenticationToken.toString());
            Jwt token = jwtAuthenticationToken.getToken();
            log.info(token != null ? token.getTokenValue() : "TOKEN IS NULL");
            if (token == null || token.getTokenValue().isEmpty()) {
                log.error("Invalid token provided!");
                throw new AccessDeniedException("Invalid token provided!");
            }
            headers.add("Authorization", "Bearer " + token.getTokenValue());
            log.info(requestEntity.getHeaders().toString());
        }
        headers.addAll(requestEntity.getHeaders());

        HttpEntity<?> http = new HttpEntity<>(requestEntity.getBody(), headers);

        return restTemplate.exchange(url,
                method,
                http, responseType, uriVariables);

    }

}
