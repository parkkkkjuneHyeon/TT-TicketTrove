package com.trove.ticket_trove.dto.member.response;

import com.trove.ticket_trove.model.entity.member.MemberEntity;
import com.trove.ticket_trove.model.user.Role;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Member {

    private Long id;
    private String name;
    private String email;
    private String password;
    private String gender;
    private Integer age;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public static Member from(MemberEntity memberEntity) {
        return Member.builder()
                .id(memberEntity.getId())
                .name(memberEntity.getName())
                .email(memberEntity.getEmail())
                .password(memberEntity.getPassword())
                .gender(memberEntity.getGender())
                .age(memberEntity.getAge())
                .role(memberEntity.getRole())
                .createdAt(memberEntity.getCreatedAt())
                .updatedAt(memberEntity.getUpdatedAt())
                .deletedAt(memberEntity.getDeletedAt())
                .build();
    }
}
