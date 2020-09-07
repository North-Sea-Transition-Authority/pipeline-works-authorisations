package uk.co.ogauthority.pwa.service.appprocessing;

import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.AppProcessingService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;

@Service
public class PublicNoticeService implements AppProcessingService {

  @Override
  public boolean canShowInTaskList(PwaAppProcessingContext processingContext) {
    return true;
  }

  @Override
  public Optional<TaskStatus> getTaskStatus(PwaAppProcessingContext processingContext) {
    return Optional.empty();
  }
}
