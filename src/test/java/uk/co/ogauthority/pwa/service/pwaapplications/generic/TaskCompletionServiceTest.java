package uk.co.ogauthority.pwa.service.pwaapplications.generic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadEnvironmentalDecommissioningService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadFastTrackService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossingAgreementsService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.location.PadLocationDetailsService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.PermanentDepositsService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelinesService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation.PadProjectInformationService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.TechnicalDrawingsService;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureDataJpa
@ActiveProfiles("integration-test")
public class TaskCompletionServiceTest {

  @SpyBean
  private TaskCompletionService taskCompletionService;

  @Autowired
  private ApplicationContext springAppContext;

  @MockBean
  private PadFastTrackService padFastTrackService;

  @MockBean
  private PadProjectInformationService projectInformationService;

  @MockBean
  private PadEnvironmentalDecommissioningService padEnvironmentalDecommissioningService;

  @MockBean
  private CrossingAgreementsService crossingAgreementsService;

  @MockBean
  private PadLocationDetailsService padLocationDetailsService;

  @MockBean
  private PadOrganisationRoleService padOrganisationRoleService;

  @MockBean
  private TechnicalDrawingsService technicalDrawingsService;

  @MockBean
  private PadPipelinesService padPipelinesService;

  @MockBean
  private PermanentDepositsService permanentDepositsService;

  @Test
  public void isTaskComplete() {

    var detail = new PwaApplicationDetail();

    ApplicationTask.stream().forEach(task -> {

      ApplicationFormSectionService service;

      switch (task) {

        case PROJECT_INFORMATION:
          service = projectInformationService;
          break;
        case FAST_TRACK:
          service = padFastTrackService;
          break;
        case ENVIRONMENTAL_DECOMMISSIONING:
          service = padEnvironmentalDecommissioningService;
          break;
        case CROSSING_AGREEMENTS:
          service = crossingAgreementsService;
          break;
        case LOCATION_DETAILS:
          service = padLocationDetailsService;
          break;
        case HUOO:
          service = padOrganisationRoleService;
          break;
        case PIPELINES:
          service = padPipelinesService;
          break;
        case TECHNICAL_DRAWINGS:
          service = technicalDrawingsService;
          break;
        case PERMANENT_DEPOSITS:
          service = permanentDepositsService;
          break;
        default:
          throw new AssertionError();
      }

      when(service.isComplete(detail)).thenReturn(true);

      var isComplete = taskCompletionService.isTaskComplete(detail, task);

      verify(service, times(1)).isComplete(detail);

      assertThat(isComplete).isTrue();

    });

  }

}
