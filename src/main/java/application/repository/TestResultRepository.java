package application.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import application.entity.TestResultEntity;

public interface TestResultRepository extends JpaRepository<TestResultEntity, Long> {
    Optional<TestResultEntity> findTopByOrderByIdDesc();
}
