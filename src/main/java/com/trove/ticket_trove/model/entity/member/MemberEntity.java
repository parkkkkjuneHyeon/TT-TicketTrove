package com.trove.ticket_trove.model.entity.member;

import com.trove.ticket_trove.dto.member.request.Member;
import com.trove.ticket_trove.model.user.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity
@Table(
        name = "member",
        indexes = {
                @Index(name = "idx_member_email", columnList = "email", unique = true)
        }
)
@SQLDelete(sql = "UPDATE member " +
        "SET deleted_at = CURRENT_TIMESTAMP " +
        "WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String password;
    private String gender;
    private Integer age;
    private Role role;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;


    @PrePersist
    private void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static MemberEntity from(
            String name, String email,
            String password, String gender,
            Integer age, Role role) {
        return MemberEntity.builder()
                .name(name)
                .email(email)
                .password(password)
                .gender(gender)
                .age(age)
                .role(role)
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if(this.role.equals(Role.ADMIN)) {
            return List.of(
                    new SimpleGrantedAuthority(Role.ADMIN.name()),
                    new SimpleGrantedAuthority("ROLE_"+Role.ADMIN.name()),
                    new SimpleGrantedAuthority(Role.USER.name()),
                    new SimpleGrantedAuthority("ROLE_"+Role.USER.name()));
        }else {
            return List.of(
                    new SimpleGrantedAuthority(Role.USER.name()),
                    new SimpleGrantedAuthority("ROLE_"+Role.USER.name()));
        }
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public static MemberEntity from(Member member) {
        return MemberEntity.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .password(member.getPassword())
                .gender(member.getGender())
                .age(member.getAge())
                .role(member.getRole())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .deletedAt(member.getDeletedAt())
                .build();
    }
}
