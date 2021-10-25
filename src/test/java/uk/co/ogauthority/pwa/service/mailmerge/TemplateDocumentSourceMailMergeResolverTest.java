package uk.co.ogauthority.pwa.service.mailmerge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem;
import uk.co.ogauthority.pwa.service.documents.templates.TemplateDocumentSource;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation.PadProjectInformationService;

@RunWith(MockitoJUnitRunner.class)
public class TemplateDocumentSourceMailMergeResolverTest {

  @Mock
  private TaskListService taskListService;

  @Mock
  private ApplicationContext applicationContext;

  @Mock
  private PadProjectInformationService padProjectInformationService;

  private TemplateDocumentSourceMailMergeResolver templateDocumentSourceMailMergeResolver;

  private List<MailMergeFieldMnem> mailMergeFields;

  @Before
  public void setUp() throws Exception {

    templateDocumentSourceMailMergeResolver = new TemplateDocumentSourceMailMergeResolver(taskListService, applicationContext);

    mailMergeFields = Arrays.stream(MailMergeFieldMnem.values())
        .collect(Collectors.toList());

  }

  @Test
  public void supportsDocumentSource_templateDocSource_true() {

    var source = new TemplateDocumentSource(DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT);

    boolean supported = templateDocumentSourceMailMergeResolver.supportsDocumentSource(source);

    assertThat(supported).isTrue();

  }

  @Test
  public void getAvailableMailMergeFields() {

    var source = new TemplateDocumentSource(DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT);

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
  public void resolveMergeFields() {

    var mockResolver = mock(TemplateDocumentSourceMailMergeResolver.class);

    when(mockResolver.getAvailableMailMergeFields(any())).thenReturn(List.of(MailMergeFieldMnem.PROJECT_NAME, MailMergeFieldMnem.PROPOSED_START_OF_WORKS_DATE));

    when(mockResolver.resolveMergeFields(any())).thenCallRealMethod();

    var docSource = new TemplateDocumentSource(DocumentSpec.INITIAL_APP_CONSENT_DOCUMENT);

    var result = mockResolver.resolveMergeFields(docSource);

    assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of(
        MailMergeFieldMnem.PROJECT_NAME.name(), MailMergeFieldMnem.PROJECT_NAME.name(),
        MailMergeFieldMnem.PROPOSED_START_OF_WORKS_DATE.name(), MailMergeFieldMnem.PROPOSED_START_OF_WORKS_DATE.name()
    ));

  }

}