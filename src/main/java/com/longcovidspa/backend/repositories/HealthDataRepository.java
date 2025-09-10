package com.longcovidspa.backend.repositories;

import com.longcovidspa.backend.model.HealthData;
import com.longcovidspa.backend.model.HeartRateDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import java.util.Date;
import java.util.List;

@Repository
public interface HealthDataRepository extends JpaRepository<HealthData,Long> {
    @Query("SELECT to_char(hd.receivedDate, 'YYYY-MM-DD HH24') as period, AVG(hd.heartRateVariability) as averageRate " +
            "FROM HealthData hd " +
            "GROUP BY to_char(hd.receivedDate, 'YYYY-MM-DD HH24')")
    List<Object[]> findHourlyAverage(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query("SELECT to_char(hd.receivedDate, 'YYYY-MM-DD') as period, AVG(hd.heartRateVariability) as averageRate " +
            "FROM HealthData hd WHERE hd.receivedDate BETWEEN :startDate AND :endDate " +
            "GROUP BY to_char(hd.receivedDate, 'YYYY-MM-DD')")
    List<HeartRateDTO> findDailyAverage(@Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

    @Query("SELECT to_char(hd.receivedDate, 'YYYY-MM') as period, AVG(hd.heartRateVariability) as averageRate " +
            "FROM HealthData hd WHERE hd.receivedDate BETWEEN :startDate AND :endDate " +
            "GROUP BY to_char(hd.receivedDate, 'YYYY-MM')")
    List<HeartRateDTO> findMonthlyAverage(@Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

    @Query("SELECT MAX(h.receivedDate) FROM HealthData h WHERE h.user.id = :patientId")
    Optional<LocalDateTime> findLatestSyncDateByPatientId(@Param("patientId") Long patientId);

    @Query("""
    select h from HealthData h
    where h.user.username = :username
    order by h.receivedDate desc
  """)
    List<HealthData> findLatestRow(@Param("username") String username,
                                   org.springframework.data.domain.Pageable pr);

    default Optional<HealthData> findLatest(String username) {
        var page = org.springframework.data.domain.PageRequest.of(0,1);
        var list = findLatestRow(username, page);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    Optional<HealthData> findTopByUser_IdOrderByReceivedDateDesc(Long patientId);

    // ✅ RANGE (optionally bounded) by patientId, ascending time
    @Query("""
           select hd from HealthData hd
           where hd.user.id = :patientId
             and (:start is null or hd.receivedDate >= :start)
             and (:end   is null or hd.receivedDate <= :end)
           order by hd.receivedDate asc
           """)
    List<HealthData> findByPatientAndRange(@Param("patientId") Long patientId,
                                           @Param("start") Timestamp start,
                                           @Param("end")   Timestamp end);

    // (Optional) examples if you need per-patient daily averages later
    @Query("""
           select to_char(hd.receivedDate, 'YYYY-MM-DD') as period,
                  avg(hd.heartRateVariability) as avgHrv
           from HealthData hd
           where hd.user.id = :patientId
             and hd.receivedDate between :start and :end
           group by to_char(hd.receivedDate, 'YYYY-MM-DD')
           order by period
           """)
    List<Object[]> findDailyHrvAvgByPatient(@Param("patientId") Long patientId,
                                            @Param("start") Timestamp start,
                                            @Param("end")   Timestamp end);

    // repositories/HealthDataRepository.java
    @Query("SELECT h FROM HealthData h " +
            "WHERE h.user.username = :username " +
            "AND h.receivedDate BETWEEN :start AND :end " +
            "ORDER BY h.receivedDate ASC")
    List<HealthData> findWindow(@Param("username") String username,
                                @Param("start") java.sql.Timestamp start,
                                @Param("end")   java.sql.Timestamp end);
    List<HealthData> findByUser_UsernameAndReceivedDateBetweenOrderByReceivedDate(
            String username, Timestamp start, Timestamp end
    );
}
