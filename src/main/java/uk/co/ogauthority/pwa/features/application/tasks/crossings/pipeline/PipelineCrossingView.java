package uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline;

import java.util.List;
import java.util.stream.Collectors;

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
