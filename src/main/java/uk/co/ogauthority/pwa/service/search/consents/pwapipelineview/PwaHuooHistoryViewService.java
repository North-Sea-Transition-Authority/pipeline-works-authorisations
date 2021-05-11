package uk.co.ogauthority.pwa.service.search.consents.pwapipelineview;

import java.util.ArrayList;
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


  public DiffedAllOrgRolePipelineGroups getOrganisationRoleSummaryForHuooMigratedData(MasterPwa masterPwa,
                                                                                      Integer selectedPipelineDetailId) {

    var selectedPipelineDetail = pipelineDetailService.getByPipelineDetailId(selectedPipelineDetailId);
    var orgRoleSummaryDto = pipelineDetailMigrationHuooDataService.getOrganisationRoleSummaryForHuooMigratedData(selectedPipelineDetail);
    var huooRolePipelineGroupsView = pwaConsentOrganisationRoleService.getAllOrganisationRolePipelineGroupView(
        masterPwa, orgRoleSummaryDto);

    return huooSummaryService.getDiffedViewUsingSummaryViews(huooRolePipelineGroupsView, huooRolePipelineGroupsView);
  }


}
