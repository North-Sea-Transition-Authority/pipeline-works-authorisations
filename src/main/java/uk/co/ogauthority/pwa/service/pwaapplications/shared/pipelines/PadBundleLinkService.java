package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadBundle;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadBundleLink;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.BundleForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadBundleLinkRepository;

@Service
public class PadBundleLinkService {

  private final PadBundleLinkRepository padBundleLinkRepository;
  private final PadPipelineService padPipelineService;

  @Autowired
  public PadBundleLinkService(
      PadBundleLinkRepository padBundleLinkRepository,
      PadPipelineService padPipelineService) {
    this.padBundleLinkRepository = padBundleLinkRepository;
    this.padPipelineService = padPipelineService;
  }

  @Transactional
  public void createBundleLinks(PadBundle bundle, BundleForm form) {
    List<PadBundleLink> links = padPipelineService.getByIdList(bundle.getPwaApplicationDetail(), form.getPipelineIds())
        .stream()
        .map(pipeline -> buildBundleLink(bundle, pipeline))
        .collect(Collectors.toUnmodifiableList());
    padBundleLinkRepository.saveAll(links);
  }

  private PadBundleLink buildBundleLink(PadBundle bundle, PadPipeline pipeline) {
    var link = new PadBundleLink();
    link.setBundle(bundle);
    link.setPipeline(pipeline);
    return link;
  }
}
