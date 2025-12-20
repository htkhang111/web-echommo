package com.echommo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "pvp_matches")
@Data
public class PvpMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long matchId;

    @ManyToOne
    @JoinColumn(name = "player1_id")
    private Character player1;

    @ManyToOne
    @JoinColumn(name = "player2_id")
    private Character player2;

    private String status;

    // [FIX] Đổi từ Long thành Integer cho khớp với charId
    private Integer winnerId;

    private boolean p1Accepted;
    private boolean p2Accepted;

    private LocalDateTime createdAt;

    private Integer turnCount;

    @Column(name = "rps_p1_move")
    private String p1Move;

    @Column(name = "rps_p2_move")
    private String p2Move;

    @Column(name = "p1_current_hp")
    private Integer p1CurrentHp;

    @Column(name = "p2_current_hp")
    private Integer p2CurrentHp;

    @Column(columnDefinition = "TEXT")
    private String lastLog;
}