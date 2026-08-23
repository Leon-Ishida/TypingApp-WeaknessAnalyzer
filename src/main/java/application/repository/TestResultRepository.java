package application.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import application.entity.TestResultEntity;

import java.time.LocalDateTime;


public interface TestResultRepository extends JpaRepository<TestResultEntity, Long> {
    Optional<TestResultEntity> findTopByOrderByIdDesc();

    List<TestResultEntity> findAllByOrderByTimestamp();

    List<TestResultEntity> findByUserIdOrderByTimestamp(String userId);

    List<TestResultEntity> findByUserIdAndTimestampGreaterThanEqualAndTimestampLessThanOrderByTimestamp(String userId, LocalDateTime startDateTime, LocalDateTime lastDateTime);

    Optional<TestResultEntity> findTopBySessionIdOrderByTimestampDesc(String sessionId);

    @Transactional
    void deleteByUserIdIsNullAndTimestampBefore(LocalDateTime thirtyMinutestAgo);
}
