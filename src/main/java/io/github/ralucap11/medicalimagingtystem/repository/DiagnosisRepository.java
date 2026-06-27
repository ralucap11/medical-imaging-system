package io.github.ralucap11.medicalimagingtystem.repository;


import io.github.ralucap11.medicalimagingtystem.entity.Diagnosis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {
    Optional<Diagnosis> findByPatientId(Long patientId);
}
