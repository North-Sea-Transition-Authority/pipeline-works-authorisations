package uk.co.ogauthority.pwa.features.application.creation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.repository.PwaApplicationRepository;

@ExtendWith(MockitoExtension.class)
class PwaApplicationReferencingServiceTest {

  @Mock
  private PwaApplicationRepository pwaApplicationRepository;

  private PwaApplicationReferencingService classUnderTest;

  @BeforeEach
  void setup() {
    when(pwaApplicationRepository.getNextRefNum()).thenReturn(1L);
    classUnderTest = new PwaApplicationReferencingService(pwaApplicationRepository);

  }


  @Test
  void createAppReference() {
    assertThat(classUnderTest.createAppReference()).isEqualTo("PA/1");
  }
}