package com.trove.ticket_trove.model.entity.member;

import com.trove.ticket_trove.model.user.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash("Member")
public class RedisHashMember {
    @Id
    private String email;
    private Long id;
    private String name;
    private String password;
    private String gender;
    private Integer age;
    private Role role;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public static RedisHashMember from(MemberEntity memberEntity) {
        return new RedisHashMember(
                memberEntity.getEmail(),
                memberEntity.getId(),
                memberEntity.getName(),
                memberEntity.getPassword(),
                memberEntity.getGender(),
                memberEntity.getAge(),
                memberEntity.getRole(),
                memberEntity.getCreatedAt(),
                memberEntity.getUpdatedAt(),
                memberEntity.getDeletedAt());
    }
}


