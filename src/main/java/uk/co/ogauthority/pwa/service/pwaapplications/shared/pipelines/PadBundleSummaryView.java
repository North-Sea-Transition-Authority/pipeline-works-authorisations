package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import java.util.List;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadBundle;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadBundleLink;

public class PadBundleSummaryView {

  private final String bundleName;
  private final List<String> pipelineReferences;

  public PadBundleSummaryView(PadBundle bundle, List<PadBundleLink> links) {
    this.bundleName = bundle.getBundleName();
    this.pipelineReferences = links.stream()
        .map(padBundleLink -> padBundleLink.getPipeline().getPipelineRef())
        .collect(Collectors.toUnmodifiableList());
  }

  public String getBundleName() {
    return bundleName;
  }

  public List<String> getPipelineReferences() {
    return pipelineReferences;
  }
}
