package io.github.ralucap11.medicalimagingtystem.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "xrays")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Xray
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "format", nullable = false)
    private String format;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="patient_id", nullable = false)
    private Patient patient;
}
