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
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications.PearsLicenceApplication;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications.PearsLicenceApplicationService;

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
    when(applicationService.getApplicationByIds(any())).thenReturn(List.of(generateApplication()));
  }

  @Test
  public void saveLicencesToApplication() {
    var form = new ProjectInformationForm();
    form.setLicenceTransferPlanned(true);
    form.setPearsApplicationList(new String[]{"555", "666"});

    var projectInformation = new PadProjectInformation();
    service.saveApplicationToPad(projectInformation, form);

    verify(applicationService).getApplicationByIds(List.of(555, 666));
    verify(repository).save(any(PadProjectInformationLicenceApplication.class));
  }

  private PearsLicenceApplication generateApplication() {
    return new PearsLicenceApplication(555, "111");
  }
}
