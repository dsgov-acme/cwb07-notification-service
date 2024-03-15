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
 * Represents a message to be sent to a user not necessarily registered in the system.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
@Entity
@Table(name = "direct_message")
@DiscriminatorValue("DIRECT_MESSAGE")
public class DirectMessage extends Message {

    private static final long serialVersionUID = 8675309867530986753L;

    @Column(name = "communication_method")
    private String communicationMethod;

    @Column(name = "destination")
    private String destination;

    @PrePersist
    protected void onCreate() {
        this.type = "DIRECT_MESSAGE";
    }
}
