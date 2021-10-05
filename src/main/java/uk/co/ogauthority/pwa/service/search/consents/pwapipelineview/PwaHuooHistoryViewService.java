package uk.co.ogauthority.pwa.service.search.consents.pwapipelineview;

import static java.util.Comparator.nullsLast;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.HuooSummaryService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.DiffedAllOrgRolePipelineGroups;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailMigrationHuooDataService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;

@Service
public class PwaHuooHistoryViewService {

  private final HuooSummaryService huooSummaryService;
  private final PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService;
  private final PipelineDetailMigrationHuooDataService pipelineDetailMigrationHuooDataService;
  private final PwaConsentService pwaConsentService;
  private final PipelineService pipelineService;
  private final PipelineDetailService pipelineDetailService;


  @Autowired
  public PwaHuooHistoryViewService(HuooSummaryService huooSummaryService,
                                   PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService,
                                   PipelineDetailMigrationHuooDataService pipelineDetailMigrationHuooDataService,
                                   PwaConsentService pwaConsentService,
                                   PipelineService pipelineService,
                                   PipelineDetailService pipelineDetailService) {
    this.huooSummaryService = huooSummaryService;
    this.pwaConsentOrganisationRoleService = pwaConsentOrganisationRoleService;
    this.pipelineDetailMigrationHuooDataService = pipelineDetailMigrationHuooDataService;
    this.pwaConsentService = pwaConsentService;
    this.pipelineService = pipelineService;
    this.pipelineDetailService = pipelineDetailService;
  }


  public DiffedAllOrgRolePipelineGroups getDiffedHuooSummaryAtTimeOfConsentAndPipeline(Integer selectedConsentId,
                                                                                       MasterPwa masterPwa,
                                                                                       PipelineId pipelineId) {

    var selectedConsent = pwaConsentService.getConsentById(selectedConsentId);
    var selectedAndPreviousConsents = getSelectedAndHistoricalConsents(selectedConsent);

    var pipeline = pipelineService.getPipelineFromId(pipelineId);
    var orgRoleSummaryDto = pwaConsentOrganisationRoleService.getOrganisationRoleSummaryForConsentsAndPipeline(
        selectedAndPreviousConsents, pipeline);
    var huooRolePipelineGroupsView = pwaConsentOrganisationRoleService.getAllOrganisationRolePipelineGroupView(
        masterPwa, orgRoleSummaryDto);

    return huooSummaryService.getDiffedViewUsingSummaryViews(huooRolePipelineGroupsView, huooRolePipelineGroupsView,
        HuooSummaryService.PipelineLabelAction.SHOW_EVERY_PIPELINE_WITHIN_GROUP);
  }

  private List<PwaConsent> getSelectedAndHistoricalConsents(PwaConsent selectedConsent) {

    var allPwaConsents = pwaConsentService.getConsentsByMasterPwa(selectedConsent.getMasterPwa());
    sortPwaConsentsLatestFirst(allPwaConsents);

    for (var x = 0; x < allPwaConsents.size(); x++) {
      if (allPwaConsents.get(x).equals(selectedConsent)) {
        return allPwaConsents.subList(x, allPwaConsents.size());
      }
    }

    throw new PwaEntityNotFoundException(String.format(
        "The selected consent with id: %s was not found in any of the consents for master pwa with id: %s",
        selectedConsent.getId(), selectedConsent.getMasterPwa().getId()));
  }


  private void sortPwaConsentsLatestFirst(List<PwaConsent> consents) {

    consents.sort(Comparator.comparing(PwaConsent::getConsentInstant, Comparator.reverseOrder())
        .thenComparing(PwaConsent::getVariationNumber, nullsLast(Comparator.reverseOrder()))
        .thenComparing(PwaConsent::getReference, Comparator.reverseOrder())
        .thenComparing(PwaConsent::getId, Comparator.reverseOrder()));
  }



  /**
   * The purpose of this method is to identify the earliest consent based on the first pipeline detail record that exists
   *   and only return consents after and including that one. Also to exclude any migrated consent apart from the "latest" migrated consent.
   *   This ensures we only show consents that have HUOO data.
   * @param masterPwa The Master Pwa used to get all consents to then be filtered down
   * @param pipelineId The Pipeline ID used to identify the relevant consents for this pipeline
   * @return A list of sorted Pwa Consents containing all those from the latest down to the first consent
   *        where the pipeline was created excluding all but the latest migrated consent.
   */
  List<PwaConsent> getAllConsentsOnOrAfterFirstConsentOfPipeline(MasterPwa masterPwa, PipelineId pipelineId) {

    var allPwaConsents = pwaConsentService.getConsentsByMasterPwa(masterPwa);
    sortPwaConsentsLatestFirst(allPwaConsents);

    var pipelineDetailFirstVersion = pipelineDetailService.getFirstConsentedPipelineDetail(pipelineId);

    var filteredConsents = new ArrayList<PwaConsent>();

    var latestMigratedConsentFound = false;
    for (var pwaConsent : allPwaConsents) {

      if (!pwaConsent.isMigratedFlag() || !latestMigratedConsentFound) {
        filteredConsents.add(pwaConsent);
      }

      if (pwaConsent.isMigratedFlag()) {
        latestMigratedConsentFound = true;
      }

      if (pwaConsent.equals(pipelineDetailFirstVersion.getPwaConsent())) {
        return filteredConsents;
      }
    }

    throw new PwaEntityNotFoundException(String.format(
        "The consent for pipeline id: %s was not found in any of the consents for master pwa with id: %s",
        pipelineId.asInt(), masterPwa.getId()));
  }



  public DiffedAllOrgRolePipelineGroups getOrganisationRoleSummaryForHuooMigratedData(MasterPwa masterPwa,
                                                                                      Integer selectedPipelineDetailId) {

    var selectedPipelineDetail = pipelineDetailService.getByPipelineDetailId(selectedPipelineDetailId);
    var orgRoleSummaryDto = pipelineDetailMigrationHuooDataService.getOrganisationRoleSummaryForHuooMigratedData(selectedPipelineDetail);
    var huooRolePipelineGroupsView = pwaConsentOrganisationRoleService.getAllOrganisationRolePipelineGroupView(
        masterPwa, orgRoleSummaryDto);

    return huooSummaryService.getDiffedViewUsingSummaryViews(huooRolePipelineGroupsView, huooRolePipelineGroupsView,
        HuooSummaryService.PipelineLabelAction.SHOW_EVERY_PIPELINE_WITHIN_GROUP);
  }


}
