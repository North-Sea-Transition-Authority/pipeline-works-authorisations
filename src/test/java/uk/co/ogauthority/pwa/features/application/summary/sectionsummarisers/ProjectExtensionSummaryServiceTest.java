package uk.co.ogauthority.pwa.features.application.summary.sectionsummarisers;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.features.application.summary.ApplicationSectionSummaryType;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@RunWith(MockitoJUnitRunner.class)
public class ProjectExtensionSummaryServiceTest {

  @Mock
  TaskListService taskListService;

  @Mock
  PadFileService padFileService;

  private ProjectExtensionSummaryService projectExtensionSummaryService;

  @Before
  public void setup() {
    projectExtensionSummaryService = new ProjectExtensionSummaryService(taskListService, padFileService);
  }

  @Test
  public void canSummariseTest() {
    when(taskListService.anyTaskShownForApplication(eq(Set.of(ApplicationTask.PROJECT_EXTENSION)),
        any(PwaApplicationDetail.class))).thenReturn(true);
    assertTrue(projectExtensionSummaryService.canSummarise(new PwaApplicationDetail()));
  }

  @Test
  public void summariseService_verifyServiceInteractions() {
    projectExtensionSummaryService.summariseSection(new PwaApplicationDetail(),
        ApplicationSectionSummaryType.PROJECT_EXTENSION.getTemplatePath());

    verify(padFileService).getUploadedFileViews(any(PwaApplicationDetail.class),
        eq(ApplicationDetailFilePurpose.PROJECT_EXTENSION),
        eq(ApplicationFileLinkStatus.FULL));
  }
}
