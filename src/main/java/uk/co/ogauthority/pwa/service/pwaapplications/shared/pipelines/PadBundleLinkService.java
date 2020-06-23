package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
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

  public List<PadBundleLink> getAllLinksForDetail(PwaApplicationDetail detail) {
    return padBundleLinkRepository.getAllByBundle_PwaApplicationDetail(detail);
  }

  public List<PadBundleLink> getLinksForBundle(PadBundle bundle) {
    return padBundleLinkRepository.getAllByBundle(bundle);
  }

  @Transactional
  public void createBundleLinks(PadBundle bundle, BundleForm form) {
    List<PadBundleLink> links = padPipelineService.getByIdList(bundle.getPwaApplicationDetail(), List.copyOf(form.getPadPipelineIds()))
        .stream()
        .map(pipeline -> buildBundleLink(bundle, pipeline))
        .collect(Collectors.toUnmodifiableList());
    padBundleLinkRepository.saveAll(links);
  }

  @Transactional
  public void removeBundleLinks(PadBundle bundle) {
    var links = padBundleLinkRepository.getAllByBundle(bundle);
    padBundleLinkRepository.deleteAll(links);
  }

  private PadBundleLink buildBundleLink(PadBundle bundle, PadPipeline pipeline) {
    var link = new PadBundleLink();
    link.setBundle(bundle);
    link.setPipeline(pipeline);
    return link;
  }
}
