package uk.co.ogauthority.pwa.service.workarea.consultations;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.controller.appprocessing.CaseManagementController;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupTeamMember;
import uk.co.ogauthority.pwa.mvc.PageView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.appprocessing.tabs.AppProcessingTab;
import uk.co.ogauthority.pwa.service.consultations.search.ConsultationRequestSearchItem;
import uk.co.ogauthority.pwa.service.consultations.search.ConsultationRequestSearcher;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaTab;
import uk.co.ogauthority.pwa.util.WorkAreaUtils;

@Service
public class ConsultationWorkAreaPageService {

  private final ConsultationRequestSearcher consultationRequestSearcher;
  private final ConsulteeGroupTeamService consulteeGroupTeamService;

  @Autowired
  public ConsultationWorkAreaPageService(ConsultationRequestSearcher consultationRequestSearcher,
                                         ConsulteeGroupTeamService consulteeGroupTeamService) {
    this.consultationRequestSearcher = consultationRequestSearcher;
    this.consulteeGroupTeamService = consulteeGroupTeamService;
  }

  public PageView<ConsultationRequestWorkAreaItem> getPageView(AuthenticatedUserAccount authenticatedUserAccount,
                                                               Set<Integer> consultationRequestIds,
                                                               int page) {

    var workAreaUri = ReverseRouter.route(on(WorkAreaController.class).renderWorkAreaTab(null, WorkAreaTab.OPEN_CONSULTATIONS, page));

    return PageView.fromPage(
        getConsultationSearchResults(authenticatedUserAccount, consultationRequestIds, page),
        workAreaUri,
        sr -> new ConsultationRequestWorkAreaItem(sr, this::consultationUrlProducer)
    );

  }

  private Page<ConsultationRequestSearchItem> getConsultationSearchResults(WebUserAccount userAccount,
                                                                           Set<Integer> consultationRequestIdList,
                                                                           int pageRequest) {

    Optional<ConsulteeGroupTeamMember> userConsulteeGroupMembership = consulteeGroupTeamService
        .getTeamMemberByPerson(userAccount.getLinkedPerson());

    Integer consulteeGroupIdToGetAllocationRequestsFor = userConsulteeGroupMembership
        .filter(member -> member.getRoles().contains(ConsulteeGroupMemberRole.RECIPIENT))
        .map(member -> member.getConsulteeGroup().getId())
        .orElse(null);

    return consultationRequestSearcher.searchByStatusForGroupIdsOrConsultationRequestIds(
        WorkAreaUtils.getWorkAreaPageRequest(pageRequest, ConsultationWorkAreaSort.DEADLINE_DATE_ASC),
        ConsultationRequestStatus.ALLOCATION,
        consulteeGroupIdToGetAllocationRequestsFor,
        consultationRequestIdList
    );

  }

  private String consultationUrlProducer(ConsultationRequestSearchItem consultationRequestSearchItem) {

    return ReverseRouter.route(on(CaseManagementController.class).renderCaseManagement(
        consultationRequestSearchItem.getApplicationDetailSearchItem().getPwaApplicationId(),
        consultationRequestSearchItem.getApplicationDetailSearchItem().getApplicationType(),
        AppProcessingTab.TASKS,
        null,
        null
    ));

  }

}
