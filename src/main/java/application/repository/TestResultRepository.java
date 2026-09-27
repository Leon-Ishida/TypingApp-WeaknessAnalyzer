package application.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import application.entity.TestResultEntity;

import java.time.LocalDateTime;


public interface TestResultRepository extends JpaRepository<TestResultEntity, Long> {
    Optional<TestResultEntity> findTopByOrderByIdDesc();

    List<TestResultEntity> findAllByOrderByTimestamp();

    List<TestResultEntity> findByUserIdOrderByTimestamp(String userId);

    List<TestResultEntity> findByUserIdAndTimestampGreaterThanEqualAndTimestampLessThanOrderByTimestamp(String userId, LocalDateTime startDateTime, LocalDateTime lastDateTime);

    Optional<TestResultEntity> findTopBySessionIdOrderByTimestampDesc(String sessionId);

    boolean existsByIdInAndUserIdIsNullAndTimestampGreaterThanEqual(Collection<Long> ids, LocalDateTime cutoff);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE TestResultEntity result
            SET result.userId = :userId
            WHERE result.id IN :ids
            AND result.userId IS NULL
            AND result.timestamp >= :cutoff
            """)
    int claimGuestResults(
        @Param("userId") String userId,
        @Param("ids") Collection<Long> ids,
        @Param("cutoff") LocalDateTime cutoff
    );

    @Transactional
    void deleteByUserIdIsNullAndTimestampBefore(LocalDateTime thirtyMinutestAgo);
}
