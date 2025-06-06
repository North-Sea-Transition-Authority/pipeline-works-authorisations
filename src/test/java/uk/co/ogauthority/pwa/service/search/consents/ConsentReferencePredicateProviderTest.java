package uk.co.ogauthority.pwa.service.search.consents;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.search.consents.ConsentSearchContext;
import uk.co.ogauthority.pwa.model.search.consents.ConsentSearchParams;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.search.consents.predicates.ConsentReferencePredicateProvider;

@ExtendWith(MockitoExtension.class)
class ConsentReferencePredicateProviderTest {

  @MockBean
  private EntityManager entityManager;

  private ConsentReferencePredicateProvider consentReferencePredicateProvider;

  private ConsentSearchContext context = new ConsentSearchContext(new AuthenticatedUserAccount(new WebUserAccount(1), Set.of()), UserType.OGA);

  @BeforeEach
  void setUp() throws Exception {

    consentReferencePredicateProvider = new ConsentReferencePredicateProvider(entityManager);
  }

  @Test
  void shouldApplyToSearch_noReferenceProvided() {

    var params = new ConsentSearchParams();

    assertThat(consentReferencePredicateProvider.shouldApplyToSearch(params, context)).isFalse();

  }

  @Test
  void shouldApplyToSearch_referenceProvided() {

    var params = new ConsentSearchParams();
    params.setConsentReference("ref");

    assertThat(consentReferencePredicateProvider.shouldApplyToSearch(params, context)).isTrue();

  }
}