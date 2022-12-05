package io.optimogroup.xracoonuser.xracoonuser.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@RequiredArgsConstructor
public class PointsTransactionDTO {

    private Long typeId;
    private Long contractId;
    private Long partyId;
    private Double genericTransactionValue;
    private String description;
    private Long genericTransactionStatus;
    private Timestamp transactionDate;

}
