package uk.co.ogauthority.pwa.service.pwaconsents.consentwriters;

import jakarta.transaction.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentType;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines.ConsentWriterDto;

@Service
public class ConsentWriterService {

  private final List<ConsentWriter> consentWriters;
  private final TaskListService taskListService;
  private final HolderChangeEmailService holderChangeEmailService;

  @Autowired
  public ConsentWriterService(List<ConsentWriter> consentWriters,
                              TaskListService taskListService,
                              HolderChangeEmailService holderChangeEmailService) {
    this.consentWriters = consentWriters;
    this.taskListService = taskListService;
    this.holderChangeEmailService = holderChangeEmailService;
  }

  @Transactional
  public void updateConsentedData(PwaApplicationDetail pwaApplicationDetail, PwaConsent pwaConsent) {

    var availableTasks = taskListService.getShownApplicationTasksForDetail(pwaApplicationDetail);

    var consentWriterDto = new ConsentWriterDto();

    var sortedWriters = consentWriters.stream()
        .filter(writer -> writer.writerIsApplicable(availableTasks, pwaConsent))
        .sorted(Comparator.comparing(ConsentWriter::getExecutionOrder))
        .collect(Collectors.toList());

    for (ConsentWriter writer : sortedWriters) {
      consentWriterDto = writer.write(pwaApplicationDetail, pwaConsent, consentWriterDto);
    }

    // don't send holder change emails for new pwas, there was no holder to begin with
    if (pwaConsent.getConsentType() != PwaConsentType.INITIAL_PWA) {

      holderChangeEmailService.sendHolderChangeEmail(
          pwaApplicationDetail.getPwaApplication(),
          consentWriterDto.getConsentRolesEnded(),
          consentWriterDto.getConsentRolesAdded());

    }

  }

}
