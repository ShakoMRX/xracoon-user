package io.optimogroup.xracoonuser.xracoonuser.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.optimogroup.xracoonuser.xracoonuser.Utils.RequestUtils;
import io.optimogroup.xracoonuser.xracoonuser.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequiredArgsConstructor
@RequestMapping("municipalities")
@Slf4j
public class MunicipalityController {

    @Value("${app.registry.host.for.user.details}")
    private String hostForRegistry;

    @Value("${app.registry.uri}")
    private String uriForRegistry;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<?> getAllMunicipalities() {
        try {
            UriComponentsBuilder accountingUriBuilder = UriComponentsBuilder
                    .fromHttpUrl(hostForRegistry + "/" + uriForRegistry);
            var municipalities = RequestUtils.ServiceCall(
                    log,
                    restTemplate,
                    objectMapper,
                    "get user pointTransactions",
                    accountingUriBuilder
                            .encode()
                            .toUriString() + "/addresses/municipality",
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<JsonNode>() {
                    });
            if (municipalities.getBody() != null)
                return new ResponseEntity<>(municipalities.getBody(), HttpStatus.OK);
            throw new BusinessException("Unknown error while retrieve user points transactions! with partyId %s");
        } catch (Exception e) {
            e.printStackTrace();
            log.info("Unknown error while retrieve user points transactions! with partyId %s");
            throw new BusinessException("Unknown error while retrieve user points transactions! with partyId %s");
        }
    }
}
