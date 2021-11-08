package uk.co.ogauthority.pwa.features.application.creation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.repository.PwaApplicationRepository;

@RunWith(MockitoJUnitRunner.class)
public class PwaApplicationReferencingServiceTest {

  @Mock
  private PwaApplicationRepository pwaApplicationRepository;

  private PwaApplicationReferencingService classUnderTest;

  @Before
  public void setup() {
    when(pwaApplicationRepository.getNextRefNum()).thenReturn(1L);
    classUnderTest = new PwaApplicationReferencingService(pwaApplicationRepository);

  }


  @Test
  public void createAppReference() {
    assertThat(classUnderTest.createAppReference()).isEqualTo("PA/1");
  }
}