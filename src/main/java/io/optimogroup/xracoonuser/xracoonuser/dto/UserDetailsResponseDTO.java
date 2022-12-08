package io.optimogroup.xracoonuser.xracoonuser.dto;

import io.optimogroup.xracoon.shared.registry.client.models.ContactInfoDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserDetailsResponseDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String gender;
    private String personalId;
    private Long legalAddressId;
    private Long physicalAddressId;
    private List<ContactDTO> contactInfos;
    private String objectType;
    private String registryType;

}
