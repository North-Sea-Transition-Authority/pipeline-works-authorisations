package uk.co.ogauthority.pwa.service.search.applicationsearch.restrictions;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchContextTestUtil;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchParametersBuilder;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationReferencePredicateProviderTest {

  @MockBean
  private EntityManager entityManager;

  private ApplicationReferencePredicateProvider applicationReferencePredicateProvider;

  @Before
  public void setUp() throws Exception {

    applicationReferencePredicateProvider = new ApplicationReferencePredicateProvider(entityManager);
  }

  @Test
  public void doesPredicateApply_noAppReferenceProvided() {

    var context = ApplicationSearchContextTestUtil.emptyUserContext(null, UserType.OGA);
    var params = ApplicationSearchParametersBuilder.createEmptyParams();

    assertThat(applicationReferencePredicateProvider.doesPredicateApply(context, params)).isFalse();

  }

  @Test
  public void doesPredicateApply_AppReferenceProvided() {

    var context = ApplicationSearchContextTestUtil.emptyUserContext(null, UserType.OGA);
    var params = new ApplicationSearchParametersBuilder()
        .setAppReference("some ref")
        .createApplicationSearchParameters();

    assertThat(applicationReferencePredicateProvider.doesPredicateApply(context, params)).isTrue();

  }
}