package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import java.util.List;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadBundle;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadBundleLink;

public class PadBundleView {

  private PadBundle bundle;
  private List<PadBundleLink> links;

  public PadBundleView(PadBundle bundle,
                       List<PadBundleLink> links) {
    this.bundle = bundle;
    this.links = links;
  }

  public PadBundle getBundle() {
    return bundle;
  }

  public List<PadBundleLink> getLinks() {
    return links;
  }
}
