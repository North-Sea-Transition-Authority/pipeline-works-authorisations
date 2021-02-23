package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.publicnotice.PublicNoticeApprovalForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.publicnotice.PublicNoticeApprovalValidator;

@RunWith(MockitoJUnitRunner.class)
public class PublicNoticeApprovalServiceTest {

  private PublicNoticeApprovalService publicNoticeApprovalService;

  @Mock
  private PublicNoticeService publicNoticeService;

  @Mock
  private PublicNoticeApprovalValidator validator;


  private PwaApplication pwaApplication;
  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;



  @Before
  public void setUp() {

    publicNoticeApprovalService = new PublicNoticeApprovalService(validator, publicNoticeService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplication = pwaApplicationDetail.getPwaApplication();
    user = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createDefaultPerson()), List.of());
  }


  @Test
  public void openPublicNoticeCanBeApproved_approvablePublicNoticeExistsWithApp() {
    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    when(publicNoticeService.getOpenPublicNoticesByStatus(PublicNoticeStatus.MANAGER_APPROVAL)).thenReturn(List.of(publicNotice));
    var publicNoticeExists = publicNoticeApprovalService.openPublicNoticeCanBeApproved(pwaApplication);
    assertThat(publicNoticeExists).isTrue();
  }

  @Test
  public void openPublicNoticeCanBeApproved_approvablePublicNoticeExistsWithDifferentApp() {
    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(new PwaApplication());
    when(publicNoticeService.getOpenPublicNoticesByStatus(PublicNoticeStatus.MANAGER_APPROVAL)).thenReturn(List.of(publicNotice));
    var publicNoticeExists = publicNoticeApprovalService.openPublicNoticeCanBeApproved(pwaApplication);
    assertThat(publicNoticeExists).isFalse();
  }

  @Test
  public void openPublicNoticeCanBeApproved_approvablePublicNoticeDoesNotExist() {
    when(publicNoticeService.getOpenPublicNoticesByStatus(PublicNoticeStatus.MANAGER_APPROVAL)).thenReturn(List.of());
    var publicNoticeExists = publicNoticeApprovalService.openPublicNoticeCanBeApproved(pwaApplication);
    assertThat(publicNoticeExists).isFalse();
  }

  @Test
  public void validate_verifyServiceInteractions() {

    var form = new PublicNoticeApprovalForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    publicNoticeApprovalService.validate(form, bindingResult);
    verify(validator, times(1)).validate(form, bindingResult);

  }


}
