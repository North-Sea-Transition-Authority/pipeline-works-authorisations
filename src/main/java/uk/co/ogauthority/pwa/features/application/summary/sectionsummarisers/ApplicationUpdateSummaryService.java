package uk.co.ogauthority.pwa.features.application.summary.sectionsummarisers;

import com.google.common.annotations.VisibleForTesting;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.ApplicationSummarisationException;
import uk.co.ogauthority.pwa.features.application.summary.ApplicationSectionSummariser;
import uk.co.ogauthority.pwa.features.application.summary.ApplicationSectionSummary;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.appprocessing.applicationupdates.ApplicationUpdateSummaryView;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestViewService;

/**
 * Construct summary of application contacts for a given application.
 */
@Service
public class ApplicationUpdateSummaryService implements ApplicationSectionSummariser {

  @VisibleForTesting
  static final String SECTION_NAME = "Previous update request";

  private final ApplicationUpdateRequestViewService applicationUpdateRequestViewService;
  private final PersonService personService;

  @Autowired
  public ApplicationUpdateSummaryService(ApplicationUpdateRequestViewService applicationUpdateRequestViewService,
                                         PersonService personService) {
    this.applicationUpdateRequestViewService = applicationUpdateRequestViewService;
    this.personService = personService;
  }

  @Override
  public boolean canSummarise(PwaApplicationDetail pwaApplicationDetail) {

    return applicationUpdateRequestViewService.getLastRespondedApplicationUpdateView(pwaApplicationDetail).isPresent();
  }

  @Override
  public ApplicationSectionSummary summariseSection(PwaApplicationDetail pwaApplicationDetail,
                                                    String templateName) {

    var view = applicationUpdateRequestViewService.getLastRespondedApplicationUpdateView(pwaApplicationDetail)
        .orElseThrow(() -> new ApplicationSummarisationException(
            "Expected to find app update request view for. pad.id:" + pwaApplicationDetail.getId())
        );

    var responsePerson = personService.getPersonById(view.getResponseByPersonId());

    var summaryView = ApplicationUpdateSummaryView.from(view, responsePerson.getFullName());

    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", SECTION_NAME);
    summaryModel.put("appUpdateSummaryView", summaryView);

    return new ApplicationSectionSummary(
        templateName,
        List.of(SidebarSectionLink.createAnchorLink(
            SECTION_NAME,
            "#previousAppUpdateSection"
        )),
        summaryModel
    );
  }


}
