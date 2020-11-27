package uk.co.ogauthority.pwa.service.appprocessing.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.withdraw.WithdrawApplicationForm;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.validators.WithdrawApplicationValidator;

@RunWith(MockitoJUnitRunner.class)
public class WithdrawApplicationServiceTest {

  private WithdrawApplicationService withdrawApplicationService;

  @Mock
  private WithdrawApplicationValidator withdrawApplicationValidator;

  @Before
  public void setUp() {
    withdrawApplicationService = new WithdrawApplicationService(withdrawApplicationValidator);
  }

  @Test
  public void canShowInTaskList() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.WITHDRAW_APPLICATION), null,
        null);

    boolean canShow = withdrawApplicationService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_industry() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY), null,
        null);

    boolean canShow = withdrawApplicationService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  public void validate() {

    var form = new WithdrawApplicationForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    withdrawApplicationService.validate(form, bindingResult, new PwaApplicationDetail());
    verify(withdrawApplicationValidator, times(1)).validate(form, bindingResult, new PwaApplicationDetail());
  }


}
