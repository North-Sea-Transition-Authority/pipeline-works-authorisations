package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.publicnotice.PublicNoticeDocumentUpdateController;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeAction;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.form.publicnotice.UpdatePublicNoticeDocumentForm;
import uk.co.ogauthority.pwa.model.view.banner.BannerLink;
import uk.co.ogauthority.pwa.model.view.banner.PageBannerView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.validators.publicnotice.PublicNoticeDocumentUpdateValidator;

@Service
public class PublicNoticeDocumentUpdateService {


  private final PublicNoticeService publicNoticeService;
  private final PublicNoticeDocumentUpdateValidator publicNoticeDocumentUpdateValidator;
  private final AppFileService appFileService;

  private static final AppFilePurpose FILE_PURPOSE = AppFilePurpose.PUBLIC_NOTICE;

  @Autowired
  public PublicNoticeDocumentUpdateService(
      PublicNoticeService publicNoticeService,
      PublicNoticeDocumentUpdateValidator publicNoticeDocumentUpdateValidator,
      AppFileService appFileService) {
    this.publicNoticeService = publicNoticeService;
    this.publicNoticeDocumentUpdateValidator = publicNoticeDocumentUpdateValidator;
    this.appFileService = appFileService;
  }




  public boolean publicNoticeDocumentCanBeUpdated(PwaApplication pwaApplication) {
    return publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.APPLICANT_UPDATE)
        .stream()
        .anyMatch(publicNotice -> publicNotice.getPwaApplication().equals(pwaApplication));
  }

  public Optional<PageBannerView> getPublicNoticeUpdatePageBannerView(PwaApplication pwaApplication) {

    if (!publicNoticeDocumentCanBeUpdated(pwaApplication)) {
      return Optional.empty();
    }

    var publicNotice = publicNoticeService.getLatestPublicNotice(pwaApplication);
    var publicNoticeRequest = publicNoticeService.getLatestPublicNoticeRequest(publicNotice);

    return Optional.of(new PageBannerView.PageBannerViewBuilder()
        .setHeader("Public notice document update requested")
        .setHeaderCaption("Requested " + DateUtils.formatDateTime(publicNoticeRequest.getResponseTimestamp()))
        .setBannerLink(new BannerLink(
            ReverseRouter.route(on(PublicNoticeDocumentUpdateController.class)
                .renderUpdatePublicNoticeDocument(pwaApplication.getId(), pwaApplication.getApplicationType(), null, null, null)),
            PublicNoticeAction.UPDATE_DOCUMENT.getDisplayText()
        ))
        .build());
  }

  public Optional<UploadedFileView> getPublicNoticeDocumentFileView(PwaApplication pwaApplication) {

    var documentFileViews = appFileService.getUploadedFileViews(pwaApplication, FILE_PURPOSE, ApplicationFileLinkStatus.FULL);
    if (!documentFileViews.isEmpty()) {
      return Optional.of(documentFileViews.get(0));
    }
    return Optional.empty();
  }


  public BindingResult validate(UpdatePublicNoticeDocumentForm form, BindingResult bindingResult) {
    publicNoticeDocumentUpdateValidator.validate(form, bindingResult);
    return bindingResult;
  }

  public void updatePublicNoticeDocumentAndTransitionWorkflow(UpdatePublicNoticeDocumentForm form,
                                                              PwaApplication pwaApplication,
                                                              AuthenticatedUserAccount authenticatedUserAccount) {
  }
}
