package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class BundleValidationFactory {

  private final boolean isComplete;
  private final List<PadBundleView> bundleViews;
  private final Function<PadBundleView, Boolean> linkValidFunction;
  private final Function<PadBundleView, String> linkErrorMessage;

  public BundleValidationFactory(
      boolean isComplete,
      List<PadBundleView> bundleViews,
      Function<PadBundleView, Boolean> linkValidFunction,
      Function<PadBundleView, String> linkErrorMessage) {
    this.isComplete = isComplete;
    this.bundleViews = bundleViews;
    this.linkValidFunction = linkValidFunction;
    this.linkErrorMessage = linkErrorMessage;
  }

  public boolean isValid(PadBundleSummaryView summaryView) {
    var bundleView = getViewFromSummary(summaryView);
    return bundleView.isPresent() ? linkValidFunction.apply(bundleView.get()) : false;
  }

  public String getErrorMessage(PadBundleSummaryView summaryView) {
    var bundleView = getViewFromSummary(summaryView);
    return bundleView.isPresent() ? linkErrorMessage.apply(bundleView.get()) : "Bundle no longer exists";
  }

  private Optional<PadBundleView> getViewFromSummary(PadBundleSummaryView summaryView) {
    return bundleViews.stream()
        .filter(bundleView -> bundleView.getBundle().getBundleName().equals(summaryView.getBundleName()))
        .findFirst();
  }

  public boolean isComplete() {
    return isComplete;
  }
}
