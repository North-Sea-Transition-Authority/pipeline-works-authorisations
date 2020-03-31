package uk.co.ogauthority.pwa.service.licence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import org.assertj.core.util.IterableUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.licence.PearsLicence;
import uk.co.ogauthority.pwa.repository.licence.PearsLicenceRepository;

@RunWith(MockitoJUnitRunner.class)
public class PearsLicenceServiceTest {

  @Mock
  private PearsLicenceRepository pearsLicenceRepository;

  private PearsLicenceService pearsLicenceService;
  private PearsLicence pearsLicence_PL1;
  private PearsLicence pearsLicence_AL6;

  @Before
  public void setUp() {
    pearsLicenceService = new PearsLicenceService(pearsLicenceRepository);
    pearsLicence_PL1 = new PearsLicence(1, "PL", 1, "PL6");
    pearsLicence_AL6 = new PearsLicence(2, "AL", 6, "AL6");
  }

  @Test
  public void getLicencesByName() {
    when(pearsLicenceRepository.findAllByLicenceNameContainingIgnoreCase("AL"))
        .thenReturn(List.of(pearsLicence_AL6));
    var licences = pearsLicenceService.getLicencesByName("AL");
    assertThat(licences).containsExactly(pearsLicence_AL6);
  }

  @Test
  public void getAllLicences() {
    when(pearsLicenceRepository.findAll()).thenReturn(IterableUtil.iterable(pearsLicence_PL1, pearsLicence_AL6));
    var licences = pearsLicenceService.getAllLicences();
    assertThat(licences).containsExactly(pearsLicence_PL1, pearsLicence_AL6);
  }

  @Test
  public void getByMasterId_Match() {
    var licence = new PearsLicence();
    when(pearsLicenceRepository.findByMasterId(1)).thenReturn(Optional.of(licence));
    var result = pearsLicenceService.getByMasterId(1);
    assertThat(result).isEqualTo(licence);
  }

  @Test(expected = EntityNotFoundException.class)
  public void getByMasterId_NoMatch() {
    when(pearsLicenceRepository.findByMasterId(1)).thenReturn(Optional.empty());
    pearsLicenceService.getByMasterId(1);
  }

}