package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo;

import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;

@Service
public class PipelinesHuooService implements ApplicationFormSectionService {

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    return false;
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    return null;
  }
}
