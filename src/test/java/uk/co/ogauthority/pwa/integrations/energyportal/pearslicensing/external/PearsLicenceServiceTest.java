package uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.internal.PearsLicenceRepository;

@ExtendWith(MockitoExtension.class)
class PearsLicenceServiceTest {

  @Mock
  private PearsLicenceRepository pearsLicenceRepository;

  private PearsLicenceService pearsLicenceService;
  private PearsLicence pearsLicence_PL1;
  private PearsLicence pearsLicence_AL6;

  @BeforeEach
  void setUp() {
    pearsLicenceService = new PearsLicenceService(pearsLicenceRepository);
    pearsLicence_PL1 = new PearsLicence(1, "PL", 1, "PL6", LicenceStatus.EXTANT);
    pearsLicence_AL6 = new PearsLicence(2, "AL", 6, "AL6", LicenceStatus.EXTANT);
  }

  @Test
  void getLicencesByName() {
    when(pearsLicenceRepository.findAllByLicenceNameContainingIgnoreCase("AL"))
        .thenReturn(List.of(pearsLicence_AL6));
    var licences = pearsLicenceService.getLicencesByName("AL");
    assertThat(licences).containsExactly(pearsLicence_AL6);
  }

  @Test
  void getAllLicences() {
    when(pearsLicenceRepository.findAll()).thenReturn(IterableUtil.iterable(pearsLicence_PL1, pearsLicence_AL6));
    var licences = pearsLicenceService.getAllLicences();
    assertThat(licences).containsExactly(pearsLicence_PL1, pearsLicence_AL6);
  }

  @Test
  void getByMasterId_Match() {
    var licence = new PearsLicence();
    when(pearsLicenceRepository.findByMasterId(1)).thenReturn(Optional.of(licence));
    var result = pearsLicenceService.getByMasterId(1);
    assertThat(result).isEqualTo(licence);
  }

  @Test
  void getByMasterId_NoMatch() {
    when(pearsLicenceRepository.findByMasterId(1)).thenReturn(Optional.empty());
    assertThrows(EntityNotFoundException.class, () ->
      pearsLicenceService.getByMasterId(1));
  }

}