package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import java.math.BigDecimal;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineCoreType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdentData;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineIdentDataRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineIdentRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineRepository;

/**
 * The purpose of this class is to perform the required operations on ident data
 * whenever a pipeline or ident has been added/modified/removed prior to saving the pipeline.
 * The functionality is all contained within this class so that a pipeline
 * cannot be persisted without having performed the operations first.
 * This also avoids the PadPipelineService and the PadPipelineIdentsService both depending on each other.
  */
@Service
public class PadPipelinePersisterService {

  private final PadPipelineRepository padPipelineRepository;
  private final PadPipelineIdentRepository padPipelineIdentRepository;
  private final PadPipelineIdentDataRepository padPipelineIdentDataRepository;

  public PadPipelinePersisterService(
      PadPipelineRepository padPipelineRepository,
      PadPipelineIdentRepository padPipelineIdentRepository,
      PadPipelineIdentDataRepository padPipelineIdentDataRepository) {
    this.padPipelineRepository = padPipelineRepository;
    this.padPipelineIdentRepository = padPipelineIdentRepository;
    this.padPipelineIdentDataRepository = padPipelineIdentDataRepository;
  }

  @Transactional
  public void savePadPipelineAndMaterialiseIdentData(PadPipeline padPipeline) {
    setMaxEternalDiameterOnPipeline(padPipeline);
    padPipelineRepository.save(padPipeline);
  }


  private void setMaxEternalDiameterOnPipeline(PadPipeline padPipeline) {
    BigDecimal largestExternalDiameter = BigDecimal.ZERO;

    if (padPipeline.getCoreType().equals(PipelineCoreType.SINGLE_CORE) && padPipeline.getId() != null) {
      largestExternalDiameter = getIdentData(padPipeline).stream()
          .map(identData -> identData.getExternalDiameter() != null ? identData.getExternalDiameter() : BigDecimal.ZERO)
          .reduce(BigDecimal.ZERO, BigDecimal::max);
    }

    padPipeline.setMaxExternalDiameter(
        largestExternalDiameter.equals(BigDecimal.ZERO) ? null : largestExternalDiameter);
  }


  private List<PadPipelineIdentData> getIdentData(PadPipeline padPipeline) {
    var idents = padPipelineIdentRepository.getAllByPadPipeline(padPipeline);
    return padPipelineIdentDataRepository.getAllByPadPipelineIdentIn(idents);
  }

}
