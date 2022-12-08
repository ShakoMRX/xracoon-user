package io.optimogroup.xracoonuser.xracoonuser.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContactDTO {
    private Long id;
    private Long regId;
    private String prefix;
    private String value;
    private String info;
    private String contactInfoType;
    private String contactInfoTag;
    private String serviceType;
}
