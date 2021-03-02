package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.publicnotice.PublicNoticeDocumentUpdateController;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeAction;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileViewTestUtil;
import uk.co.ogauthority.pwa.model.form.publicnotice.UpdatePublicNoticeDocumentForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.validators.publicnotice.PublicNoticeDocumentUpdateValidator;

@RunWith(MockitoJUnitRunner.class)
public class PublicNoticeDocumentUpdateServiceTest {

  private PublicNoticeDocumentUpdateService publicNoticeDocumentUpdateService;

  @Mock
  private PublicNoticeService publicNoticeService;

  @Mock
  private PublicNoticeDocumentUpdateValidator validator;

  @Mock
  private AppFileService appFileService;


  private PwaApplication pwaApplication;
  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;
  private static final AppFilePurpose FILE_PURPOSE = AppFilePurpose.PUBLIC_NOTICE;



  @Before
  public void setUp() {

    publicNoticeDocumentUpdateService = new PublicNoticeDocumentUpdateService(publicNoticeService, validator, appFileService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplication = pwaApplicationDetail.getPwaApplication();
    user = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createDefaultPerson()), List.of());
  }


  @Test
  public void publicNoticeDocumentCanBeUpdated_updatablePublicNoticeExistsWithApp() {
    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    when(publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.APPLICANT_UPDATE)).thenReturn(List.of(publicNotice));
    var publicNoticeExists = publicNoticeDocumentUpdateService.publicNoticeDocumentCanBeUpdated(pwaApplication);
    assertThat(publicNoticeExists).isTrue();
  }

  @Test
  public void publicNoticeDocumentCanBeUpdated_updatablePublicNoticeExistsWithDifferentApp() {
    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(new PwaApplication());
    when(publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.APPLICANT_UPDATE)).thenReturn(List.of(publicNotice));
    var publicNoticeExists = publicNoticeDocumentUpdateService.publicNoticeDocumentCanBeUpdated(pwaApplication);
    assertThat(publicNoticeExists).isFalse();
  }

  @Test
  public void publicNoticeDocumentCanBeUpdated_updatablePublicNoticeDoesNotExist() {
    when(publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.APPLICANT_UPDATE)).thenReturn(List.of());
    var publicNoticeExists = publicNoticeDocumentUpdateService.publicNoticeDocumentCanBeUpdated(pwaApplication);
    assertThat(publicNoticeExists).isFalse();
  }

  @Test
  public void getPublicNoticeDocumentFileView_fileViewExists() {

    var documentFileView = UploadedFileViewTestUtil.createDefaultFileView();
    when(appFileService.getUploadedFileViews(pwaApplication, FILE_PURPOSE, ApplicationFileLinkStatus.FULL))
        .thenReturn(List.of(documentFileView));

    var actualFileViewOpt = publicNoticeDocumentUpdateService.getPublicNoticeDocumentFileView(pwaApplication);
    assertThat(actualFileViewOpt.isPresent()).isTrue();
    assertThat(actualFileViewOpt.get()).isEqualTo(documentFileView);
  }

  @Test
  public void getPublicNoticeDocumentFileView_fileViewDoesNotExists() {

    when(appFileService.getUploadedFileViews(pwaApplication, FILE_PURPOSE, ApplicationFileLinkStatus.FULL))
        .thenReturn(List.of());

    var actualFileViewOpt = publicNoticeDocumentUpdateService.getPublicNoticeDocumentFileView(pwaApplication);
    assertThat(actualFileViewOpt.isEmpty()).isTrue();
  }

  @Test
  public void getPublicNoticeUpdatePageBannerView_publicNoticeDocumentCanNotBeUpdated() {
    when(publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.APPLICANT_UPDATE)).thenReturn(List.of());
    var pageBannerViewOpt = publicNoticeDocumentUpdateService.getPublicNoticeUpdatePageBannerView(pwaApplication);
    assertThat(pageBannerViewOpt.isEmpty()).isTrue();
  }

  @Test
  public void getPublicNoticeUpdatePageBannerView_publicNoticeDocumentCanBeUpdated() {

    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    when(publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.APPLICANT_UPDATE)).thenReturn(List.of(publicNotice));

    when(publicNoticeService.getLatestPublicNotice(pwaApplication))
        .thenReturn(publicNotice);

    var publicNoticeRequest = PublicNoticeTestUtil.createApprovedPublicNoticeRequest(publicNotice);
    when(publicNoticeService.getLatestPublicNoticeRequest(publicNotice))
        .thenReturn(publicNoticeRequest);

    var pageBannerViewOpt = publicNoticeDocumentUpdateService.getPublicNoticeUpdatePageBannerView(pwaApplication);
    assertThat(pageBannerViewOpt.isPresent()).isTrue();

    var pageBannerView = pageBannerViewOpt.get();
    assertThat(pageBannerView.getHeader()).isEqualTo("Public notice document update requested");
    assertThat(pageBannerView.getHeaderCaption()).isEqualTo("Requested " + DateUtils.formatDateTime(
        publicNoticeRequest.getResponseTimestamp()));
    assertThat(pageBannerView.getBannerLink().getUrl()).isEqualTo(
        ReverseRouter.route(on(PublicNoticeDocumentUpdateController.class)
            .renderUpdatePublicNoticeDocument(pwaApplication.getId(), pwaApplication.getApplicationType(), null, null, null)));
    assertThat(pageBannerView.getBannerLink().getText()).isEqualTo(PublicNoticeAction.UPDATE_DOCUMENT.getDisplayText());
  }

  @Test
  public void validate_verifyServiceInteractions() {

    var form = new UpdatePublicNoticeDocumentForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    publicNoticeDocumentUpdateService.validate(form, bindingResult);
    verify(validator, times(1)).validate(form, bindingResult);

  }


}
