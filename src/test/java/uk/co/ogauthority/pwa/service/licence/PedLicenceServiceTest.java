package uk.co.ogauthority.pwa.service.licence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.assertj.core.util.IterableUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.model.entity.licence.PedLicence;
import uk.co.ogauthority.pwa.repository.licence.PedLicenceRepository;

@RunWith(SpringRunner.class)
public class PedLicenceServiceTest {

  @Mock
  private PedLicenceRepository pedLicenceRepository;

  private PedLicenceService pedLicenceService;
  private PedLicence pedLicence_PL1;
  private PedLicence pedLicence_AL6;

  @Before
  public void setUp() {
    pedLicenceService = new PedLicenceService(pedLicenceRepository);
    pedLicence_PL1 = new PedLicence(1, "PL", 1, "PL6");
    pedLicence_AL6 = new PedLicence(2, "AL", 6, "AL6");
  }

  @Test
  public void getLicencesByName() {
    when(pedLicenceRepository.findAllByLicenceNameLikeIgnoreCase("AL"))
        .thenReturn(List.of(pedLicence_AL6));
    var licences = pedLicenceService.getLicencesByName("AL");
    assertThat(licences).containsExactly(pedLicence_AL6);
  }

  @Test
  public void getAllLicences() {
    when(pedLicenceRepository.findAll()).thenReturn(IterableUtil.iterable(pedLicence_PL1, pedLicence_AL6));
    var licences = pedLicenceService.getAllLicences();
    assertThat(licences).containsExactly(pedLicence_PL1, pedLicence_AL6);
  }
}