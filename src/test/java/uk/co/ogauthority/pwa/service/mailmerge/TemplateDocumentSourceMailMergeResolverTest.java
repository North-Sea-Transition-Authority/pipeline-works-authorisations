package uk.co.ogauthority.pwa.service.mailmerge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationService;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem;
import uk.co.ogauthority.pwa.service.documents.templates.TemplateDocumentSource;

@ExtendWith(MockitoExtension.class)
class TemplateDocumentSourceMailMergeResolverTest {

  @Mock
  private TaskListService taskListService;

  @Mock
  private ApplicationContext applicationContext;

  @Mock
  private PadProjectInformationService padProjectInformationService;

  private TemplateDocumentSourceMailMergeResolver templateDocumentSourceMailMergeResolver;

  private List<MailMergeFieldMnem> mailMergeFields;

  @BeforeEach
  void setUp() throws Exception {

    templateDocumentSourceMailMergeResolver = new TemplateDocumentSourceMailMergeResolver(taskListService, applicationContext);

    mailMergeFields = Arrays.stream(MailMergeFieldMnem.values())
        .collect(Collectors.toList());

  }

  @Test
  void supportsDocumentSource_templateDocSource_true() {

    var source = new TemplateDocumentSource(DocumentSpec.INITIAL_PETROLEUM_CONSENT_DOCUMENT);

    boolean supported = templateDocumentSourceMailMergeResolver.supportsDocumentSource(source);

    assertThat(supported).isTrue();

  }

  @Test
  void getAvailableMailMergeFields() {

    var source = new TemplateDocumentSource(DocumentSpec.INITIAL_PETROLEUM_CONSENT_DOCUMENT);

    when(taskListService.getApplicationTasksForAppType(any()))
        .thenReturn(List.of(ApplicationTask.PROJECT_INFORMATION));

    when(applicationContext.getBean(PadProjectInformationService.class)).thenReturn(padProjectInformationService);
    when(padProjectInformationService.getAvailableMailMergeFields(any())).thenReturn(mailMergeFields);

    var availableFields = templateDocumentSourceMailMergeResolver.getAvailableMailMergeFields(source);

    assertThat(availableFields)
        .hasSize(mailMergeFields.size())
        .containsAll(mailMergeFields);

  }

  @Test
  void resolveMergeFields() {

    var mockResolver = mock(TemplateDocumentSourceMailMergeResolver.class);

    when(mockResolver.getAvailableMailMergeFields(any())).thenReturn(List.of(MailMergeFieldMnem.PROJECT_NAME, MailMergeFieldMnem.PROPOSED_START_OF_WORKS_DATE));

    when(mockResolver.resolveMergeFields(any())).thenCallRealMethod();

    var docSource = new TemplateDocumentSource(DocumentSpec.INITIAL_PETROLEUM_CONSENT_DOCUMENT);

    var result = mockResolver.resolveMergeFields(docSource);

    assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of(
        MailMergeFieldMnem.PROJECT_NAME.name(), MailMergeFieldMnem.PROJECT_NAME.name(),
        MailMergeFieldMnem.PROPOSED_START_OF_WORKS_DATE.name(), MailMergeFieldMnem.PROPOSED_START_OF_WORKS_DATE.name()
    ));

  }

}
