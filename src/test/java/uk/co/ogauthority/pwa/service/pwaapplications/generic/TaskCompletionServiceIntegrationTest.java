package uk.co.ogauthority.pwa.service.pwaapplications.generic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
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
import uk.co.ogauthority.pwa.service.devuk.PadFieldService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadEnvironmentalDecommissioningService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadFastTrackService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.campaignworks.CampaignWorksService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossingAgreementsService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.location.PadLocationDetailsService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.partnerletters.PadPartnerLettersService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdepositdrawings.DepositDrawingsService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.PermanentDepositService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PadPipelinesHuooService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinetechinfo.PadDesignOpConditionsService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinetechinfo.PadFluidCompositionInfoService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinetechinfo.PadPipelineOtherPropertiesService;
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

  @MockBean
  private PadFluidCompositionInfoService padFluidCompositionInfoService;

  @MockBean
  private PadPipelineOtherPropertiesService padPipelineOtherPropertiesService;

  @MockBean
  private PadDesignOpConditionsService padDesignOpConditionsService;

  @MockBean
  private PadPartnerLettersService padPartnerLettersService;

  @MockBean
  private PadFieldService padFieldService;

  @MockBean
  private PwaContactService pwaContactService;

  private ApplicationFormSectionService getMockedTaskService(ApplicationTask applicationTask) {
    switch (applicationTask) {
      case FIELD_INFORMATION:
        return padFieldService;
      case APPLICATION_USERS:
        return pwaContactService;
      case PROJECT_INFORMATION:
        return projectInformationService;
      case FAST_TRACK:
        return padFastTrackService;
      case ENVIRONMENTAL_DECOMMISSIONING:
        return padEnvironmentalDecommissioningService;
      case CROSSING_AGREEMENTS:
        return crossingAgreementsService;
      case LOCATION_DETAILS:
        return padLocationDetailsService;
      case HUOO:
        return padOrganisationRoleService;
      case PIPELINES:
        return padPipelineService;
      case PIPELINES_HUOO:
        return padPipelinesHuooService;
      case CAMPAIGN_WORKS:
        return campaignWorksService;
      case TECHNICAL_DRAWINGS:
        return technicalDrawingSectionService;
      case PERMANENT_DEPOSITS:
        return permanentDepositService;
      case PERMANENT_DEPOSIT_DRAWINGS:
        return depositDrawingsService;
      case GENERAL_TECH_DETAILS:
        return padPipelineTechInfoService;
      case FLUID_COMPOSITION:
        return padFluidCompositionInfoService;
      case PIPELINE_OTHER_PROPERTIES:
        return padPipelineOtherPropertiesService;
      case DESIGN_OP_CONDITIONS:
        return padDesignOpConditionsService;
      case PARTNER_LETTERS:
        return padPartnerLettersService;
      default:
        throw new AssertionError(applicationTask);
    }
  }

  @Test
  public void isTaskComplete() {

    var detail = new PwaApplicationDetail();

    ApplicationTask.stream().forEach(task -> {

      ApplicationFormSectionService service = getMockedTaskService(task);

      when(service.isComplete(detail)).thenReturn(true);

      var isComplete = taskCompletionService.isTaskComplete(detail, task);

      verify(service, times(1)).isComplete(detail);

      assertThat(isComplete).isTrue();

    });

  }

  @Test
  public void getTaskInfoList() {

    var detail = new PwaApplicationDetail();

    ApplicationTask.stream().forEach(task -> {

      ApplicationFormSectionService service = getMockedTaskService(task);

      when(service.getTaskInfoList(detail)).thenReturn(List.of());

      var taskInfoList = taskCompletionService.getTaskInfoList(detail, task);

      verify(service, times(1)).getTaskInfoList(detail);

      assertThat(taskInfoList).isEmpty();

    });

  }

}
