package io.optimogroup.xracoonuser.xracoonuser.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
@Builder
public class AmountOfSpentPoints {

    private  Double amountOfSpentPoints;
}
