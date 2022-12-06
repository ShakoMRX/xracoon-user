package io.optimogroup.xracoonuser.xracoonuser.repository;

import io.optimogroup.xracoonuser.xracoonuser.dto.RatingDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RatingRepository {

    @Autowired
    private RedisTemplate redisTemplate;

    public void save(RatingDto ratingDto) {
        redisTemplate.opsForHash().put("rating", ratingDto.getId(), ratingDto);
    }

    public List<RatingDto> findAll(){
        return redisTemplate.opsForHash().values("rating");
    }
}
