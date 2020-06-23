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
import uk.co.ogauthority.pwa.service.pwaapplications.shared.campaignworks.CampaignWorksService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossingAgreementsService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.location.PadLocationDetailsService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdepositdrawings.DepositDrawingsService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.PermanentDepositService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PadPipelinesHuooService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinetechinfo.PadPipelineTechInfoService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation.PadProjectInformationService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.TechnicalDrawingSectionService;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureDataJpa
@ActiveProfiles("integration-test")
public class TaskCompletionServiceIntegrationTest {

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
  private TechnicalDrawingSectionService technicalDrawingSectionService;

  @MockBean
  private PadPipelineService padPipelineService;

  @MockBean
  private PermanentDepositService permanentDepositService;

  @MockBean
  private DepositDrawingsService depositDrawingsService;

  @MockBean
  private CampaignWorksService campaignWorksService;

  @MockBean
  private PadPipelinesHuooService padPipelinesHuooService;

  @MockBean
  private PadPipelineTechInfoService padPipelineTechInfoService;

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
          service = padPipelineService;
          break;
        case PIPELINES_HUOO:
          service = padPipelinesHuooService;
          break;
        case CAMPAIGN_WORKS:
          service = campaignWorksService;
          break;
        case TECHNICAL_DRAWINGS:
          service = technicalDrawingSectionService;
          break;
        case PERMANENT_DEPOSITS:
          service = permanentDepositService;
          break;
        case PERMANENT_DEPOSIT_DRAWINGS:
          service = depositDrawingsService;
          break;
        case GENERAL_TECH_DETAILS:
          service = padPipelineTechInfoService;
          break;
        default:
          throw new AssertionError(task);
      }

      when(service.isComplete(detail)).thenReturn(true);

      var isComplete = taskCompletionService.isTaskComplete(detail, task);

      verify(service, times(1)).isComplete(detail);

      assertThat(isComplete).isTrue();

    });

  }

}
