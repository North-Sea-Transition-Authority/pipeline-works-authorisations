package uk.co.ogauthority.pwa.features.application.submission;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsProperties;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.features.application.tasks.campaignworks.CampaignWorksService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.CrossingAgreementsService;
import uk.co.ogauthority.pwa.features.application.tasks.designopconditions.PadDesignOpConditionsService;
import uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom.PadEnvironmentalDecommissioningService;
import uk.co.ogauthority.pwa.features.application.tasks.fasttrack.PadFastTrackService;
import uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition.PadFluidCompositionInfoService;
import uk.co.ogauthority.pwa.features.application.tasks.generaltech.PadPipelineTechInfoService;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.PadLocationDetailsService;
import uk.co.ogauthority.pwa.features.application.tasks.othertechprops.PadPipelineOtherPropertiesService;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.DepositDrawingsService;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PermanentDepositService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.overview.TechnicalDrawingSectionService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.PadPipelinesHuooService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureDataJpa
@ActiveProfiles(profiles = { "integration-test", "test" })
@EnableConfigurationProperties(value = AnalyticsProperties.class)
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
