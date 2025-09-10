package com.longcovidspa.backend.repositories;

import com.longcovidspa.backend.model.MedicApplication;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface MedicApplicationRepo extends JpaRepository<MedicApplication, Long> {
    Optional<MedicApplication> findTopByUserIdOrderBySubmittedAtDesc(Long userId);
    List<MedicApplication> findByStatus(String status);

    @Query("select m from MedicApplication m where m.emailToken = :t")
    Optional<MedicApplication> findByEmailToken(@Param("t") String token);
    List<MedicApplication> findByStatusOrderBySubmittedAtDesc(String status); // PENDING, APPROVED, REJECTED
    boolean existsByUserIdAndStatus(Long userId, String status); // prevent multiple PENDING apps

}
