package application.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import application.entity.TestResultEntity;

public interface TestResultRepository extends JpaRepository<TestResultEntity, Long> {
    
}
