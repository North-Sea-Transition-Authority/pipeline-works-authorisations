package uk.co.ogauthority.pwa.features.reassignment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.WorkAreaApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.service.workarea.viewentities.WorkAreaAppUserTab;
import uk.co.ogauthority.pwa.service.workarea.viewentities.WorkAreaAppUserTab_;

@RunWith(MockitoJUnitRunner.class)
public class ReviewIdentifierServiceTest {

  @Mock
  EntityManager entityManager;

  @Mock
  CriteriaBuilder criteriaBuilder;

  @Mock
  CriteriaQuery<WorkAreaApplicationDetailSearchItem> criteriaQuery;

  @Mock
  Root rootQuery;

  @Mock
  Join joinQuery;

  @Captor
  ArgumentCaptor<Predicate[]> criteriaCaptor;

  ReviewIdentifierService service;

  @Before
  public void setup() {
    service = new ReviewIdentifierService(entityManager);
    when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
    when(criteriaBuilder.createQuery(WorkAreaApplicationDetailSearchItem.class)).thenReturn(criteriaQuery);
    when(criteriaQuery.from(WorkAreaAppUserTab.class)).thenReturn(rootQuery);
    when(rootQuery.join(WorkAreaAppUserTab_.workAreaApplicationDetailSearchItem)).thenReturn(joinQuery);
    when(entityManager.createQuery(any(CriteriaQuery.class))).thenReturn(mock(TypedQuery.class));
  }

  @Test
  public void getRassignableCases_NoCaseOfficerFilter() {
    service.findCasesInReview(null);

    verify(criteriaQuery).where(criteriaCaptor.capture());
    verify(joinQuery, never()).get(ApplicationDetailView_.CASE_OFFICER_PERSON_ID);
    assertThat(criteriaCaptor.getAllValues()).hasSize(2);
  }

  @Test
  public void getRassignableCases_CaseOfficerFilter() {
    service.findCasesInReview(5000);

    verify(criteriaQuery).where(criteriaCaptor.capture());
    verify(joinQuery).get(ApplicationDetailView_.CASE_OFFICER_PERSON_ID);
    assertThat(criteriaCaptor.getAllValues()).hasSize(3);
  }
}
