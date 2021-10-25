package uk.co.ogauthority.pwa.service.search.consents.pwapipelineview;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.HuooSummaryService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.DiffedAllOrgRolePipelineGroups;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailMigrationHuooDataService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
import uk.co.ogauthority.pwa.util.DateUtils;

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

  /**
   * WARNING: this method ensures the same sort order as used during the migration of legacy data from the old pipelines
   * service. This sort order can be found in the get_last_consented_consent_id function in the migration package. As HUOOs
   * were only migrated to the latest consent (according to the migration sort order), it is important that we sort consents
   * in the same way here. If making changes to this sort, ensure that migrated consents are always sorted as below to
   * ensure the correct consent is the head of the migrated consents in the list.
   */
  private void sortPwaConsentsLatestFirst(List<PwaConsent> consents) {

    consents.sort(Comparator.comparing(PwaConsent::getConsentInstant, Comparator.reverseOrder())
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
  List<PwaConsent> getAllNonMigratedConsentsPlusLatestMigratedOnOrAfterFirstConsentOfPipeline(MasterPwa masterPwa, PipelineId pipelineId) {

    var firstDetailForPipe = pipelineDetailService.getFirstConsentedPipelineDetail(pipelineId);
    var datePipelineStarted = DateUtils.instantToLocalDate(firstDetailForPipe.getStartTimestamp());

    // ensure we don't look at any consents from before the pipeline history starts
    var consentsAfterPipeStart = pwaConsentService.getConsentsByMasterPwa(masterPwa)
        .stream()
        .filter(consent -> DateUtils.isOnOrAfter(DateUtils.instantToLocalDate(consent.getConsentInstant()), datePipelineStarted))
        .collect(Collectors.toList());

    sortPwaConsentsLatestFirst(consentsAfterPipeStart);

    var filteredConsents = new ArrayList<PwaConsent>();

    var latestMigratedConsentFound = false;
    for (var pwaConsent : consentsAfterPipeStart) {

      if (!pwaConsent.isMigratedFlag() || !latestMigratedConsentFound) {
        filteredConsents.add(pwaConsent);
      }

      if (pwaConsent.isMigratedFlag()) {
        latestMigratedConsentFound = true;
      }

    }

    return filteredConsents;

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
