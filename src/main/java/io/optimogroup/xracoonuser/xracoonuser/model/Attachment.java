package io.optimogroup.xracoonuser.xracoonuser.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "ATTACHMENTS", schema = "USER_MANAGER")
@SequenceGenerator(name = "attIdSeq", sequenceName = "SEQ_ATTACHMENTS", allocationSize = 1)
public class Attachment {
    @Id
    @GeneratedValue(generator = "attIdSeq", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "PATH")
    private String path;

    @Column(name = "COLOR_CODE")
    private String colorCode;
}
