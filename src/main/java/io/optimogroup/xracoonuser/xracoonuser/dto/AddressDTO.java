package io.optimogroup.xracoonuser.xracoonuser.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {
    private MunicipalityDTO municipality;
    private String address;
}
