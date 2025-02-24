package uk.co.ogauthority.pwa.service.mailmerge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationService;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.DateUtils;

@ExtendWith(MockitoExtension.class)
class PwaApplicationMailMergeResolverTest {

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  @Mock
  private TaskListService taskListService;

  @Mock
  private ApplicationContext applicationContext;

  @Mock
  private PadProjectInformationService padProjectInformationService;

  @Mock
  private ApplicationFormSectionService genericSectionService;

  private PwaApplicationMailMergeResolver pwaApplicationMailMergeResolver;

  private List<MailMergeFieldMnem> mailMergeFields;

  @BeforeEach
  void setUp() throws Exception {

    pwaApplicationMailMergeResolver = new PwaApplicationMailMergeResolver(pwaApplicationDetailService, taskListService, applicationContext);

    mailMergeFields = Arrays.stream(MailMergeFieldMnem.values())
        .collect(Collectors.toList());

  }

  @Test
  void supportsDocumentSource_app_true() {

    var app = new PwaApplication();

    boolean supported = pwaApplicationMailMergeResolver.supportsDocumentSource(app);

    assertThat(supported).isTrue();

  }

  @Test
  void getAvailableMailMergeFields() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    when(pwaApplicationDetailService.getLatestSubmittedDetail(detail.getPwaApplication()))
        .thenReturn(Optional.of(detail));

    when(taskListService.getShownApplicationTasksForDetail(detail))
        .thenReturn(List.of(ApplicationTask.PROJECT_INFORMATION));

    when(applicationContext.getBean(PadProjectInformationService.class)).thenReturn(padProjectInformationService);
    when(padProjectInformationService.getAvailableMailMergeFields(detail.getPwaApplicationType())).thenReturn(mailMergeFields);

    var availableFields = pwaApplicationMailMergeResolver.getAvailableMailMergeFields(detail.getPwaApplication());

    assertThat(availableFields).isEqualTo(mailMergeFields);

  }

  @Test
  void resolveMergeFields() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    when(pwaApplicationDetailService.getLatestSubmittedDetail(detail.getPwaApplication()))
        .thenReturn(Optional.of(detail));

    var allTasks = ApplicationTask.stream()
        .collect(Collectors.toList());
    when(taskListService.getShownApplicationTasksForDetail(detail))
        .thenReturn(List.of(ApplicationTask.PROJECT_INFORMATION));

    when(applicationContext.getBean(PadProjectInformationService.class)).thenReturn(padProjectInformationService);

    var resolvedFields = Map.of(
        MailMergeFieldMnem.PROPOSED_START_OF_WORKS_DATE, DateUtils.formatDate(Instant.now()),
        MailMergeFieldMnem.PROJECT_NAME, "project name"
    );

    when(padProjectInformationService.resolveMailMergeFields(detail)).thenReturn(resolvedFields);

    var resultMap = pwaApplicationMailMergeResolver.resolveMergeFields(detail.getPwaApplication());

    var expectedResultMap = resolvedFields.entrySet().stream()
        .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));

    assertThat(resultMap).containsExactlyInAnyOrderEntriesOf(expectedResultMap);

  }

}