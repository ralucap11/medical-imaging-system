package io.github.ralucap11.medicalimagingtystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Patient
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cnp", nullable = false, unique = true)
    private String cnp;

    @Column(name = "weight", nullable = false)
    private Double weight;

    @Column(name = "age", nullable = false)
    private Integer age;

    @Column(name = "gender", nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "height", nullable = false)
    private Double height;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;


    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    private List<Xray> xrays = new ArrayList<>();

}
