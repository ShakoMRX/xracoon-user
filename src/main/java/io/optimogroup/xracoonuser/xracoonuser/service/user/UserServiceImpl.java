package io.optimogroup.xracoonuser.xracoonuser.service.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.optimogroup.xracoon.shared.models.BaseException;
import io.optimogroup.xracoon.shared.s3.client.exception.BadRequestException;
import io.optimogroup.xracoonuser.xracoonuser.Utils.RequestUtils;
import io.optimogroup.xracoonuser.xracoonuser.dto.AccountDetail;
import io.optimogroup.xracoonuser.xracoonuser.dto.PointsDTO;
import io.optimogroup.xracoonuser.xracoonuser.dto.UserDTO;
import io.optimogroup.xracoonuser.xracoonuser.dto.UserDetailsDTO;
import io.optimogroup.xracoonuser.xracoonuser.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {


    @Value("${app.registry.host.for.user.details}")
    private String hostForRegistry;

    @Value("${app.registry.uri}")
    private String uriForRegistry;

    @Value("${app.accounting-service.host}")
    private String accountingHost;

    @Value("${app.accounting-service.uri}")
    private String accountingUri;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Override
    public String getCurrentUserId() {
        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        Jwt token = jwtAuthenticationToken.getToken();
        String userId = token.getClaim("user_id");
        if (userId.isEmpty()) {
            log.error("Invalid User_id provided!");
            throw new AccessDeniedException("Invalid User_id provided!");
        }
        return userId;
    }

    @Override
    public UserDTO getUserDetails() {
        UserDTO userDTO = new UserDTO();
        try {
            Long partyId = getUserDetailsFromRegistry(userDTO);
            AccountDetail accountDetail = getAccountingDetails(partyId);
            userDTO.setAccountDetail(accountDetail);
            return userDTO;
        } catch (Exception e) {
            e.printStackTrace();
            log.info("Unknown error while retrieve userDetail info from registry");
            throw new BusinessException("Unknown error while retrieve userDetail info from registry");
        }
    }

    public Long getUserDetailsFromRegistry(UserDTO userDTO) {
        try {
            UriComponentsBuilder urlRegistry = UriComponentsBuilder
                    .fromHttpUrl(hostForRegistry + "/" + uriForRegistry);
            var userDetailsFromRegistryResponse = RequestUtils.ServiceCall(
                    log,
                    restTemplate,
                    objectMapper,
                    "get userDetails",
                    urlRegistry
                            .encode()
                            .toUriString(),
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<JsonNode>() {
                    });
            int statusCode = userDetailsFromRegistryResponse.getStatusCode().value();
            UserDetailsDTO userDetailsDTO = objectMapper
                    .treeToValue(userDetailsFromRegistryResponse.getBody(), UserDetailsDTO.class);
            if (statusCode == 200 || statusCode == 201) {
                userDTO.setDetails(userDetailsDTO);
                return userDetailsDTO.getId();
            }
            throw new BusinessException("Invalid user Provided!");
        } catch (Exception e) {
            e.printStackTrace();
            log.info("Error while retrieve UserDetails from registry!");
            throw new BusinessException(e.getMessage());
        }
    }

    public AccountDetail getAccountingDetails(Long partyId) {
        try {
            UriComponentsBuilder accountingUriBuilder = UriComponentsBuilder
                    .fromHttpUrl(accountingHost + "/" + accountingUri);
            var userAccountingResponse = RequestUtils.ServiceCall(
                    log,
                    restTemplate,
                    objectMapper,
                    "get accountDetails",
                    accountingUriBuilder
                            .encode()
                            .toUriString() + "/contracts/accounting-info/" + partyId,
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<JsonNode>() {
                    });
            int statusCode = userAccountingResponse.getStatusCode().value();
            if (statusCode == 200 || statusCode == 201)
                return objectMapper
                        .treeToValue(userAccountingResponse.getBody(), AccountDetail.class);
            throw new BusinessException("Invalid response from accounting server !");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error while retrieve accounting details from accounting for partyId %s".formatted(partyId));
            throw new BusinessException("Error while retrieve accounting details from accounting for partyId %s".formatted(partyId));
        }
    }


}
