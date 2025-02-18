package uk.co.ogauthority.pwa.service.search.applicationsearch.restrictions;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchContextTestUtil;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchParametersBuilder;

@ExtendWith(MockitoExtension.class)
class ApplicationReferencePredicateProviderTest {

  @MockBean
  private EntityManager entityManager;

  private ApplicationReferencePredicateProvider applicationReferencePredicateProvider;

  @BeforeEach
  void setUp() throws Exception {

    applicationReferencePredicateProvider = new ApplicationReferencePredicateProvider(entityManager);
  }

  @Test
  void doesPredicateApply_noAppReferenceProvided() {

    var context = ApplicationSearchContextTestUtil.emptyUserContext(null, UserType.OGA);
    var params = ApplicationSearchParametersBuilder.createEmptyParams();

    assertThat(applicationReferencePredicateProvider.doesPredicateApply(context, params)).isFalse();

  }

  @Test
  void doesPredicateApply_AppReferenceProvided() {

    var context = ApplicationSearchContextTestUtil.emptyUserContext(null, UserType.OGA);
    var params = new ApplicationSearchParametersBuilder()
        .setAppReference("some ref")
        .createApplicationSearchParameters();

    assertThat(applicationReferencePredicateProvider.doesPredicateApply(context, params)).isTrue();

  }
}