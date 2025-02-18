package uk.co.ogauthority.pwa.features.application.tasks.projectinfo;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications.PearsLicenceTransaction;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications.PearsLicenceTransactionService;

@ExtendWith(MockitoExtension.class)
class PadLicenceTransactionServiceTest {

  @Mock
  private PadProjectInformationLicenceApplicationRepository repository;

  @Mock
  private PearsLicenceTransactionService applicationService;

  @Mock
  private PadProjectInformationService projectInformationService;

  @Mock
  private EntityManager entityManager;

  @Captor
  ArgumentCaptor<PadProjectInformationLicenceApplication> applicationCaptor;

  private PadLicenceTransactionService service;

  @BeforeEach
  void setUp() {
    service = new PadLicenceTransactionService(repository, applicationService, entityManager);
  }

  @Test
  void saveApplicationsToPad() {
    when(applicationService.getApplicationsByIds(any())).thenReturn(List.of(generateApplication()));

    var form = new ProjectInformationForm();
    form.setLicenceTransferPlanned(true);
    form.setPearsApplicationList(new String[]{"555", "666"});

    var projectInformation = new PadProjectInformation();
    service.saveApplicationsToPad(projectInformation, form);

    verify(applicationService).getApplicationsByIds(List.of(555, 666));
    verify(repository).saveAll(any(List.class));
  }

  @Test
  void mapEntityToForm() {
    var projectInformation = new PadProjectInformation();
    projectInformation.setId(1);

    var form = new ProjectInformationForm();
    var projectInfoLicenceApplication = new PadProjectInformationLicenceApplication();
    projectInfoLicenceApplication.setPearsLicenceApplications(new PearsLicenceTransaction(555, "PL47/401"));

    when(repository.findAllByPadProjectInformation(projectInformation)).thenReturn(List.of(projectInfoLicenceApplication));
    service.mapApplicationsToForm(form, projectInformation);

    assertThat(form.getPearsApplicationList()).containsOnly("555");
  }

  @Test
  void copyEntities() {
    var oldProjectInformation = new PadProjectInformation();
    oldProjectInformation.setId(2222);
    var newProjectInformation = new PadProjectInformation();
    newProjectInformation.setId(321);

    var pearsApplication = new PearsLicenceTransaction();

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


  private PearsLicenceTransaction generateApplication() {
    return new PearsLicenceTransaction(555, "111");
  }
}
