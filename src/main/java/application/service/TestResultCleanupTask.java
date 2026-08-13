package application.service;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import application.repository.TestResultRepository;

@Service
public class TestResultCleanupTask {
    private final TestResultRepository repository;

    public TestResultCleanupTask(TestResultRepository repository) {
        this.repository = repository;
    }

    @Scheduled(fixedRate = 600000)
    void deleteGuestResult() {
        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);
        repository.deleteByUserIdIsNullAndTimestampBefore(thirtyMinutesAgo);;
    }
}
