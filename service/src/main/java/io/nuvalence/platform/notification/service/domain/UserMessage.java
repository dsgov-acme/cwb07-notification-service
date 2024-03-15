package io.nuvalence.platform.notification.service.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a message to be sent to a user registered in the system.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
@Entity
@Table(name = "user_message")
@DiscriminatorValue("USER_MESSAGE")
public class UserMessage extends Message {

    private static final long serialVersionUID = 1234567890123456789L;

    @Column(name = "user_id", length = 64)
    private String userId;

    @PrePersist
    protected void onCreate() {
        this.type = "USER_MESSAGE";
    }
}
