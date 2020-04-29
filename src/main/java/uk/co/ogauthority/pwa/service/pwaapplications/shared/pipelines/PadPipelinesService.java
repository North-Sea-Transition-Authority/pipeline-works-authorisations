package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineHeaderForm;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.LatitudeCoordinate;
import uk.co.ogauthority.pwa.model.location.LongitudeCoordinate;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadPipelineRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;

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
    padPipeline.setToLocation(form.getToLocation());
    padPipeline.setComponentPartsDescription(form.getComponentPartsDescription());
    padPipeline.setLength(form.getLength());
    padPipeline.setProductsToBeConveyed(form.getProductsToBeConveyed());
    padPipeline.setTrenchedBuriedBackfilled(form.getTrenchedBuriedBackfilled());

    if (form.getTrenchedBuriedBackfilled()) {
      padPipeline.setTrenchingMethodsDescription(form.getTrenchingMethods());
    }

    padPipeline.setFromCoordinates(new CoordinatePair(
        new LatitudeCoordinate(form.getFromLatDeg(), form.getFromLatMin(), form.getFromLatSec(), form.getFromLatDirection()),
        new LongitudeCoordinate(form.getFromLongDeg(), form.getFromLongMin(), form.getFromLongSec(), form.getFromLongDirection())
    ));

    padPipeline.setToCoordinates(new CoordinatePair(
        new LatitudeCoordinate(form.getToLatDeg(), form.getToLatMin(), form.getToLatSec(), form.getToLatDirection()),
        new LongitudeCoordinate(form.getToLongDeg(), form.getToLongMin(), form.getToLongSec(), form.getToLongDirection())
    ));

    padPipelineRepository.save(padPipeline);
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
