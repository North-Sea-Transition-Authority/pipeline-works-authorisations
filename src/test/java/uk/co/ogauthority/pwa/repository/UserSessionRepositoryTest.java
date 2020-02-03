package uk.co.ogauthority.pwa.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import javax.persistence.PersistenceUnitUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.model.entity.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.model.entity.UserSession;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create"
})
public class UserSessionRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private UserSessionRepository userSessionRepository;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  private PersistenceUnitUtil persistenceUnitUtil;

  private static boolean functionCreated = false;

  @Before
  public void setup() {
    if (!functionCreated) {
      jdbcTemplate.execute(
          "CREATE ALIAS USER_SYSTEM_PRIVILEGES FOR \"uk.co.ogauthority.pwa.repository.UserSessionRepositoryTest.testPrivilegeFunction\"");
      functionCreated = true;
    }

    var session = new UserSession("ID1");
    session = entityManager.getEntityManager().merge(session);

    var userAccount = new AuthenticatedUserAccount("UA1");
    userAccount = entityManager.getEntityManager().merge(userAccount);

    session.setAuthenticatedUserAccount(userAccount);
    entityManager.persist(session);

    entityManager.flush();
    entityManager.clear();

    persistenceUnitUtil = entityManager.getEntityManager().getEntityManagerFactory().getPersistenceUnitUtil();
  }

  public static String testPrivilegeFunction(String userId) {
    return "ROLE1,ROLE2";
  }

  @Test
  public void testFindById() {
    Optional<UserSession> repoSession = userSessionRepository.findById("ID1");
    assertThat(repoSession).isPresent();

    //Default behaviour should be to lazy load the AuthenticatedUserAccount
    assertThat(persistenceUnitUtil.isLoaded(repoSession.get().getAuthenticatedUserAccount())).isFalse();
  }

  @Test
  public void testFindAndLoadUserAccountById() {
    Optional<UserSession> repoSession = userSessionRepository.findAndLoadUserAccountById("ID1");
    assertThat(repoSession).isPresent();

    //AuthenticatedUserAccount should be eagerly fetched in this case
    assertThat(persistenceUnitUtil.isLoaded(repoSession.get().getAuthenticatedUserAccount())).isTrue();
    assertThat(repoSession.get().getAuthenticatedUserAccount().getSystemPrivileges()).containsExactlyInAnyOrder("ROLE1", "ROLE2");
  }

}