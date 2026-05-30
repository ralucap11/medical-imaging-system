package io.github.ralucap11.medicalimagingtystem.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "diagnoses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Diagnosis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDate date;

    @OneToOne
    @JoinColumn(name = "xray_id", unique = true)
    private Xray xray;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;
}