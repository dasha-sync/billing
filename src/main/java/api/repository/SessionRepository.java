package api.repository;

import api.model.Session;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

  Optional<Session> findBySessionId(String sessionId);

  @Modifying
  @Query("DELETE FROM Session s WHERE s.expiresAt < :now")
  void deleteExpiredSessions(@Param("now") LocalDateTime now);

  @Modifying
  @Query("DELETE FROM Session s WHERE s.sessionId = :sessionId")
  void deleteBySessionId(@Param("sessionId") String sessionId);
}
