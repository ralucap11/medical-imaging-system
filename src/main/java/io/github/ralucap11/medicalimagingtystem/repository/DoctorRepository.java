package io.github.ralucap11.medicalimagingtystem.repository;

import io.github.ralucap11.medicalimagingtystem.entity.Doctor;
import io.github.ralucap11.medicalimagingtystem.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long>
{

}

