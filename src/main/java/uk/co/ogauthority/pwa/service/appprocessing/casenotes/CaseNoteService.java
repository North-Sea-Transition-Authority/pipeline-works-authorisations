package uk.co.ogauthority.pwa.service.appprocessing.casenotes;

import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.AppProcessingService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;

@Service
public class CaseNoteService implements AppProcessingService {

  @Override
  public boolean canShowInTaskList(PwaAppProcessingContext processingContext) {
    return !processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY);
  }

  @Override
  public Optional<TaskStatus> getTaskStatus(PwaAppProcessingContext processingContext) {
    return Optional.empty();
  }

}
