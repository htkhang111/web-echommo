package com.echommo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "pvp_match")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PvpMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long matchId;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "turn_count")
    private int turnCount;

    @Column(name = "last_log", columnDefinition = "TEXT")
    private String lastLog;

    @Column(name = "winner_id")
    private Long winnerId;

    // --- CÁC TRƯỜNG THÔNG TIN NGƯỜI CHƠI ---
    @ManyToOne
    @JoinColumn(name = "player1_id")
    private Character player1;

    @ManyToOne
    @JoinColumn(name = "player2_id")
    private Character player2;

    @Column(name = "p1_current_hp")
    private Integer p1CurrentHp;

    @Column(name = "p2_current_hp")
    private Integer p2CurrentHp;

    @Column(name = "p1_move")
    private String p1Move;

    @Column(name = "p2_move")
    private String p2Move;

    // --- [MỚI THÊM] LƯU LẠI NƯỚC ĐI CỦA HIỆP TRƯỚC ---
    // Giúp Frontend hiển thị lại kết quả dù turn đã reset
    @Column(name = "last_p1_move")
    private String lastP1Move;

    @Column(name = "last_p2_move")
    private String lastP2Move;
    // -------------------------------------------------

    @Column(name = "p1_accepted")
    private boolean p1Accepted;

    @Column(name = "p2_accepted")
    private boolean p2Accepted;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PvpChat> chats = new ArrayList<>();
}