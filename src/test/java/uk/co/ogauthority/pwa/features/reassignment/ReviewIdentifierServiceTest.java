package uk.co.ogauthority.pwa.features.reassignment;

import static org.assertj.core.api.Assertions.assertThat;

import javax.persistence.criteria.Predicate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReviewIdentifierServiceTest {
  @Mock
  CaseReassignmentRepository reassignmentRepository;

  @Captor
  ArgumentCaptor<Predicate[]> criteriaCaptor;

  ReviewIdentifierService service;

  @Before
  public void setup() {
    service = new ReviewIdentifierService(reassignmentRepository);
  }

  @Test
  public void getRassignableCases_NoCaseOfficerFilter() {

    assertThat(criteriaCaptor.getAllValues()).hasSize(2);
  }

  @Test
  public void getRassignableCases_CaseOfficerFilter() {

    assertThat(criteriaCaptor.getAllValues()).hasSize(3);
  }
}
