package io.optimogroup.xracoonuser.xracoonuser.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.optimogroup.xracoon.shared.registry.client.models.ContactInfoDto;
import io.optimogroup.xracoon.shared.registry.client.models.OrgLegalFormDto;
import io.optimogroup.xracoon.shared.registry.client.models.RegistryTypeDto;
import io.optimogroup.xracoon.shared.registry.client.models.TaxationPolicyDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDetailsDTO implements Serializable {

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
    private String personDob;
//    @JsonIgnore
    private String uuid;

}
