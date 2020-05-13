package uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines;

import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;

public class PadPipelineOverviewDto {

  private PwaApplicationDetail detail;
  private PadPipeline padPipeline;
  private Long numberOfIdents;

  public PadPipelineOverviewDto(PwaApplicationDetail detail,
                                PadPipeline padPipeline, Long numberOfIdents) {
    this.detail = detail;
    this.padPipeline = padPipeline;
    this.numberOfIdents = numberOfIdents;
  }

  public PwaApplicationDetail getDetail() {
    return detail;
  }

  public PadPipeline getPadPipeline() {
    return padPipeline;
  }

  public Long getNumberOfIdents() {
    return numberOfIdents;
  }
}
