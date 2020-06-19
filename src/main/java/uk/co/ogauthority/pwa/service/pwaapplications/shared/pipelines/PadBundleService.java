package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadBundle;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadBundleLink;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.BundleForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadBundleRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;

@Service
public class PadBundleService implements ApplicationFormSectionService {

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

  public PadBundle getBundle(PwaApplicationDetail detail, Integer bundleId) {
    return padBundleRepository.getByPwaApplicationDetailAndId(detail, bundleId)
        .orElseThrow(
            () -> new PwaEntityNotFoundException(
                String.format("Unable to find pipeline bundle (%d) for app (%d)", bundleId, detail.getId())));
  }

  @Transactional
  public void createBundleAndLinks(PwaApplicationDetail detail, BundleForm form) {
    var bundle = new PadBundle();
    bundle.setPwaApplicationDetail(detail);
    updateBundleAndLinks(bundle, form);
  }

  @Transactional
  public void updateBundleAndLinks(PadBundle bundle, BundleForm form) {
    bundle.setBundleName(form.getBundleName());
    padBundleRepository.save(bundle);
    padBundleLinkService.removeBundleLinks(bundle);
    padBundleLinkService.createBundleLinks(bundle, form);
  }

  public List<PadBundleSummaryView> getBundleSummaryViews(PwaApplicationDetail detail) {
    Map<PadBundle, List<PadBundleLink>> bundleLinkMap = padBundleLinkService.getAllLinksForDetail(detail)
        .stream()
        .collect(Collectors.groupingBy(PadBundleLink::getBundle));
    return bundleLinkMap.entrySet()
        .stream()
        .map(entry -> new PadBundleSummaryView(entry.getKey(), entry.getValue()))
        .collect(Collectors.toUnmodifiableList());
  }

  public BundleValidationFactory getBundleValidationFactory(PwaApplicationDetail detail) {
    return new BundleValidationFactory(
        isComplete(detail),
        getBundleViews(detail),
        this::isBundleViewValid,
        this::getBundleViewErrorMessage
    );
  }

  public List<PadBundleView> getBundleViews(PwaApplicationDetail detail) {
    Map<PadBundle, List<PadBundleLink>> bundleLinkMap = padBundleLinkService.getAllLinksForDetail(detail)
        .stream()
        .collect(Collectors.groupingBy(PadBundleLink::getBundle));
    return bundleLinkMap.entrySet()
        .stream()
        .map(entry -> new PadBundleView(entry.getKey(), entry.getValue()))
        .collect(Collectors.toUnmodifiableList());
  }

  public PadBundleView getBundleView(PwaApplicationDetail detail, Integer bundleId) {
    var bundle = getBundle(detail, bundleId);
    var links = padBundleLinkService.getLinksForBundle(bundle);
    return new PadBundleView(bundle, links);
  }

  public void mapBundleViewToForm(PadBundleView bundleView, BundleForm bundleForm) {
    bundleForm.setBundleName(bundleView.getBundle().getBundleName());
    List<Integer> pipelineIds = bundleView.getLinks()
        .stream()
        .map(padBundleLink -> padBundleLink.getPipeline().getId())
        .collect(Collectors.toUnmodifiableList());
    bundleForm.setPipelineIds(pipelineIds);
  }

  private boolean isBundleViewValid(PadBundleView bundleView) {
    // TODO: PWA-619 - Remove hard-coded bundle size check
    if (bundleView.getLinks().size() < 2) {
      return false;
    }
    return true;
  }

  private String getBundleViewErrorMessage(PadBundleView bundleView) {
    // TODO: PWA-619 - Remove hard-coded bundle size check
    if (bundleView.getLinks().size() < 2) {
      return "This bundle requires at least two pipelines";
    }
    return null;
  }

  public boolean canAddBundle(PwaApplicationDetail detail) {
    return padPipelineService.getTotalPipelinesContainedInApplication(detail) >= 2;
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    // TODO: PWA-619 - Remove hard-coded bundle size check
    return getBundleSummaryViews(detail)
        .stream()
        .allMatch(padBundleSummaryView -> padBundleSummaryView.getPipelineReferences().size() >= 2);
  }

  @Override
  @Deprecated
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    throw new ActionNotAllowedException("PadBundleService should not be validated");
  }
}
