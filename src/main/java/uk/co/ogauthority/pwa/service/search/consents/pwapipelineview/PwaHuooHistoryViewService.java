package uk.co.ogauthority.pwa.service.search.consents.pwapipelineview;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.stream.Collectors.groupingBy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.HuooSummaryService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.DiffedAllOrgRolePipelineGroups;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;
import uk.co.ogauthority.pwa.util.DateUtils;

@Service
public class PwaHuooHistoryViewService {

  private final HuooSummaryService huooSummaryService;
  private final PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService;
  private final PwaConsentService pwaConsentService;
  private final PipelineService pipelineService;


  @Autowired
  public PwaHuooHistoryViewService(HuooSummaryService huooSummaryService,
                                   PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService,
                                   PwaConsentService pwaConsentService,
                                   PipelineService pipelineService) {
    this.huooSummaryService = huooSummaryService;
    this.pwaConsentOrganisationRoleService = pwaConsentOrganisationRoleService;
    this.pwaConsentService = pwaConsentService;
    this.pipelineService = pipelineService;
  }



  private String createConsentVersionOption(PwaConsent pwaConsent, Integer order) {

    var orderTagDisplay = order != null ? String.format(" (%s)", order) : "";
    var consentReferenceDisplay = pwaConsent.getReference() != null
        ? " - " + pwaConsent.getReference() : "";
    return DateUtils.formatDate(pwaConsent.getConsentInstant()) + orderTagDisplay + consentReferenceDisplay;
  }

  public Map<String, String> getConsentHistorySearchSelectorItems(MasterPwa masterPwa) {

    var consents = pwaConsentService.getConsentsByMasterPwa(masterPwa);
    //group all the consents by the day they were created (for easier order tagging of consents changed on the same day)
    var dateToConsentsMap = consents.stream()
        .sorted(Comparator.comparing(PwaConsent::getConsentInstant).reversed())
        .collect(groupingBy(consent ->
            DateUtils.instantToLocalDate(consent.getConsentInstant()), LinkedHashMap::new, Collectors.toList()));

    Map<String, String> consentIdToOptionMap = new LinkedHashMap<>();
    dateToConsentsMap.forEach((consentTimestamp, consentsForDate) -> {
      //this list of consents are already ordered from newest, just need to sort by variation number for versions on the same day
      consentsForDate.sort(nullsLast(Comparator.comparing(PwaConsent::getVariationNumber, nullsLast(naturalOrder())).reversed()));
      for (var x  = 0; x < consentsForDate.size(); x++) {
        var consent = consentsForDate.get(x);
        var consentOrderTagNumber = consentsForDate.size() > 1 ? consentsForDate.size() - x : null;
        consentIdToOptionMap.put(
            String.valueOf(consent.getId()), createConsentVersionOption(consent, consentOrderTagNumber));
      }
    });

    var latestConsentVersionEntryOpt = consentIdToOptionMap.entrySet().stream().findFirst();
    if (latestConsentVersionEntryOpt.isPresent()) {
      var latestConsentVersionEntry = latestConsentVersionEntryOpt.get();
      latestConsentVersionEntry.setValue(String.format("Latest version (%s)", latestConsentVersionEntry.getValue()));
    }

    return consentIdToOptionMap;
  }



  public DiffedAllOrgRolePipelineGroups getDiffedHuooSummaryAtTimeOfConsentAndPipeline(Integer selectedConsentId,
                                                                                       MasterPwa masterPwa,
                                                                                       PipelineId pipelineId) {

    var selectedConsent = pwaConsentService.getConsentsById(selectedConsentId);
    var selectedAndPreviousConsents = new ArrayList<PwaConsent>();
    selectedAndPreviousConsents.add(selectedConsent);
    selectedAndPreviousConsents.addAll(
        //need to make sure we get on as well as before for consents that do not include time on the consent instance
        pwaConsentService.getPwaConsentsWhereConsentInstantBefore(masterPwa, selectedConsent.getConsentInstant()));

    var pipeline = pipelineService.getPipelineFromId(pipelineId);
    var orgRoleSummaryDto = pwaConsentOrganisationRoleService.getOrganisationRoleSummaryForConsentsAndPipeline(
        selectedAndPreviousConsents, pipeline);
    var huooRolePipelineGroupsView = pwaConsentOrganisationRoleService.getAllOrganisationRolePipelineGroupView(
        masterPwa, orgRoleSummaryDto);

    return huooSummaryService.getDiffedViewUsingSummaryViews(huooRolePipelineGroupsView, huooRolePipelineGroupsView);
  }


}
