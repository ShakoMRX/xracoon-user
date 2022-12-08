package io.optimogroup.xracoonuser.xracoonuser.service.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.optimogroup.xracoon.shared.models.BaseException;
import io.optimogroup.xracoon.shared.s3.client.exception.BadRequestException;
import io.optimogroup.xracoonuser.xracoonuser.Utils.RequestUtils;
import io.optimogroup.xracoonuser.xracoonuser.dto.*;
import io.optimogroup.xracoonuser.xracoonuser.exception.BusinessException;
import io.optimogroup.xracoonuser.xracoonuser.model.User;
import io.optimogroup.xracoonuser.xracoonuser.repository.UserRepository;
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
import org.webjars.NotFoundException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {


    private final UserRepository userRepository;
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
    public User get(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User Not Found with id %s".formatted(userId)));
    }

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
            userDTO.setAvatar(UserAvatarDTO.builder()
                    .path("2")
                    .code("#F44336")
                    .build());
            return userDTO;
        } catch (Exception e) {
            e.printStackTrace();
            log.info("Unknown error while retrieve userDetail info from registry");
            throw new BusinessException("Unknown error while retrieve userDetail info from registry");
        }
    }

    @Override
    public List<PointsTransactionDTO> getUserPointTransactions(Long partyId) {
        if (partyId == null) throw new BusinessException("Invalid party id provided!");
        User user = get(partyId);
        Long regId = user.getPartyId();
        if (regId == null) throw new BusinessException("invalid reg id provided! for use %s ".formatted(partyId));
        try {
            UriComponentsBuilder accountingUriBuilder = UriComponentsBuilder
                    .fromHttpUrl(accountingHost + "/" + accountingUri);
            var pointTransactions = RequestUtils.ServiceCall(
                    log,
                    restTemplate,
                    objectMapper,
                    "get user pointTransactions",
                    accountingUriBuilder
                            .encode()
                            .toUriString() + "/contracts/point-transactions/" + regId,
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<JsonNode>() {
                    });
            if (pointTransactions.getBody() != null)
                return objectMapper.readValue(pointTransactions.getBody().traverse(), new TypeReference<>() {
                });
            throw new BusinessException("Unknown error while retrieve user points transactions! with partyId %s");
        } catch (Exception e) {
            e.printStackTrace();
            log.info("Unknown error while retrieve user points transactions! with partyId %s");
            throw new BusinessException("Unknown error while retrieve user points transactions! with partyId %s");
        }
    }

    @Override
    public UserAvatarDTO uploadAvatar(Long userId, String path, String code) {
        User user = get(userId);
        if (userId != null) throw new BusinessException("Invalid userId provided!");
        if (path.isEmpty() || code.isEmpty()) throw new BusinessException("Invalid parameters provided!");
        return UserAvatarDTO.builder()
                .code(code)
                .path(path)
                .build();
    }

    @Override
    public void updateUser(Long id, UserDetailsDTO detailsDTO) {
        try {

            User user = get(id);
            Long partyId = user.getPartyId();
            String uuId = user.getUserUuid();
            detailsDTO.setUuid(uuId);
            detailsDTO.setObjectType("customer");
            List<ContactDTO> existingContacts = new ArrayList<>();
            List<ContactDTO> newContracts = new ArrayList<>();
            List<ContactDTO> contacts = detailsDTO.getContactInfos();
            if (contacts != null) {
                contacts.forEach(c -> c.setRegId(partyId));
                for (ContactDTO contract : contacts) {
                    if (contract.getId() != null)
                        existingContacts.add(contract);
                    else newContracts.add(contract);
                }
            }

            UriComponentsBuilder urlRegistry = UriComponentsBuilder
                    .fromHttpUrl(hostForRegistry + "/" + uriForRegistry);
            var res = RequestUtils.ServiceCall(
                    log,
                    restTemplate,
                    objectMapper,
                    "get userDetails",
                    urlRegistry
                            .encode()
                            .toUriString() + "/" + partyId,
                    HttpMethod.PUT,
                    new HttpEntity<>(detailsDTO),
                    new ParameterizedTypeReference<JsonNode>() {
                    });
            int statusCode = res.getStatusCode().value();
            if (statusCode == 200 || statusCode == 201 && contacts.size() > 0) {
                var nCRes = RequestUtils.ServiceCall(
                        log,
                        restTemplate,
                        objectMapper,
                        "get userDetails",
                        urlRegistry
                                .encode()
                                .toUriString() + "/" + partyId + "/contact-info",
                        HttpMethod.PUT,
                        new HttpEntity<>(newContracts),
                        new ParameterizedTypeReference<JsonNode>() {
                        });
                for (ContactDTO exContact : existingContacts) {
                    var exCRes = RequestUtils.ServiceCall(
                            log,
                            restTemplate,
                            objectMapper,
                            "get userDetails",
                            urlRegistry
                                    .encode()
                                    .toUriString() + "/" + partyId + "/contact-info/" + exContact.getId(),
                            HttpMethod.PUT,
                            new HttpEntity<>(exContact),
                            new ParameterizedTypeReference<JsonNode>() {
                            });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("error while update user with id %s !".formatted(id));
            throw new BusinessException("error while update user with id %s !".formatted(id));
        }
    }

//    @Override
//    public UserAvatarDTO uploadAvatar(Long userId, String path, String code) {
//
//    }

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
                            .toUriString() + "/user-id",
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<JsonNode>() {
                    });
            int statusCode = userDetailsFromRegistryResponse.getStatusCode().value();
            UserDetailsDTO userDetailsDTO = objectMapper
                    .treeToValue(userDetailsFromRegistryResponse.getBody(), UserDetailsDTO.class);
            if (statusCode == 200 || statusCode == 201) {
                User user = saveUser(userDetailsDTO);
                userDTO.setDetails(UserDetailsResponseDTO
                        .builder()
                        .id(user.getId())
                        .objectType(userDetailsDTO.getObjectType())
                        .personalId(userDetailsDTO.getPersonalId())
                        .legalAddressId(userDetailsDTO.getLegalAddressId())
                        .gender(userDetailsDTO.getGender())
                        .contactInfos(userDetailsDTO.getContactInfos())
                        .firstName(userDetailsDTO.getFirstName())
                        .lastName(userDetailsDTO.getLastName())
                        .registryType(userDetailsDTO.getRegistryType())
                        .physicalAddressId(userDetailsDTO.getPhysicalAddressId())
                        .build()
                );
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

    public User saveUser(UserDetailsDTO UserDetailsDTO) {
        try {
            User user = new User();
            if (UserDetailsDTO != null) {
                String uuId = UserDetailsDTO.getUuid();
                if (uuId.isEmpty()) throw new BusinessException("Invalid user uuId provided!");
                if (UserDetailsDTO.getId() == null) throw new BusinessException("Invalid user partyId provided!");
                user.setUserUuid(uuId);
                user.setPartyId(UserDetailsDTO.getId());
                if (!userRepository.existsByUserUuid(uuId))
                    return userRepository.save(user);
                else {
                    User existingUser = userRepository.findByUserUuid(uuId);
                    existingUser.setPartyId(UserDetailsDTO.getId());
                    return userRepository.save(existingUser);
                }
            }
            throw new BusinessException("Invalid UserDetailsDTO provided!");
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("Unknown error while generate user info!");
        }
    }

}
