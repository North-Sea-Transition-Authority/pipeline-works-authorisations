package uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea;

import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;

@Service
public class CarbonStorageAreaCrossingService implements ApplicationFormSectionService {
  @Override
  public boolean canShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    return  pwaApplicationDetail.getResourceType().equals(PwaResourceType.CCUS) || pwaApplicationDetail.getCsaCrossed();
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    return false;
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    return null;
  }

  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {

  }
}
