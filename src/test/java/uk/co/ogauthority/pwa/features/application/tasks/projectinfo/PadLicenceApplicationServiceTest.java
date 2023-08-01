package uk.co.ogauthority.pwa.features.application.tasks.projectinfo;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import javax.persistence.EntityManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications.PearsLicenceApplication;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications.PearsLicenceApplicationService;

@RunWith(MockitoJUnitRunner.class)
public class PadLicenceApplicationServiceTest {

  @Mock
  private PadProjectInformationLicenceApplicationRepository repository;

  @Mock
  private PearsLicenceApplicationService applicationService;

  @Mock
  private PadProjectInformationService projectInformationService;

  @Mock
  private EntityManager entityManager;

  @Captor
  ArgumentCaptor<PadProjectInformationLicenceApplication> applicationCaptor;

  private PadLicenceApplicationService service;

  @Before
  public void setUp() {
    service = new PadLicenceApplicationService(repository, applicationService, entityManager);
    when(applicationService.getApplicationsByIds(any())).thenReturn(List.of(generateApplication()));
  }

  @Test
  public void saveApplicationsToPad() {
    var form = new ProjectInformationForm();
    form.setLicenceTransferPlanned(true);
    form.setPearsApplicationList(new String[]{"555", "666"});

    var projectInformation = new PadProjectInformation();
    service.saveApplicationsToPad(projectInformation, form);

    verify(applicationService).getApplicationsByIds(List.of(555, 666));
    verify(repository).save(any(PadProjectInformationLicenceApplication.class));
  }

  @Test
  public void mapEntityToForm() {
    var projectInformation = new PadProjectInformation();
    projectInformation.setId(1);

    var form = new ProjectInformationForm();
    var projectInfoLicenceApplication = new PadProjectInformationLicenceApplication();
    projectInfoLicenceApplication.setPearsLicenceApplications(new PearsLicenceApplication(555, "PL47/401"));

    when(repository.findAllByPadProjectInformation(projectInformation)).thenReturn(List.of(projectInfoLicenceApplication));
    service.mapApplicationsToForm(form, projectInformation);

    assertThat(form.getPearsApplicationList()).containsOnly("555");
  }

  @Test
  public void copyEntities() {
    var oldProjectInformation = new PadProjectInformation();
    oldProjectInformation.setId(2222);
    var newProjectInformation = new PadProjectInformation();
    newProjectInformation.setId(321);

    var pearsApplication = new PearsLicenceApplication();

    var oldApplicationReference = new PadProjectInformationLicenceApplication();
    oldApplicationReference.setId(500);
    oldApplicationReference.setPadProjectInformation(oldProjectInformation);
    oldApplicationReference.setPearsLicenceApplications(pearsApplication);
    when(repository.findAllByPadProjectInformation(oldProjectInformation)).thenReturn(List.of(oldApplicationReference));

    service.copyApplicationsToPad(oldProjectInformation, newProjectInformation);
    verify(entityManager).persist(applicationCaptor.capture());

    var capturedEntity = applicationCaptor.getValue();
    assertThat(capturedEntity.getId()).isNull();
    assertThat(capturedEntity.getPadProjectInformation()).isEqualTo(newProjectInformation);
    assertThat(capturedEntity.getPearsLicenceApplication()).isEqualTo(pearsApplication);
  }


  private PearsLicenceApplication generateApplication() {
    return new PearsLicenceApplication(555, "111");
  }
}
