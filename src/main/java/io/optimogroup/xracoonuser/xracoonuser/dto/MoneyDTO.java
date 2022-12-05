package io.optimogroup.xracoonuser.xracoonuser.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MoneyDTO {

    private Double amountOfGel;

}
