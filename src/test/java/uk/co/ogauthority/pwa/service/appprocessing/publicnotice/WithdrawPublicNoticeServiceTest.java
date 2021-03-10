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
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.publicnotice.WithdrawPublicNoticeForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.publicnotice.WithdrawPublicNoticeValidator;

@RunWith(MockitoJUnitRunner.class)
public class WithdrawPublicNoticeServiceTest {

  private WithdrawPublicNoticeService withdrawPublicNoticeService;

  @Mock
  private PublicNoticeService publicNoticeService;

  @Mock
  private WithdrawPublicNoticeValidator validator;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private NotifyService notifyService;

  @Mock
  private EmailCaseLinkService emailCaseLinkService;


  private PwaApplication pwaApplication;
  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;



  @Before
  public void setUp() {

    withdrawPublicNoticeService = new WithdrawPublicNoticeService(publicNoticeService, validator,
        camundaWorkflowService, emailCaseLinkService, notifyService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplication = pwaApplicationDetail.getPwaApplication();
    user = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createDefaultPerson()), List.of());
  }


  @Test
  public void publicNoticeCanBeWithdrawn_withdrawablePublicNoticeExistsForApp() {
    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
    when(publicNoticeService.getOpenPublicNotices()).thenReturn(List.of(publicNotice));
    var publicNoticeCanBeWithdrawn = withdrawPublicNoticeService.publicNoticeCanBeWithdrawn(pwaApplication);
    assertThat(publicNoticeCanBeWithdrawn).isTrue();
  }

  @Test
  public void publicNoticeCanBeWithdrawn_withdrawablePublicNoticeExistsForDifferentApp() {
    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(new PwaApplication());
    when(publicNoticeService.getOpenPublicNotices()).thenReturn(List.of(publicNotice));
    var publicNoticeCanBeWithdrawn = withdrawPublicNoticeService.publicNoticeCanBeWithdrawn(pwaApplication);
    assertThat(publicNoticeCanBeWithdrawn).isFalse();
  }

  @Test
  public void publicNoticeCanBeWithdrawn_withdrawablePublicNoticeDoesNotExist() {
    when(publicNoticeService.getOpenPublicNotices()).thenReturn(List.of());
    var publicNoticeCanBeWithdrawn = withdrawPublicNoticeService.publicNoticeCanBeWithdrawn(pwaApplication);
    assertThat(publicNoticeCanBeWithdrawn).isFalse();
  }



  @Test
  public void validate_verifyServiceInteractions() {

    var form = new WithdrawPublicNoticeForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    withdrawPublicNoticeService.validate(form, bindingResult);
    verify(validator, times(1)).validate(form, bindingResult);

  }


}
