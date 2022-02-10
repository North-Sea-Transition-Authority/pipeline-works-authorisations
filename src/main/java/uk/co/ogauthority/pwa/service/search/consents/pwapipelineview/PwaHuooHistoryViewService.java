package uk.co.ogauthority.pwa.service.search.consents.pwapipelineview;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.summary.sectionsummarisers.HuooSummaryService;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
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

  /**
   * WARNING: this ensures the same sort order as used during the migration of legacy data from the old pipelines
   * service. This sort order can be found in the get_last_consented_consent_id function in the migration package. As HUOOs
   * were only migrated to the latest consent (according to the migration sort order), it is important that we sort consents
   * in the same way here. If making changes to this sort, ensure that migrated consents are always sorted as below to
   * ensure the correct consent is the head of the migrated consents in the list.
   */
  private static final Comparator<PwaConsent> CONSENTS_DESC = Comparator
      .comparing(PwaConsent::getConsentInstant, Comparator.reverseOrder())
      .thenComparing(PwaConsent::getId, Comparator.reverseOrder());

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

    var selectedOrgRoleSummaryDto = pwaConsentOrganisationRoleService
        .getOrganisationRoleSummaryForConsentsAndPipeline(selectedAndPreviousConsents, pipeline);

    var selectedConsentHuooRolePipelineGroupsView = pwaConsentOrganisationRoleService
        .getAllOrganisationRolePipelineGroupView(masterPwa, selectedOrgRoleSummaryDto);

    // if we have another version to compare to, get a summary for all the previous consents
    // that we can diff the selected one against
    var previousConsents = selectedAndPreviousConsents.stream()
        .filter(c -> !Objects.equals(c, selectedConsent))
        .sorted(CONSENTS_DESC)
        .collect(Collectors.toList());

    var consentsToDiff = previousConsents.isEmpty() ? selectedAndPreviousConsents : previousConsents;

    var previousOrgRoleSummaryDto = pwaConsentOrganisationRoleService
        .getOrganisationRoleSummaryForConsentsAndPipeline(consentsToDiff, pipeline);

    var previousConsentsHuooRolePipelineGroupsView = pwaConsentOrganisationRoleService
        .getAllOrganisationRolePipelineGroupView(masterPwa, previousOrgRoleSummaryDto);

    return huooSummaryService.getDiffedViewUsingSummaryViews(
        selectedConsentHuooRolePipelineGroupsView,
        previousConsentsHuooRolePipelineGroupsView,
        HuooSummaryService.PipelineLabelAction.SHOW_EVERY_PIPELINE_WITHIN_GROUP);

  }

  private List<PwaConsent> getSelectedAndHistoricalConsents(PwaConsent selectedConsent) {

    var allPwaConsents = pwaConsentService.getConsentsByMasterPwa(selectedConsent.getMasterPwa())
        .stream()
        .sorted(CONSENTS_DESC)
        .collect(Collectors.toList());

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
        .sorted(CONSENTS_DESC)
        .collect(Collectors.toList());

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

  /**
   * Get a summary of the migrated HUOO data for a master PWA. This will always be diffed against itself, as
   * the migrated HUOO data was stored against a single consent during the migration process, so there is
   * nothing to diff against. It was not possible to identify which consent each piece of HUOO data was
   * associated with due to the nature of the legacy system data model.
   * @param masterPwa we are getting migrated HUOO data for
   * @param selectedPipelineDetailId the version record for the pipeline we are looking up the data for
   * @return an object containing lists of HUOO data in a format that can be processed by the diff changes macro
   */
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
