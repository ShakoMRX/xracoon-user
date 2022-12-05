package io.optimogroup.xracoonuser.xracoonuser.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@RequiredArgsConstructor
public class AccountDetail {
    private MoneyDTO amountOfGel;
    private PointsDTO amountOfPoint;
    private AmountOfSpentPoints amountOfSpentPoints;
    private List<PointsTransactionDTO> pointsTransactions;
}
