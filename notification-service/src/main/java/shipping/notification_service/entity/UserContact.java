package shipping.notification_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_contact")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserContact {
    @Id
    @Column(name = "user_id") private Long userId;
    @Column(name = "name", length = 50) private String name;
    @Column(name = "email", nullable = false, length = 100) private String email;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
    @Column(name = "phone", length = 15) private String phone;
}

