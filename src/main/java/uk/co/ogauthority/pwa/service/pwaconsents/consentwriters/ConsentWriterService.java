package uk.co.ogauthority.pwa.service.pwaconsents.consentwriters;

import java.util.Comparator;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListService;

@Service
public class ConsentWriterService {

  private final List<ConsentWriter> consentWriters;
  private final TaskListService taskListService;

  @Autowired
  public ConsentWriterService(List<ConsentWriter> consentWriters,
                              TaskListService taskListService) {
    this.consentWriters = consentWriters;
    this.taskListService = taskListService;
  }

  @Transactional
  public void updateConsentedData(PwaApplicationDetail pwaApplicationDetail, PwaConsent pwaConsent) {

    var availableTasks = taskListService.getShownApplicationTasksForDetail(pwaApplicationDetail);

    consentWriters.stream()
        .filter(writer -> availableTasks.contains(writer.getTaskDependentOn()))
        .sorted(Comparator.comparing(ConsentWriter::getExecutionOrder))
        .forEach(writer -> writer.write(pwaApplicationDetail, pwaConsent));

  }

}
