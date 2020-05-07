package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineHeaderForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadPipelineRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.util.CoordinateUtils;

@Service
public class PadPipelinesService implements ApplicationFormSectionService {

  private final PadPipelineRepository padPipelineRepository;

  @Autowired
  public PadPipelinesService(PadPipelineRepository padPipelineRepository) {
    this.padPipelineRepository = padPipelineRepository;
  }

  @Transactional
  public void addPipeline(PwaApplicationDetail pwaApplicationDetail, PipelineHeaderForm form) {
    var newPipeline = new PadPipeline(pwaApplicationDetail);
    saveEntityUsingForm(newPipeline, form);
  }

  public void saveEntityUsingForm(PadPipeline padPipeline, PipelineHeaderForm form) {

    padPipeline.setPipelineType(form.getPipelineType());

    padPipeline.setFromLocation(form.getFromLocation());
    padPipeline.setFromCoordinates(CoordinateUtils.coordinatePairFromForm(form.getFromCoordinateForm()));
    padPipeline.setToLocation(form.getToLocation());
    padPipeline.setToCoordinates(CoordinateUtils.coordinatePairFromForm(form.getToCoordinateForm()));

    padPipeline.setComponentPartsDescription(form.getComponentPartsDescription());
    padPipeline.setLength(form.getLength());
    padPipeline.setProductsToBeConveyed(form.getProductsToBeConveyed());
    padPipeline.setTrenchedBuriedBackfilled(form.getTrenchedBuriedBackfilled());

    if (form.getTrenchedBuriedBackfilled()) {
      padPipeline.setTrenchingMethodsDescription(form.getTrenchingMethods());
    }

    padPipelineRepository.save(padPipeline);
  }

  public PadPipeline getById(Integer padPipelineId) {
    return padPipelineRepository.findById(padPipelineId)
        .orElseThrow(() -> new PwaEntityNotFoundException(String.format("Couldn't find PadPipeline with ID: %s", padPipelineId)));
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
}
