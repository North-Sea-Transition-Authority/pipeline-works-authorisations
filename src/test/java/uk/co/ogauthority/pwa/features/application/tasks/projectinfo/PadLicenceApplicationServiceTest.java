package uk.co.ogauthority.pwa.features.application.tasks.projectinfo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications.PearsLicenceApplicationService;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications.PearsLicenceApplications;

@RunWith(MockitoJUnitRunner.class)
public class PadLicenceApplicationServiceTest {

  @Mock
  private PadProjectInformationLicenceApplicationsRepository repository;

  @Mock
  private PearsLicenceApplicationService applicationService;

  private PadLicenceApplicationService service;

  @Before
  public void setUp() {
    service = new PadLicenceApplicationService(repository, applicationService);
    when(applicationService.getLicencesByIds(any())).thenReturn(List.of(generateApplication()));
  }

  @Test
  public void saveLicencesToApplication() {
    var form = new ProjectInformationForm();
    form.setLicenceTransferPlanned(true);
    form.setLicenceList(new String[]{"555", "666"});

    var projectInformation = new PadProjectInformation();
    service.saveLicencesToApplication(projectInformation, form);

    verify(applicationService).getLicencesByIds(List.of(555, 666));
    verify(repository).save(any(PadProjectInformationLicenceApplications.class));
  }

  private PearsLicenceApplications generateApplication() {
    return new PearsLicenceApplications(555, "111");
  }
}
