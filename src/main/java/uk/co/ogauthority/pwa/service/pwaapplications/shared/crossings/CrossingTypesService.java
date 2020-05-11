package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings;

import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.tasklist.TagColour;
import uk.co.ogauthority.pwa.model.tasklist.TaskListLabel;
import uk.co.ogauthority.pwa.model.tasklist.TaskListSection;

@Service
public class CrossingTypesService implements TaskListSection {
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
  public boolean getShowNotCompletedLabel() {
    return true;
  }

  @Override
  public List<TaskListLabel> getTaskListLabels(PwaApplicationDetail pwaApplicationDetail) {
    if (pwaApplicationDetail.getCablesCrossed() == null
        || pwaApplicationDetail.getMedianLineCrossed() == null
        || pwaApplicationDetail.getPipelinesCrossed() == null) {
      return List.of(
          new TaskListLabel("Not completed", TagColour.RED));
    }
    return List.of();
  }
}
