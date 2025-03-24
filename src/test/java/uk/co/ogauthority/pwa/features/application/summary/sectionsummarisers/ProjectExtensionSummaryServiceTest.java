package uk.co.ogauthority.pwa.features.application.summary.sectionsummarisers;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.features.application.summary.ApplicationSectionSummaryType;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@ExtendWith(MockitoExtension.class)
class ProjectExtensionSummaryServiceTest {

  @Mock
  TaskListService taskListService;

  @Mock
  PadFileManagementService padFileManagementService;

  private ProjectExtensionSummaryService projectExtensionSummaryService;

  @BeforeEach
  void setup() {
    projectExtensionSummaryService = new ProjectExtensionSummaryService(taskListService, padFileManagementService);
  }

  @Test
  void canSummariseTest() {
    when(taskListService.anyTaskShownForApplication(eq(Set.of(ApplicationTask.PROJECT_EXTENSION)),
        any(PwaApplicationDetail.class))).thenReturn(true);
    assertTrue(projectExtensionSummaryService.canSummarise(new PwaApplicationDetail()));
  }

  @Test
  void summariseService_verifyServiceInteractions() {
    var pwaApplicationDetail = new PwaApplicationDetail();

    projectExtensionSummaryService.summariseSection(pwaApplicationDetail,
        ApplicationSectionSummaryType.PROJECT_EXTENSION.getTemplatePath());

    verify(padFileManagementService).getUploadedFileViews(pwaApplicationDetail, FileDocumentType.PROJECT_EXTENSION);
  }
}
