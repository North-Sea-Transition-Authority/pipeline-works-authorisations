package uk.co.ogauthority.pwa.service.workarea.consultations;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.mvc.PageView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.consultations.search.ConsultationRequestSearchItem;
import uk.co.ogauthority.pwa.service.consultations.search.ConsultationRequestSearcher;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaTab;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.Team;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.util.WorkAreaUtils;

@Service
public class ConsultationWorkAreaPageService {

  private final ConsultationRequestSearcher consultationRequestSearcher;
  private final TeamQueryService teamQueryService;

  @Autowired
  public ConsultationWorkAreaPageService(ConsultationRequestSearcher consultationRequestSearcher,
                                         TeamQueryService teamQueryService) {
    this.consultationRequestSearcher = consultationRequestSearcher;
    this.teamQueryService = teamQueryService;
  }

  public PageView<ConsultationRequestWorkAreaItem> getPageView(AuthenticatedUserAccount authenticatedUserAccount,
                                                               Set<Integer> consultationRequestIds,
                                                               int page) {

    var workAreaUri = ReverseRouter.route(on(WorkAreaController.class)
        .renderWorkAreaTab(null, WorkAreaTab.OPEN_CONSULTATIONS, page, Optional.empty()));

    return PageView.fromPage(
        getConsultationSearchResults(authenticatedUserAccount, consultationRequestIds, page),
        workAreaUri,
        ConsultationRequestWorkAreaItem::new
    );

  }

  private Page<ConsultationRequestSearchItem> getConsultationSearchResults(WebUserAccount userAccount,
                                                                           Set<Integer> consultationRequestIdList,
                                                                           int pageRequest) {

    var consulteeGroupIdToGetAllocationRequestsFor =
        teamQueryService.getTeamsOfTypeUserHasAnyRoleIn(userAccount.getWuaId(), TeamType.CONSULTEE, Set.of(Role.RECIPIENT))
            .stream()
            .findFirst()
            .map(Team::getScopeId)
            .map(Integer::valueOf)
            .orElse(null);

    return consultationRequestSearcher.searchByStatusForGroupIdsOrConsultationRequestIds(
        WorkAreaUtils.getWorkAreaPageRequest(pageRequest, ConsultationWorkAreaSort.DEADLINE_DATE_ASC),
        ConsultationRequestStatus.ALLOCATION,
        consulteeGroupIdToGetAllocationRequestsFor,
        consultationRequestIdList
    );

  }

}
