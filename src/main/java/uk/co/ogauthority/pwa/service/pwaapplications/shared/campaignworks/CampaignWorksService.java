package uk.co.ogauthority.pwa.service.pwaapplications.shared.campaignworks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation.PadProjectInformationService;

@Service
public class CampaignWorksService implements ApplicationFormSectionService {

  private final PadProjectInformationService padProjectInformationService;
  private final PadPipelineService padPipelineService;

  @Autowired
  public CampaignWorksService(
      PadProjectInformationService padProjectInformationService,
      PadPipelineService padPipelineService) {
    this.padProjectInformationService = padProjectInformationService;
    this.padPipelineService = padPipelineService;
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    // TODO PWA-372 do validation
    return false;
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    // TODO PWA-372 if project information says campaign works should be used,
    // then we need to check at least one schedule and that every schedule has at least one pad pipeline AND valid schedule dates
    return bindingResult;
  }

  @Override
  public boolean canShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    return padProjectInformationService.isCampaignApproachBeingUsed(pwaApplicationDetail)
        && padPipelineService.totalPipelineContainedInApplication(pwaApplicationDetail) > 0L;
  }

}
