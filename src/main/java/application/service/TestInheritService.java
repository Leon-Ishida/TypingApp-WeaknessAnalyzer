package application.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import application.dto.TestInheritResponse;
import application.entity.TestResultEntity;
import application.repository.TestResultRepository;
import application.security.CustomUserDetails;
import jakarta.transaction.Transactional;

@Service
public class TestInheritService {
    private final TestResultRepository repository;

    public TestInheritService(TestResultRepository repository) {
        this.repository = repository;
    }

    public List<TestInheritResponse> makeInheritList(Set<Long> sessionCandidateIds) {
        List<TestResultEntity> ableInheritEntities = searchInheritEntities(sessionCandidateIds);
        List<TestInheritResponse> inheritResultsList = new ArrayList<>();
        for (TestResultEntity entity : ableInheritEntities) {
            inheritResultsList.add(new TestInheritResponse(entity.getId(), entity.getTimestamp(), entity.getWpm(), entity.getAccuracy()));
        }
        return inheritResultsList;
    }

    @Transactional
    public int tieResultsWithUser(Set<Long> selectedIds, Set<Long> sessionCandidateIds) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails customUserDetails) {
            String userId = customUserDetails.getUserId().toString();

            Set<Long> targetIds = new HashSet<>(selectedIds);
            targetIds.retainAll(sessionCandidateIds);

            if (targetIds.isEmpty()) {
                return 0;
            }

            LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30);
            return repository.claimGuestResults(userId, targetIds, cutoff);
        }

        return -1;
    }

    private List<TestResultEntity> searchInheritEntities(Set<Long> sessionCandidateIds) {
        List<TestResultEntity> ableInheritEntities = new ArrayList<>();

        List<TestResultEntity> unLoginedResultEntities = repository.findAllById(sessionCandidateIds);

        for (TestResultEntity checkingEntity : unLoginedResultEntities) {
            if (isAbleInheritEntity(checkingEntity, sessionCandidateIds)) {
                ableInheritEntities.add(checkingEntity);
            }
        }

        return ableInheritEntities;
    }

    private boolean isAbleInheritEntity(TestResultEntity entity, Set<Long> sessionCandidateIds) {
        return sessionCandidateIds.contains(entity.getId()) && 
            entity.getUserId() == null && 
            entity.getTimestamp().isAfter(LocalDateTime.now().minusMinutes(30));
    }
}
