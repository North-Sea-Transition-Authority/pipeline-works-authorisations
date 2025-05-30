package uk.co.ogauthority.pwa.features.application.summary;

import static java.util.stream.Collectors.groupingBy;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.appsummary.ApplicationSummaryView;
import uk.co.ogauthority.pwa.model.view.appsummary.VisibleApplicationVersionOptionsForUser;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.rendering.TemplateRenderingService;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.util.DateUtils;

@Service
public class ApplicationSummaryViewService {

  private final ApplicationSummaryService applicationSummaryService;
  private final TemplateRenderingService templateRenderingService;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final TeamQueryService teamQueryService;

  @Autowired
  public ApplicationSummaryViewService(ApplicationSummaryService applicationSummaryService,
                                       TemplateRenderingService templateRenderingService,
                                       PwaApplicationDetailService pwaApplicationDetailService,
                                       TeamQueryService teamQueryService) {
    this.applicationSummaryService = applicationSummaryService;
    this.templateRenderingService = templateRenderingService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.teamQueryService = teamQueryService;
  }

  public ApplicationSummaryView getApplicationSummaryView(PwaApplicationDetail pwaApplicationDetail) {

    var summarisedSections = applicationSummaryService.summarise(pwaApplicationDetail);

    String combinedRenderedSummaryHtml = summarisedSections.stream()
        .map(summary -> templateRenderingService.render(summary.getTemplatePath(), summary.getTemplateModel(), true))
        .collect(Collectors.joining());

    List<SidebarSectionLink> sidebarSectionLinks = summarisedSections.stream()
        .flatMap(o -> o.getSidebarSectionLinks().stream())
        .collect(Collectors.toList());

    return new ApplicationSummaryView(combinedRenderedSummaryHtml, sidebarSectionLinks);

  }


  public ApplicationSummaryView getApplicationSummaryViewForAppDetailId(Integer appDetailId) {
    var pwaApplicationDetail = pwaApplicationDetailService.getDetailByDetailId(appDetailId);
    return getApplicationSummaryView(pwaApplicationDetail);
  }



  private String createAppDetailVersionOption(PwaApplicationDetail appDetail, Integer order) {
    var orderTagDisplay = order != null ? String.format(" (%s)", order) : "";
    return DateUtils.formatDate(appDetail.getSubmittedTimestamp()) + orderTagDisplay;
  }

  public VisibleApplicationVersionOptionsForUser getVisibleApplicationVersionOptionsForUser(PwaApplication pwaApplication,
                                                                                            AuthenticatedUserAccount user) {

    // TODO we should have a core authorisation method that could tell us the versions any given person
    // can see on an application given their involvement.
    var applicationDetails = pwaApplicationDetailService.getAllDetailsForApplication(pwaApplication)
        .stream().filter(detail -> !ApplicationState.INDUSTRY_EDITABLE.includes(detail.getStatus()))
        .collect(Collectors.toList());

    if (teamQueryService.userIsMemberOfAnyScopedTeamOfType((long) user.getWuaId(), TeamType.CONSULTEE)) {
      applicationDetails = applicationDetails.stream()
          .filter(pwaApplicationDetail -> pwaApplicationDetail.getConfirmedSatisfactoryTimestamp() != null)
          .toList();
    }

    //group all the details by the day they were submitted (for easier order tagging of updates made on the same day)
    var dateToAppDetailsMap = applicationDetails.stream()
        .sorted(Comparator.comparing(PwaApplicationDetail::getSubmittedTimestamp).reversed())
        .collect(groupingBy(appDetail ->
            DateUtils.instantToLocalDate(appDetail.getSubmittedTimestamp()), LinkedHashMap::new, Collectors.toList()));

    Map<String, String> detailIdToOptionMap = new LinkedHashMap<>();
    dateToAppDetailsMap.forEach((submittedDate, appDetailsForDate) -> {
      //this list of app details are already ordered from newest
      for (var x  = 0; x < appDetailsForDate.size(); x++) {
        var appDetail = appDetailsForDate.get(x);
        var appDetailOrderTagNumber = appDetailsForDate.size() > 1 ? appDetailsForDate.size() - x : null;
        detailIdToOptionMap.put(
            appDetail.getId().toString(), createAppDetailVersionOption(appDetail, appDetailOrderTagNumber));
      }
    });

    var latestAppDetailVersionEntryOpt = detailIdToOptionMap.entrySet().stream().findFirst();
    if (latestAppDetailVersionEntryOpt.isPresent()) {
      var latestAppDetailVersionEntry = latestAppDetailVersionEntryOpt.get();
      latestAppDetailVersionEntry.setValue(String.format("Latest version (%s)", latestAppDetailVersionEntry.getValue()));
    }

    return new VisibleApplicationVersionOptionsForUser(detailIdToOptionMap);
  }







}
