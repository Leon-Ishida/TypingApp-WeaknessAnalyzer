package application.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import application.dto.PeriodAnalyzeRequest;
import application.dto.PeriodAnalyzeResponse;
import application.entity.TestResultEntity;
import application.model.TestResult;
import application.repository.TestResultRepository;
import application.security.CustomUserDetails;
import jakarta.servlet.http.HttpSession;

@ExtendWith(MockitoExtension.class)
public class AggregateServiceTest {

    @Mock 
    private TestResultRepository repository;

    @Mock
    private Authentication authentication;

    @Mock 
    private HttpSession session;

    @Mock 
    private CustomUserDetails userDetails;

    @InjectMocks 
    private AggregateService aggregateService;

    private final UUID TESTUSERID = UUID.randomUUID();

    @BeforeEach 
    void setup() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUserId()).thenReturn(TESTUSERID);

        //when(session.getId()).thenReturn("test-session-id");
    }
    
    // 履歴件数が0, 1, 30, 31件のテスト
    @Test 
    @DisplayName("履歴が0件の時、集計結果も0件であること")
    void testZeroHistory() {
        List<TestResultEntity> selectedResult = new ArrayList<>();
        
        setupRepository(selectedResult);
        
        PeriodAnalyzeResponse response = aggregateService.makeAggregatedInfo(
            new PeriodAnalyzeRequest(
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30)
            ),
            authentication,
            session
        );

        assertTrue(response.scoreTrends().isEmpty(), "グラフ描写用統計リストが空であること");
    }

    @Test 
    @DisplayName("履歴が1件の時、集計結果も1件であること")
    void testOneHistory() {
        List<TestResultEntity> selectedResult = new ArrayList<>(List.of(
            TestResultEntity.fromRecord(
                TESTUSERID.toString(), 
                session.getId(), 
                new TestResult(
                    LocalDateTime.of(2026, 9, 5, 0, 0),
                    new LinkedHashMap<>(),
                    2.0,
                    2.0
                )
            )
        ));

        setupRepository(selectedResult);
        
        PeriodAnalyzeResponse response = aggregateService.makeAggregatedInfo(
            new PeriodAnalyzeRequest(
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30)
            ),
            authentication,
            session
        );

        assertTrue(response.scoreTrends().size() == 1, "グラフ描写用統計リストが1件であること");
    }

    @Test 
    @DisplayName("履歴が30件の時、集計結果も30件であること")
    void testThirtyHistory() {
        List<TestResultEntity> selectedResult = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            selectedResult.add(TestResultEntity.fromRecord(
                TESTUSERID.toString(), 
                session.getId(), 
                new TestResult(
                    LocalDateTime.of(2026, 9, 5, 0, 0),
                    new LinkedHashMap<>(),
                    2.0,
                    2.0
                )
            ));
        }

        setupRepository(selectedResult);
        
        PeriodAnalyzeResponse response = aggregateService.makeAggregatedInfo(
            new PeriodAnalyzeRequest(
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30)
            ),
            authentication,
            session
        );

        assertTrue(response.scoreTrends().size() == 30, "グラフ描写用統計リストが30件であること");        
    }

    @Test 
    @DisplayName("履歴が31件以上の時、同じ日の統計で平均がとられること")
    void testThirtyOneHistory() {
        List<TestResultEntity> selectedResult = new ArrayList<>();
        for (int i = 0; i < 31; i++) {
            selectedResult.add(TestResultEntity.fromRecord(
                TESTUSERID.toString(), 
                session.getId(), 
                new TestResult(
                    LocalDateTime.of(2026, 9, 5, 0, 0),
                    new LinkedHashMap<>(),
                    2.0,
                    2.0
                )
            ));
        }

        setupRepository(selectedResult);
        
        PeriodAnalyzeResponse response = aggregateService.makeAggregatedInfo(
            new PeriodAnalyzeRequest(
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30)
            ),
            authentication,
            session
        );

        assertTrue(response.scoreTrends().size() == 1, "グラフ描写用統計リストが1件であること"); 
    }

    private void setupRepository(List<TestResultEntity> selectedResult) {
        when(repository.findByUserIdAndTimestampGreaterThanEqualAndTimestampLessThanOrderByTimestamp(
            eq(TESTUSERID.toString()),
            any(LocalDateTime.class),
            any(LocalDateTime.class)
        )).thenReturn(selectedResult);
    }
}
