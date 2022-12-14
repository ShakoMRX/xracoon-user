package io.optimogroup.xracoonuser.xracoonuser.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "USERS", schema = "USER_MANAGER")
@Getter
@Setter
@SequenceGenerator(name = "userIdSeq", schema = "USER_MANAGER", sequenceName = "SEQ_USERS", allocationSize = 1)
public class User {

    @Id
    @GeneratedValue(generator = "userIdSeq", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "USER_UUID")
    private String userUuid;

    @Column(name = "PARTY_ID")
    private Long partyId;

    @Column(name = "ATTACHMENT_ID")
    private Long attachmentId;
}
