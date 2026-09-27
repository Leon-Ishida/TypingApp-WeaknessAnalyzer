package application.session;

import java.util.HashSet;
import java.util.Set;

public class GuestResultsSessionStore {
    public static Set<Long> getSessionCandidateIds(Object candidates) {
        Set<Long> candidateIds = new HashSet<>();
        if (candidates instanceof Set<?> rawList) {
            for (Object value : rawList) {
                if (value instanceof Long id) {
                    candidateIds.add(id);
                }
            }
        }

        return candidateIds;
    }
}
