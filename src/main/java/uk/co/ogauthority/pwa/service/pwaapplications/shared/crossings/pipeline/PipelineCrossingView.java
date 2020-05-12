package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline;

import java.util.List;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.pipelines.PadPipelineCrossing;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.pipelines.PadPipelineCrossingOwner;

public class PipelineCrossingView {

  private Integer id;
  private String reference;
  private String owners;

  public PipelineCrossingView(PadPipelineCrossing padPipelineCrossing, List<PadPipelineCrossingOwner> owners) {
    id = padPipelineCrossing.getId();
    reference = padPipelineCrossing.getPipelineCrossed();
    if (owners.size() > 0) {
      this.owners = owners.stream()
          .map(padPipelineCrossingOwner -> padPipelineCrossingOwner.isManualEntry()
                ? padPipelineCrossingOwner.getManualOrganisationEntry()
                : padPipelineCrossingOwner.getOrganisationUnit().getName())
          .collect(Collectors.joining(", "));
    } else {
      this.owners = "Pipeline belongs to the holder";
    }
  }

  public Integer getId() {
    return id;
  }

  public String getReference() {
    return reference;
  }

  public String getOwners() {
    return owners;
  }
}
