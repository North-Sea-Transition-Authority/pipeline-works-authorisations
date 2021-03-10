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
import uk.co.ogauthority.pwa.model.form.publicnotice.FinalisePublicNoticeForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.publicnotice.FinalisePublicNoticeValidator;

@RunWith(MockitoJUnitRunner.class)
public class FinalisePublicNoticeServiceTest {

  private FinalisePublicNoticeService finalisePublicNoticeService;

  @Mock
  private PublicNoticeService publicNoticeService;

  @Mock
  private FinalisePublicNoticeValidator validator;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;
  

  private PwaApplication pwaApplication;
  private AuthenticatedUserAccount user;



  @Before
  public void setUp() {

    finalisePublicNoticeService = new FinalisePublicNoticeService(publicNoticeService, validator,
        camundaWorkflowService);

    var pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplication = pwaApplicationDetail.getPwaApplication();
    user = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createDefaultPerson()), List.of());
  }


  @Test
  public void publicNoticeCanBeFinalised_publicNoticeThatCanBeFinalisedExistsWithApp() {
    var publicNotice = PublicNoticeTestUtil.createCaseOfficerReviewPublicNotice(pwaApplication);
    when(publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.CASE_OFFICER_REVIEW)).thenReturn(List.of(publicNotice));
    var publicNoticeExists = finalisePublicNoticeService.publicNoticeCanBeFinalised(pwaApplication);
    assertThat(publicNoticeExists).isTrue();
  }

  @Test
  public void publicNoticeCanBeFinalised_publicNoticeThatCanBeFinalisedExistsWithDifferentApp() {
    var publicNotice = PublicNoticeTestUtil.createCaseOfficerReviewPublicNotice(new PwaApplication());
    when(publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.CASE_OFFICER_REVIEW)).thenReturn(List.of(publicNotice));
    var publicNoticeExists = finalisePublicNoticeService.publicNoticeCanBeFinalised(pwaApplication);
    assertThat(publicNoticeExists).isFalse();
  }

  @Test
  public void publicNoticeCanBeFinalised_publicNoticeThatCanBeFinalisedDoesNotExist() {
    when(publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.CASE_OFFICER_REVIEW)).thenReturn(List.of());
    var publicNoticeExists = finalisePublicNoticeService.publicNoticeCanBeFinalised(pwaApplication);
    assertThat(publicNoticeExists).isFalse();
  }

  @Test
  public void validate_verifyServiceInteractions() {

    var form = new FinalisePublicNoticeForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    finalisePublicNoticeService.validate(form, bindingResult);
    verify(validator, times(1)).validate(form, bindingResult);
  }



}
