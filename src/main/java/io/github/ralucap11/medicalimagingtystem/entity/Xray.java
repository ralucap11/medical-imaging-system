package io.github.ralucap11.medicalimagingtystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

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

    @Column(name = "xray_name", nullable = false)
    private String xrayName;

    @Column(name = "ai_classification")
    private String aiClassification;

    @Column(name = "cobb_angle")
    private Double cobbAngle;

    @Column(name = "ai_confidence")
    private Double aiConfidence;

    @Column(name = "cobb_visualization", columnDefinition = "TEXT")
    private String cobbVisualization;

    @Column(name = "description")
    private String description;

    @Column(name = "date_uploaded", nullable = false)
    private LocalDate dateUploaded;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="patient_id", nullable = false)
    private Patient patient;

    @OneToOne(mappedBy = "xray", cascade = CascadeType.ALL)
    private Diagnosis diagnosis;
}
