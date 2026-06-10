package uk.co.ogauthority.pwa.service.search.consents;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.search.consents.ConsentSearchContext;
import uk.co.ogauthority.pwa.model.search.consents.ConsentSearchParams;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.search.consents.predicates.PipelinePredicateProvider;

@ExtendWith(MockitoExtension.class)
class PipelinePredicateProviderTest {

  @MockitoBean
  private EntityManager entityManager;

  private PipelinePredicateProvider pipelinePredicateProvider;

  private final ConsentSearchContext context = new ConsentSearchContext(new AuthenticatedUserAccount(new WebUserAccount(1), Set.of()), UserType.OGA);

  @BeforeEach
  void setUp() {
    pipelinePredicateProvider = new PipelinePredicateProvider(entityManager);
  }

  @Test
  void shouldApplyToSearch_noReferenceOrNumberProvided() {
    var params = new ConsentSearchParams();

    assertThat(pipelinePredicateProvider.shouldApplyToSearch(params, context)).isFalse();

  }

  @Test
  void shouldApplyToSearch_referenceProvided() {
    var params = new ConsentSearchParams();
    params.setPipelineReference("ref");

    assertThat(pipelinePredicateProvider.shouldApplyToSearch(params, context)).isTrue();
  }

  @Test
  void shouldApplyToSearch_pipelineNumberSelectorFieldProvided() {
    var params = new ConsentSearchParams();
    params.setPipelineNumberSelectorField("PL10");

    assertThat(pipelinePredicateProvider.shouldApplyToSearch(params, context)).isTrue();
  }
}