package io.optimogroup.xracoonuser.xracoonuser.dto;

import lombok.*;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class VoucherInfoForUserDTO {
    private String imageUrl;
    private Timestamp transactionDate;
    private Double voucherPrice;
    private Double earnedPoints;
    private Long voucherId;
    private String providerTitle;
    private String description;
}
