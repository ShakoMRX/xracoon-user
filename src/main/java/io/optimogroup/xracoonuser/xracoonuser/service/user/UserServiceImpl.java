package io.optimogroup.xracoonuser.xracoonuser.service.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import io.optimogroup.xracoon.shared.models.BaseException;
//import io.optimogroup.xracoon.shared.s3.client.exception.BadRequestException;
import io.optimogroup.xracoonuser.xracoonuser.Utils.RequestUtils;
import io.optimogroup.xracoonuser.xracoonuser.dto.*;
import io.optimogroup.xracoonuser.xracoonuser.exception.BusinessException;
import io.optimogroup.xracoonuser.xracoonuser.model.Attachment;
import io.optimogroup.xracoonuser.xracoonuser.model.User;
import io.optimogroup.xracoonuser.xracoonuser.repository.AttachmentRepository;
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
import java.util.Optional;

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

    private final AttachmentRepository attachmentRepository;

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

    public User getByPartyId(Long partyId) {
        if (partyId == null) throw new BusinessException("Invalid partyId provided!");
        return userRepository.findByPartyId(partyId);
    }

    @Override
    public UserDTO getUserDetails() {
        UserDTO userDTO = new UserDTO();
        try {
            Long partyId = getUserDetailsFromRegistry(userDTO);
            User user = getByPartyId(partyId);
            if (user.getAttachmentId() != null) {
                Attachment attachment = attachmentRepository.findById(user.getAttachmentId())
                        .orElseThrow(() -> new NotFoundException("Attachments not found!"));
                userDTO.setAvatar(UserAvatarDTO.builder()
                        .path(attachment.getPath())
                        .code(attachment.getColorCode())
                        .build());
            } else {
                userDTO.setAvatar(UserAvatarDTO.builder()
                        .path("2")
                        .code("#F44336")
                        .build());
            }
            AccountDetail accountDetail = getAccountingDetails(partyId);
            userDTO.setAccountDetail(accountDetail);
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
        if (userId == null) throw new BusinessException("Invalid userId provided!");
        if (path.isEmpty() || code.isEmpty()) throw new BusinessException("Invalid parameters provided!");
        try {
            Attachment attachment = new Attachment();
            attachment.setPath(path);
            attachment.setColorCode(code);
            Attachment savedAtt = attachmentRepository.save(attachment);
            user.setAttachmentId(savedAtt.getId());
            userRepository.save(user);
            return UserAvatarDTO.builder()
                    .code(code)
                    .path(path)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            log.info("Unknown error while add avatar for user userId= %s ".formatted(userId));
            throw new BusinessException("Unknown error while add avatar for user userId= %s ".formatted(userId));
        }
    }

    @Override
    public void updateUser(Long id, UserDetailsDTO detailsDTO) {
        if (detailsDTO.getPersonalId().length() != 11)
            throw new BusinessException("Personal Id must be with 11 digits!");
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
                updateNewContacts(urlRegistry, partyId, id, newContracts);
                updateExistingContacts(urlRegistry, partyId, id, existingContacts);
                updateUserAddresses(urlRegistry, partyId, id, detailsDTO.getUserAddress());
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("error while update user with id %s !".formatted(id));
            throw new BusinessException("error while update user with id %s !".formatted(id));
        }
    }


    public void updateNewContacts(UriComponentsBuilder urlRegistry,
                                  long partyId,
                                  long id,
                                  List<ContactDTO> newContracts) {
        var nCRes = RequestUtils.ServiceCall(
                log,
                restTemplate,
                objectMapper,
                "get userDetails",
                urlRegistry
                        .encode()
                        .toUriString() + "/" + partyId + "/contact-info",
                HttpMethod.POST,
                new HttpEntity<>(newContracts),
                new ParameterizedTypeReference<JsonNode>() {
                });
        if (nCRes.getStatusCode().value() == 200)
            log.info("successfully created contacts for user id = %s ".formatted(id));
    }

    public void updateUserAddresses(UriComponentsBuilder urlRegistry,
                                    long partyId,
                                    long id,
                                    UserAddressDTO userAddress) {
        if (userAddress != null) {
            var adRes = RequestUtils.ServiceCall(
                    log,
                    restTemplate,
                    objectMapper,
                    "get userDetails",
                    urlRegistry
                            .encode()
                            .toUriString() + "/" + partyId + "/addresses",
                    HttpMethod.POST,
                    new HttpEntity<>(userAddress),
                    new ParameterizedTypeReference<JsonNode>() {
                    });
            if (adRes.getStatusCode().value() == 200)
                log.info("successfully updated addresses for user id = %s ".formatted(id));
        }
    }

    public void updateExistingContacts(UriComponentsBuilder urlRegistry,
                                       long partyId,
                                       long id,
                                       List<ContactDTO> existingContacts) {
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
            if (exCRes.getStatusCode().value() == 200)
                log.info("successfully updated contacts for user id = %s ".formatted(id));
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
                userDTO.setDetails(mapDetailsToResponseDto(userDetailsDTO, user.getId()));
                return userDetailsDTO.getId();
            }
            throw new BusinessException("Invalid user Provided!");
        } catch (Exception e) {
            e.printStackTrace();
            log.info("Error while retrieve UserDetails from registry!");
            throw new BusinessException(e.getMessage());
        }
    }

    public UserDetailsResponseDTO mapDetailsToResponseDto(UserDetailsDTO userDetailsDTO, long userId) {
        return UserDetailsResponseDTO
                .builder()
                .id(userId)
                .objectType(userDetailsDTO.getObjectType())
                .personalId(userDetailsDTO.getPersonalId())
                .legalAddressId(userDetailsDTO.getLegalAddressId())
                .gender(userDetailsDTO.getGender())
                .contactInfos(userDetailsDTO.getContactInfos())
                .firstName(userDetailsDTO.getFirstName())
                .lastName(userDetailsDTO.getLastName())
                .registryType(userDetailsDTO.getRegistryType())
                .physicalAddressId(userDetailsDTO.getPhysicalAddressId())
                .personDob(userDetailsDTO.getPersonDob())
                .physicalAddress(userDetailsDTO.getPhysicalAddress())
                .legalAddress(userDetailsDTO.getLegalAddress())
                .build();
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
                Long partyId = UserDetailsDTO.getId();
                user.setUserUuid(uuId);
                user.setPartyId(partyId);
                if (!userRepository.existsByPartyId(partyId)) {
                    user.setAttachmentId(1L); // default
                    return userRepository.save(user);
                } else {
                    User existingUser = userRepository.findByPartyId(partyId);
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

    @Override
    public UserAvatarDTO getUserAvatar(Long partyId) {
        try {
            if (partyId == null) throw new BusinessException("Invalid user Id provided from registry!");
            User user = userRepository.findByPartyId(partyId);
            if (user.getAttachmentId() == null)
                throw new BusinessException("User has not avatar , please configure avatar from userProfile");
            Optional<Attachment> avatarDTO = attachmentRepository.findById(user.getAttachmentId());
            if (avatarDTO.isPresent()) {
                return UserAvatarDTO.builder()
                        .path(avatarDTO.get().getPath())
                        .code(avatarDTO.get().getColorCode())
                        .build();
            } else
                throw new BusinessException("Unknown Error while retrieve user from registry!");
        } catch (Exception e) {
            e.printStackTrace();
            log.info("Unknown error while retrieve user id from registry!");
            throw new BusinessException("Unknown error while retrieve user id from registry!");
        }

    }

    @Override
    public List<VoucherInfoForUserDTO> getUserVouchers() {
        try {
            UriComponentsBuilder accountingUriBuilder = UriComponentsBuilder
                    .fromHttpUrl(accountingHost + "/" + accountingUri);
            var vouchers = RequestUtils.ServiceCall(
                    log,
                    restTemplate,
                    objectMapper,
                    "get user vouchers",
                    accountingUriBuilder
                            .encode()
                            .toUriString() + "/vouchers/get-user-vouchers/",
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<JsonNode>() {
                    });
            if (vouchers.getBody() != null)
                return objectMapper.readValue(vouchers.getBody().traverse(), new TypeReference<>() {
                });
            throw new BusinessException("Unknown error while retrieve user vouchers");
        } catch (Exception e) {
            e.printStackTrace();
            log.info("Unknown error while retrieve user vouchers!");
            throw new BusinessException("Unknown error while retrieve user vouchers");
        }
    }

}
