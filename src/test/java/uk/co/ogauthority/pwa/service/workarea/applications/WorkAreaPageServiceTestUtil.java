package uk.co.ogauthority.pwa.service.workarea.applications;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.WorkAreaApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.model.workflow.GenericWorkflowSubject;
import uk.co.ogauthority.pwa.service.enums.workflow.UserWorkflowTask;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;

public class WorkAreaPageServiceTestUtil {

  public static Pageable getWorkAreaViewPageable(int requestedPage, String sortProperty) {
    return PageRequest.of(requestedPage, WorkAreaService.PAGE_SIZE,
        Sort.by(Sort.Order.asc(sortProperty).nullsLast()));
  }

  public static Pageable getWorkAreaViewPageable(int requestedPage, ApplicationWorkAreaSort applicationWorkAreaSort) {
    return PageRequest.of(requestedPage, WorkAreaService.PAGE_SIZE,
        applicationWorkAreaSort.getSort());
  }


  public static Pageable getProposedStartAscNullsFirstPageRequest(int requestedPage) {
    return PageRequest.of(requestedPage, WorkAreaService.PAGE_SIZE,
        Sort.by(Sort.Direction.ASC, "padProposedStart"));
  }

  public static Page<WorkAreaApplicationDetailSearchItem> getFakeWorkAreaSearchItemPage(
      List<WorkAreaApplicationDetailSearchItem> results, int page) {

    return new PageImpl<>(
        results,
        getWorkAreaViewPageable(page, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        results.size());

  }

  public static WorkflowTaskInstance getAppWorkflowTaskInstance(Integer businessKey, UserWorkflowTask task) {
    return new WorkflowTaskInstance(new GenericWorkflowSubject(businessKey, WorkflowType.PWA_APPLICATION), task);
  }


}
