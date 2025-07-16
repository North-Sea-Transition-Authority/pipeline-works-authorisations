package uk.co.ogauthority.pwa.model.auditrevisions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.util.SecurityUtils;

@ExtendWith(MockitoExtension.class)
class AuditRevisionListenerTest {

  private final AuditRevisionListener auditRevisionListener = new AuditRevisionListener();

  @Test
  void newRevision_whenProxyUser_thenProxyWuaIdIsSet() {
    var auditRevision = new AuditRevision();
    auditRevision.setPersonId(null);
    auditRevision.setProxyWuaId(null);

    var person = new Person();
    person.setId(1);

    var authenticatedUserAccount = new AuthenticatedUserAccount();
    authenticatedUserAccount.setPerson(person);
    authenticatedUserAccount.setProxyUserWuaId(2);


    try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
      securityUtils.when(SecurityUtils::getAuthenticatedUserFromSecurityContext).thenReturn(
          Optional.of(authenticatedUserAccount));

      auditRevisionListener.newRevision(auditRevision);
    }

    assertThat(auditRevision.getPersonId()).isEqualTo(1);
    assertThat(auditRevision.getProxyWuaId()).isEqualTo(2);
  }

  @Test
  void newRevision_whenNotProxyUser_thenProxyWuaIdIsNull() {
    var auditRevision = new AuditRevision();
    auditRevision.setPersonId(0);
    auditRevision.setProxyWuaId(null);

    var person = new Person();
    person.setId(1);

    var authenticatedUserAccount = new AuthenticatedUserAccount();
    authenticatedUserAccount.setPerson(person);
    authenticatedUserAccount.setProxyUserWuaId(null);


    try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
      securityUtils.when(SecurityUtils::getAuthenticatedUserFromSecurityContext).thenReturn(
          Optional.of(authenticatedUserAccount));

      auditRevisionListener.newRevision(auditRevision);
    }

    assertThat(auditRevision.getPersonId()).isEqualTo(1);
    assertThat(auditRevision.getProxyWuaId()).isNull();
  }

  @Test
  void newRevision_whenUnauthenticated_thenDoNothing() {
    var auditRevision = new AuditRevision();
    auditRevision.setPersonId(null);
    auditRevision.setProxyWuaId(null);

    var person = new Person();
    person.setId(1);

    var authenticatedUserAccount = new AuthenticatedUserAccount();
    authenticatedUserAccount.setPerson(person);
    authenticatedUserAccount.setProxyUserWuaId(null);

    try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
      securityUtils.when(SecurityUtils::getAuthenticatedUserFromSecurityContext).thenReturn(Optional.empty());

      auditRevisionListener.newRevision(auditRevision);
    }

    assertThat(auditRevision.getPersonId()).isNull();
    assertThat(auditRevision.getProxyWuaId()).isNull();
  }
}