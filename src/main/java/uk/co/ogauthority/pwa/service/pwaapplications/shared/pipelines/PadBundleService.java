package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadBundle;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.BundleForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadBundleRepository;

@Service
public class PadBundleService {

  private final PadBundleRepository padBundleRepository;
  private final PadBundleLinkService padBundleLinkService;
  private final PadPipelineService padPipelineService;

  @Autowired
  public PadBundleService(
      PadBundleRepository padBundleRepository,
      PadBundleLinkService padBundleLinkService,
      PadPipelineService padPipelineService) {
    this.padBundleRepository = padBundleRepository;
    this.padBundleLinkService = padBundleLinkService;
    this.padPipelineService = padPipelineService;
  }

  @Transactional
  public void createBundleAndLinks(PwaApplicationDetail detail, BundleForm form) {
    var bundle = new PadBundle();
    bundle.setBundleName(form.getBundleName());
    bundle.setPwaApplicationDetail(detail);
    padBundleRepository.save(bundle);
    padBundleLinkService.createBundleLinks(bundle, form);
  }

  public boolean canAddBundle(PwaApplicationDetail detail) {
    return padPipelineService.getTotalPipelinesContainedInApplication(detail) >= 2;
  }

}
