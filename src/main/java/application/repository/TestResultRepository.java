package application.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import application.entity.TestResultEntity;
import java.time.LocalDateTime;


public interface TestResultRepository extends JpaRepository<TestResultEntity, Long> {
    Optional<TestResultEntity> findTopByOrderByIdDesc();

    List<TestResultEntity> findByTimestampGreaterThanEqualAndSmallerThanOrderByTimestamp(LocalDateTime startDateTime, LocalDateTime lastDateTime);
}
