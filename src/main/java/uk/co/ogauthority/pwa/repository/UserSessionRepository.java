package uk.co.ogauthority.pwa.repository;

import java.util.Optional;
import org.springframework.data.repository.Repository;
import uk.co.ogauthority.pwa.model.entity.UserSession;

public interface UserSessionRepository extends Repository<UserSession, String> {
  Optional<UserSession> findById(String id);
}