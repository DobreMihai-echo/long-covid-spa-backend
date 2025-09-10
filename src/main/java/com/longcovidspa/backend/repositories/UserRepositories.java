package com.longcovidspa.backend.repositories;

import com.longcovidspa.backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepositories extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    @Query(
            value = "select p from User d join d.assignedPatients p where d.id = :doctorId",
            countQuery = "select count(p) from User d join d.assignedPatients p where d.id = :doctorId"
    )
    Page<User> findPatientsOfDoctor(@Param("doctorId") Long doctorId, Pageable pageable);

    // Find MEDIC by id (guard rails)
    @Query("select case when count(u)>0 then true else false end " +
            "from User u join u.roles r where u.id=:userId and r.name='ROLE_MEDIC'")
    boolean isMedic(@Param("userId") Long userId);

    // Search assignable patients (ROLE_USER), optional text filter
    @Query(
            value = """
      select u from User u join u.roles r
      where r.name='ROLE_USER' and
            (:q is null or :q='' or
             lower(u.email) like lower(concat('%', :q, '%')) or
             lower(u.firstName) like lower(concat('%', :q, '%')) or
             lower(u.lastName) like lower(concat('%', :q, '%')))
      """,
            countQuery = """
      select count(u) from User u join u.roles r
      where r.name='ROLE_USER' and
            (:q is null or :q='' or
             lower(u.email) like lower(concat('%', :q, '%')) or
             lower(u.firstName) like lower(concat('%', :q, '%')) or
             lower(u.lastName) like lower(concat('%', :q, '%')))
      """
    )
    Page<User> searchPatients(@Param("q") String q, Pageable pageable);
}
