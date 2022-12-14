package io.optimogroup.xracoonuser.xracoonuser.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserAddressDTO {

    private AddressDTO physicalAddress;
    private AddressDTO legalAddress;
}
