package uk.co.ogauthority.pwa.service.mailmerge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem;
import uk.co.ogauthority.pwa.service.documents.DocumentSource;
import uk.co.ogauthority.pwa.service.documents.templates.TemplateDocumentSource;

@Service
public class TemplateDocumentSourceMailMergeResolver implements DocumentSourceMailMergeResolver {

  private final TaskListService taskListService;
  private final ApplicationContext applicationContext;

  @Autowired
  public TemplateDocumentSourceMailMergeResolver(TaskListService taskListService,
                                                 ApplicationContext applicationContext) {
    this.taskListService = taskListService;
    this.applicationContext = applicationContext;
  }

  @Override
  public boolean supportsDocumentSource(DocumentSource documentSource) {
    return documentSource.getClass().equals(TemplateDocumentSource.class);
  }

  @Override
  public List<MailMergeFieldMnem> getAvailableMailMergeFields(DocumentSource documentSource) {

    var templateDocumentSource = (TemplateDocumentSource) documentSource;

    Map<PwaApplicationType, List<ApplicationTask>> appTypeToAppTasksMap = new HashMap<>();

    templateDocumentSource.getDocumentSpec()
        .getApplicationType()
        .forEach(pwaApplicationType -> appTypeToAppTasksMap.put(
            pwaApplicationType,
            taskListService.getApplicationTasksForAppType(pwaApplicationType)));

    var mailMergeMnemSet = new HashSet<MailMergeFieldMnem>();

    appTypeToAppTasksMap.forEach(((pwaApplicationType, applicationTasks) -> {

      applicationTasks.forEach(applicationTask -> {

        var mergeFields = applicationContext.getBean(applicationTask.getServiceClass())
            .getAvailableMailMergeFields(pwaApplicationType);

        mailMergeMnemSet.addAll(mergeFields);

      });

    }));

    return List.copyOf(mailMergeMnemSet);

  }

  @Override
  public Map<String, String> resolveMergeFields(DocumentSource documentSource) {

    var availableMergeFields = getAvailableMailMergeFields(documentSource);

    return availableMergeFields.stream()
        .collect(Collectors.toMap(MailMergeFieldMnem::name, MailMergeFieldMnem::name));

  }

}
