package com.back.snobs.domain.snob.refreshToken;

import com.back.snobs.domain.BaseTimeEntity;
import com.back.snobs.domain.snob.Snob;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class RefreshToken extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tokenIdx;

    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "snobIdx")
    private Snob snob;

    @Column
    private String token;

    @Builder
    public RefreshToken(Snob snob, String token) {
        this.snob = snob;
        this.token = token;
    }

    public void updateToken(String token) {
        this.token = token;
    }
}
