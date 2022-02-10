package uk.co.ogauthority.pwa.model.form.pwaapplications.views;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline.MedianLineStatus;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;

public class MedianLineAgreementView {

  private final MedianLineStatus agreementStatus;
  private final String negotiatorName;
  private final String negotiatorEmail;
  private final List<UploadedFileView> fileViews;

  public MedianLineAgreementView(MedianLineStatus agreementStatus,
                                 String negotiatorName,
                                 String negotiatorEmail,
                                 List<UploadedFileView> fileViews) {
    this.agreementStatus = agreementStatus;
    this.negotiatorName = negotiatorName;
    this.negotiatorEmail = negotiatorEmail;
    this.fileViews = fileViews;
  }

  public MedianLineStatus getAgreementStatus() {
    return agreementStatus;
  }

  public String getNegotiatorName() {
    return negotiatorName;
  }

  public String getNegotiatorEmail() {
    return negotiatorEmail;
  }

  public List<UploadedFileView> getSortedFileViews() {
    return fileViews.stream()
        .sorted(Comparator.comparing(UploadedFileView::getFileName))
        .collect(Collectors.toList());
  }
}
