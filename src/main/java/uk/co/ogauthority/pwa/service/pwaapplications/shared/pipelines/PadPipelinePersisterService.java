package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import java.math.BigDecimal;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineCoreType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineIdentRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineRepository;

/**
 * The purpose of this class is to perform the required operations on ident data whenever a pipeline or ident has been added/modified/removed prior to saving the pipeline.
 * The functionality is all contained within this class so that a pipeline cannot be persisted without having performed the operations first.
 * This also avoids the PadPipelineService and the PadPipelineIdentsService both depending on each other.
  */
@Service
public class PadPipelinePersisterService {

  private final PadPipelineRepository padPipelineRepository;

  public PadPipelinePersisterService(
      PadPipelineRepository padPipelineRepository) {
    this.padPipelineRepository = padPipelineRepository;
  }

  @Transactional
  public void savePadPipelineAndMaterialiseIdentData(PadPipeline padPipeline, List<IdentView> identViews) {
    setMaxEternalDiameter(padPipeline, identViews);
    createPipelineName(padPipeline);
    padPipelineRepository.save(padPipeline);
  }


  private void setMaxEternalDiameter(PadPipeline padPipeline, List<IdentView> identViews) {
    BigDecimal largestExternalDiameter = BigDecimal.ZERO;

    if (padPipeline.getCoreType().equals(PipelineCoreType.SINGLE_CORE)) {
      for (var identView : identViews) {
        if (identView.getExternalDiameter() != null && largestExternalDiameter.compareTo(
            identView.getExternalDiameter()) == -1) {
          largestExternalDiameter = identView.getExternalDiameter();
        }
      }
    }

    padPipeline.setMaxExternalDiameter(
        largestExternalDiameter.equals(BigDecimal.ZERO) ? null : largestExternalDiameter);
  }


  private void createPipelineName(PadPipeline padPipeline) {
    var pipelineName = padPipeline.getPipelineRef() + " - ";
    if (padPipeline.getCoreType().equals(PipelineCoreType.SINGLE_CORE) && padPipeline.getMaxExternalDiameter() != null) {
      pipelineName += padPipeline.getMaxExternalDiameter() + " Millimetre ";
    }
    pipelineName += padPipeline.getPipelineType().getDisplayName();
    if (padPipeline.getPipelineInBundle()) {
      pipelineName += " (" + padPipeline.getBundleName() + ")";
    }
    padPipeline.setPipelineName(pipelineName);
  }

}
