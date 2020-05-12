package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.tasklist.TaskListLabel;
import uk.co.ogauthority.pwa.model.tasklist.TaskListSection;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;

@Service
public class CrossingTypesService implements ApplicationFormSectionService, TaskListSection {
  @Override
  public boolean isTaskListEntryCompleted(PwaApplicationDetail pwaApplicationDetail) {
    return pwaApplicationDetail.getPipelinesCrossed() != null
        && pwaApplicationDetail.getCablesCrossed() != null
        && pwaApplicationDetail.getMedianLineCrossed() != null;
  }

  @Override
  public boolean getCanShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    return true;
  }

  @Override
  public List<TaskListLabel> getTaskListLabels(PwaApplicationDetail pwaApplicationDetail) {
    return List.of();
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    return isTaskListEntryCompleted(detail);
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    throw new ActionNotAllowedException("This service shouldn't be validated");
  }
}
