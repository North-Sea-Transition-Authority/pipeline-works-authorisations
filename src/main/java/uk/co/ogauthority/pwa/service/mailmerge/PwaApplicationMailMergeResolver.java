package uk.co.ogauthority.pwa.service.mailmerge;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.service.documents.DocumentSource;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListService;

@Service
public class PwaApplicationMailMergeResolver implements DocumentSourceMailMergeResolver {

  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final TaskListService taskListService;
  private final ApplicationContext applicationContext;

  @Autowired
  public PwaApplicationMailMergeResolver(PwaApplicationDetailService pwaApplicationDetailService,
                                         TaskListService taskListService,
                                         ApplicationContext applicationContext) {
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.taskListService = taskListService;
    this.applicationContext = applicationContext;
  }

  @Override
  public boolean supportsDocumentSource(DocumentSource documentSource) {
    return documentSource.getClass().equals(PwaApplication.class);
  }

  @Override
  public List<MailMergeFieldMnem> getAvailableMailMergeFields(DocumentSource documentSource) {

    var app = (PwaApplication) documentSource;
    var detail = pwaApplicationDetailService.getLatestSubmittedDetail(app)
        .orElseThrow(() -> new PwaEntityNotFoundException(String.format("No submitted detail for app with id [%s]", app.getId())));

    return taskListService.getShownApplicationTasksForDetail(detail).stream()
        .map(task -> applicationContext.getBean(task.getServiceClass()))
        .flatMap(taskService -> taskService.getAvailableMailMergeFields(detail).stream())
        .collect(Collectors.toList());

  }

  @Override
  public Map<String, String> resolveMergeFields(DocumentSource documentSource) {

    var app = (PwaApplication) documentSource;
    var detail = pwaApplicationDetailService.getLatestSubmittedDetail(app)
        .orElseThrow(() -> new PwaEntityNotFoundException(String.format("No submitted detail for app with id [%s]", app.getId())));

    return taskListService.getShownApplicationTasksForDetail(detail).stream()
        .map(task -> applicationContext.getBean(task.getServiceClass()))
        .flatMap(taskService -> taskService.resolveMailMergeFields(detail).entrySet().stream())
        .collect(Collectors.toMap(entry -> entry.getKey().name(), Map.Entry::getValue));

  }
}
