package com.elearning.service.repositories;

import com.elearning.service.entities.ReviewHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewHistoryRepository extends JpaRepository<ReviewHistory, Long> {
    
    List<ReviewHistory> findByUser_Id(Long userId);
    
    List<ReviewHistory> findByUser_IdAndReviewDate(Long userId, LocalDate reviewDate);
    
    @Query("SELECT DISTINCT rh.reviewDate FROM ReviewHistory rh WHERE rh.user.id = :userId ORDER BY rh.reviewDate DESC")
    List<LocalDate> findDistinctReviewDatesByUserId(@Param("userId") Long userId);
    
    @Query("SELECT DISTINCT rh.reviewDate FROM ReviewHistory rh WHERE rh.user.id = :userId AND rh.reviewDate >= :startDate AND rh.reviewDate <= :endDate ORDER BY rh.reviewDate")
    List<LocalDate> findDistinctReviewDatesByUserIdInMonth(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(rh) FROM ReviewHistory rh WHERE rh.user.id = :userId AND rh.reviewDate = :date")
    Long countByUserIdAndReviewDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT CASE WHEN COUNT(rh) > 0 THEN true ELSE false END FROM ReviewHistory rh WHERE rh.user.id = :userId AND rh.reviewDate = :date")
    boolean existsByUserIdAndReviewDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    long countByUser_Id(Long userId);
    
    @Query("SELECT AVG(rh.quality) FROM ReviewHistory rh WHERE rh.user.id = :userId")
    Optional<Double> getAverageQuality(@Param("userId") Long userId);
    
    @Query("SELECT (CAST(COUNT(CASE WHEN rh.isSuccessful = true THEN 1 END) AS double) / CAST(COUNT(rh) AS double)) * 100.0 FROM ReviewHistory rh WHERE rh.user.id = :userId AND rh.reviewDate >= :sinceDate")
    Optional<Double> getRecentAccuracy(@Param("userId") Long userId, @Param("sinceDate") LocalDate sinceDate);
    
    // Delete methods for cascade deletion
    void deleteByCardIdIn(List<Long> cardIds);
}
