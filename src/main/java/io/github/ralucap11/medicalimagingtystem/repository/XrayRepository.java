package io.github.ralucap11.medicalimagingtystem.repository;

import io.github.ralucap11.medicalimagingtystem.entity.Xray;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface XrayRepository extends JpaRepository<Xray, Long>
{
    boolean existsById(Long patientId);
}
