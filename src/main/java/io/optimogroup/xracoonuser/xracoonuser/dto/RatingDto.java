package io.optimogroup.xracoonuser.xracoonuser.dto;


import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@RedisHash("rating")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RatingDto implements Serializable {

    @Id
    private Long id;
    private String amount;

}
