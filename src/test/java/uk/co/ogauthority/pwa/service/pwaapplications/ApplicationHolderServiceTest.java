package uk.co.ogauthority.pwa.service.pwaapplications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.pwa.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwa.huoo.ApplicationHolderOrganisation;
import uk.co.ogauthority.pwa.repository.pwaapplications.huoo.ApplicationHolderOrganisationRepository;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.ApplicationHolderService;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationHolderServiceTest {

  @Mock
  private ApplicationHolderOrganisationRepository applicationHolderOrganisationRepository;

  @Captor
  ArgumentCaptor<List<ApplicationHolderOrganisation>> orgsDeletedCaptor;

  @Captor
  ArgumentCaptor<ApplicationHolderOrganisation> appHolderArgCaptor;

  private ApplicationHolderService applicationHolderService;

  @Before
  public void setUp() {
    applicationHolderService = new ApplicationHolderService(applicationHolderOrganisationRepository);
  }

  @Test
  public void testHolderDetails() {

    var detail = new PwaApplicationDetail();
    var appHolder1 = new ApplicationHolderOrganisation();
    var appHolder2 = new ApplicationHolderOrganisation();
    when(applicationHolderOrganisationRepository.findByPwaApplicationDetail(detail)).thenReturn(List.of(appHolder1, appHolder2));

    var portalOrg = new PortalOrganisationUnit();

    applicationHolderService.updateHolderDetails(detail, portalOrg);

    verify(applicationHolderOrganisationRepository, times(1)).deleteAll(orgsDeletedCaptor.capture());
    verify(applicationHolderOrganisationRepository, times(1)).save(appHolderArgCaptor.capture());

    assertThat(orgsDeletedCaptor.getValue()).containsExactlyInAnyOrder(appHolder1, appHolder2);

    var appHolderOrg = appHolderArgCaptor.getValue();
    assertThat(appHolderOrg.getPwaApplicationDetail()).isEqualTo(detail);
    assertThat(appHolderOrg.getOrganisationUnit()).isEqualTo(portalOrg);

  }

}
