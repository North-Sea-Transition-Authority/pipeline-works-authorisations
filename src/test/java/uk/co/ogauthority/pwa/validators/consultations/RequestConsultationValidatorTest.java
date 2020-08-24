package uk.co.ogauthority.pwa.validators.consultations;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestForm;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class RequestConsultationValidatorTest {

  @Mock
  private ConsultationRequestService consultationRequestService;
  @Mock
  private ConsulteeGroupDetailService consulteeGroupDetailService;

  private ConsultationRequestValidator validator;

  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {
    validator = new ConsultationRequestValidator();
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 100);
  }



  @Test
  public void validate_form_empty() {
    var form = new ConsultationRequestForm();
    form.setDaysToRespond(null);
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, 
        new ConsultationRequestValidationHints(consultationRequestService, consulteeGroupDetailService, pwaApplicationDetail.getPwaApplication()));
    assertThat(errorsMap).containsOnly(
        entry("consulteeGroupSelection", Set.of("consulteeGroupSelection.required")),
        entry("daysToRespond", Set.of("daysToRespond.required"))
    );
  }

  @Test
  public void validate_form_valid() {
    var form = new ConsultationRequestForm();
    form.getConsulteeGroupSelection().put("1", "true");
    form.setDaysToRespond(5);

    var consulteeGroupDetail = new ConsulteeGroupDetail();
    consulteeGroupDetail.setId(1);
    consulteeGroupDetail.setConsulteeGroup(new ConsulteeGroup());

    when(consulteeGroupDetailService.getConsulteeGroupDetailById(1)).thenReturn(consulteeGroupDetail);
    when(consultationRequestService.isConsultationRequestOpen(consulteeGroupDetail.getConsulteeGroup(), pwaApplicationDetail.getPwaApplication())).thenReturn(false);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, 
        new ConsultationRequestValidationHints(consultationRequestService, consulteeGroupDetailService, pwaApplicationDetail.getPwaApplication()));
    assertThat(errorsMap).isEmpty();

    verify(consultationRequestService, times(1)).isConsultationRequestOpen(consulteeGroupDetail.getConsulteeGroup(), pwaApplicationDetail.getPwaApplication());
  }

  @Test
  public void validate_consultationAlreadyOpen_invalid() {
    var form = new ConsultationRequestForm();
    form.getConsulteeGroupSelection().put("1", "true");
    form.setDaysToRespond(5);

    var consulteeGroupDetail = new ConsulteeGroupDetail();
    consulteeGroupDetail.setId(1);
    consulteeGroupDetail.setConsulteeGroup(new ConsulteeGroup());

    when(consulteeGroupDetailService.getConsulteeGroupDetailById(1)).thenReturn(consulteeGroupDetail);
    when(consultationRequestService.isConsultationRequestOpen(consulteeGroupDetail.getConsulteeGroup(), pwaApplicationDetail.getPwaApplication())).thenReturn(true);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, 
        new ConsultationRequestValidationHints(consultationRequestService, consulteeGroupDetailService, pwaApplicationDetail.getPwaApplication()));
    assertThat(errorsMap).contains(
        entry("consulteeGroupSelection", Set.of("consulteeGroupSelection.invalid"))
    );

    verify(consultationRequestService, times(1)).isConsultationRequestOpen(consulteeGroupDetail.getConsulteeGroup(), pwaApplicationDetail.getPwaApplication());
  }


  @Test
  public void validate_daysToRespondZero_invalid() {
    var form = new ConsultationRequestForm();
    form.getConsulteeGroupSelection().put("1", "true");
    form.setDaysToRespond(0);

    var groupDetail = new ConsulteeGroupDetail();
    groupDetail.setName("My Group");
    var consulteeGroup = new ConsulteeGroup();
    consulteeGroup.setId(1);
    groupDetail.setConsulteeGroup(consulteeGroup);
    when(consulteeGroupDetailService.getConsulteeGroupDetailById(1)).thenReturn(groupDetail);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, 
        new ConsultationRequestValidationHints(consultationRequestService, consulteeGroupDetailService, pwaApplicationDetail.getPwaApplication()));
    assertThat(errorsMap).contains(
        entry("daysToRespond", Set.of("daysToRespond.invalid"))
    );
  }






}