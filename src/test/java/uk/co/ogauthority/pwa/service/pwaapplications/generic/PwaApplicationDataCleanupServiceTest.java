package uk.co.ogauthority.pwa.service.pwaapplications.generic;

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
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom.PadEnvironmentalDecommissioningService;
import uk.co.ogauthority.pwa.features.application.tasks.fasttrack.PadFastTrackService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.campaignworks.CampaignWorksService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossingAgreementsService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.location.PadLocationDetailsService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.DepositDrawingsService;
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
public class PwaApplicationDataCleanupServiceTest {

  @SpyBean
  private PwaApplicationDataCleanupService pwaApplicationDataCleanupService;

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
  private TaskListService taskListService;

  @MockBean
  private PadFileService padFileService;

  @Test
  public void cleanupData() {

    var detail = new PwaApplicationDetail();

    when(taskListService.getShownApplicationTasksForDetail(detail)).thenReturn(List.of(
        ApplicationTask.PROJECT_INFORMATION,
        ApplicationTask.GENERAL_TECH_DETAILS,
        ApplicationTask.LOCATION_DETAILS
    ));

    var user = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createDefaultPerson()), List.of());
    pwaApplicationDataCleanupService.cleanupData(detail, user);

    verify(projectInformationService, times(1)).cleanupData(detail);
    verify(padPipelineTechInfoService, times(1)).cleanupData(detail);
    verify(padLocationDetailsService, times(1)).cleanupData(detail);
    verify(padFileService, times(1)).deleteTemporaryFilesForDetail(detail, user);

  }

}
