package uk.co.ogauthority.pwa.service.search.consents.pwapipelineview;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.HuooSummaryService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.DiffedAllOrgRolePipelineGroups;
import uk.co.ogauthority.pwa.service.pwaconsents.PipelineDetailService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentOrganisationRoleService;

@Service
public class PwaHuooHistoryViewService {

  private final HuooSummaryService huooSummaryService;
  private final PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService;
  private final PipelineDetailService pipelineDetailService;


  @Autowired
  public PwaHuooHistoryViewService(HuooSummaryService huooSummaryService,
                                   PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService,
                                   PipelineDetailService pipelineDetailService) {
    this.huooSummaryService = huooSummaryService;
    this.pwaConsentOrganisationRoleService = pwaConsentOrganisationRoleService;
    this.pipelineDetailService = pipelineDetailService;
  }



  public DiffedAllOrgRolePipelineGroups getDiffedHuooSummaryModel(Integer selectedPipelineDetailId,
                                                                  Integer pipelineId, MasterPwa masterPwa) {

    var pipelineDetail = pipelineDetailService.getLatestByPipelineId(pipelineId);
    var orgRoleSummaryDto = pwaConsentOrganisationRoleService.getOrganisationRoleSummary(pipelineDetail);
    var huooRolePipelineGroupsView = pwaConsentOrganisationRoleService.getAllOrganisationRolePipelineGroupView(
        masterPwa, orgRoleSummaryDto);

    return huooSummaryService.getDiffedViewUsingSummaryViews(huooRolePipelineGroupsView, huooRolePipelineGroupsView);
  }


}
