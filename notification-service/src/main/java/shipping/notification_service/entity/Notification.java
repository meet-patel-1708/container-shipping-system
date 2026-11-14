package shipping.notification_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @Column(name = "user_id",nullable = false)
    private Long userId;

    @Column(name = "booking_id",nullable = false)
    private Long bookingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel",nullable = false,length = 20)
    private Channel channel;

    @Column(name = "title",nullable = false,length = 100)
    private String title;

    @Column(name = "message",nullable = false,length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false,length = 20)
    private NotificationStatus status;
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void create() {
        this.createdAt = LocalDateTime.now();
    }
}
