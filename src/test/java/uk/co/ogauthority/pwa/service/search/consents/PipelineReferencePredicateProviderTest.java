package uk.co.ogauthority.pwa.service.search.consents;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import javax.persistence.EntityManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.search.consents.ConsentSearchContext;
import uk.co.ogauthority.pwa.model.search.consents.ConsentSearchParams;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.search.consents.predicates.PipelineReferencePredicateProvider;

@RunWith(MockitoJUnitRunner.class)
public class PipelineReferencePredicateProviderTest {

  @MockBean
  private EntityManager entityManager;

  private PipelineReferencePredicateProvider pipelineReferencePredicateProvider;

  private ConsentSearchContext context = new ConsentSearchContext(new AuthenticatedUserAccount(new WebUserAccount(1), Set.of()), UserType.OGA);

  @Before
  public void setUp() throws Exception {

    pipelineReferencePredicateProvider = new PipelineReferencePredicateProvider(entityManager);
  }

  @Test
  public void shouldApplyToSearch_noReferenceProvided() {

    var params = new ConsentSearchParams();

    assertThat(pipelineReferencePredicateProvider.shouldApplyToSearch(params, context)).isFalse();

  }

  @Test
  public void shouldApplyToSearch_referenceProvided() {

    var params = new ConsentSearchParams();
    params.setPipelineReference("ref");

    assertThat(pipelineReferencePredicateProvider.shouldApplyToSearch(params, context)).isTrue();

  }
}