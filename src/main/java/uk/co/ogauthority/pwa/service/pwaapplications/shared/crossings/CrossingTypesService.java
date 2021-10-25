package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings;

import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.CrossingTypesForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline.CrossingTypesView;

@Service
public class CrossingTypesService implements ApplicationFormSectionService {

  public void mapApplicationDetailToForm(PwaApplicationDetail detail, CrossingTypesForm form) {
    form.setMedianLineCrossed(detail.getMedianLineCrossed());
    form.setPipelinesCrossed(detail.getPipelinesCrossed());
    form.setCablesCrossed(detail.getCablesCrossed());
  }

  public CrossingTypesView getCrossingTypesView(PwaApplicationDetail detail) {
    return new CrossingTypesView(
      detail.getPipelinesCrossed(),
      detail.getCablesCrossed(),
      detail.getMedianLineCrossed()
    );
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    return detail.getPipelinesCrossed() != null
        && detail.getCablesCrossed() != null
        && detail.getMedianLineCrossed() != null;
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    throw new ActionNotAllowedException("This service shouldn't be validated");
  }

  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {
    // not required to do anything
  }
}
