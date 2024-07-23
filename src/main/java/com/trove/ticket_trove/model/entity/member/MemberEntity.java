package com.trove.ticket_trove.model.entity.member;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

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
public class MemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String password;
    private String gender;
    private Integer age;
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @LastModifiedDate
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
            Integer age) {
        return MemberEntity.builder()
                .name(name)
                .email(email)
                .password(password)
                .gender(gender)
                .age(age)
                .build();
    }
}
