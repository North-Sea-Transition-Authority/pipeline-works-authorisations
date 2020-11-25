package uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.techdetails.AdmiraltyChartDocumentForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.options.PadOptionsCompleteService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class TechnicalDrawingSectionServiceTest {

  @Mock
  private AdmiraltyChartFileService admiraltyChartFileService;

  @Mock
  private PadTechnicalDrawingService padTechnicalDrawingService;

  @Mock
  private UmbilicalCrossSectionService umbilicalCrossSectionService;

  @Mock
  private PadFileService padFileService;

  @Mock
  private PadOptionsCompleteService padOptionsCompleteService;

  private TechnicalDrawingSectionService technicalDrawingSectionService;
  private PwaApplicationDetail detail;

  @Before
  public void setUp() {
    technicalDrawingSectionService = new TechnicalDrawingSectionService(
        admiraltyChartFileService,
        padTechnicalDrawingService,
        umbilicalCrossSectionService,
        padFileService,
        padOptionsCompleteService);
    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
  }

  @Test
  public void validate_serviceInteraction_cantUploadAdmiraltyDocuments() {
    var form = new AdmiraltyChartDocumentForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(admiraltyChartFileService.canUploadDocuments(detail)).thenReturn(false);
    technicalDrawingSectionService.validate(form, bindingResult, ValidationType.FULL, detail);
    verify(padTechnicalDrawingService, times(1)).validateSection(bindingResult, detail);
    verify(admiraltyChartFileService, never()).validate(any(), any(), any(), any());
  }

  @Test
  public void validate_serviceInteraction_canUploadAdmiraltyDocuments() {
    var form = new AdmiraltyChartDocumentForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(admiraltyChartFileService.canUploadDocuments(detail)).thenReturn(true);
    technicalDrawingSectionService.validate(form, bindingResult, ValidationType.FULL, detail);
    verify(padTechnicalDrawingService, times(1)).validateSection(bindingResult, detail);
    verify(admiraltyChartFileService, times(1)).validate(any(), eq(bindingResult), eq(ValidationType.FULL), eq(detail));
  }

  @Test
  public void cleanupData_serviceInteractions() {
    technicalDrawingSectionService.cleanupData(detail);
    verify(padTechnicalDrawingService, times(1)).cleanupData(detail);

  }

  @Test
  public void canShowInTaskList_notOptionsVariation() {
    var notOptions = EnumSet.allOf(PwaApplicationType.class);
    notOptions.remove(PwaApplicationType.OPTIONS_VARIATION);

    for (PwaApplicationType type : notOptions) {
      detail.getPwaApplication().setApplicationType(type);
      assertThat(technicalDrawingSectionService.canShowInTaskList(detail)).isTrue();
    }

  }

  @Test
  public void canShowInTaskList_OptionsVariation_optionsNotComplete() {
    when(padOptionsCompleteService.approvedOptionComplete(detail)).thenReturn(false);

    detail.getPwaApplication().setApplicationType(PwaApplicationType.OPTIONS_VARIATION);

    assertThat(technicalDrawingSectionService.canShowInTaskList(detail)).isFalse();

  }

  @Test
  public void canShowInTaskList_OptionsVariation_optionsComplete() {
    when(padOptionsCompleteService.approvedOptionComplete(detail)).thenReturn(true);

    detail.getPwaApplication().setApplicationType(PwaApplicationType.OPTIONS_VARIATION);

    assertThat(technicalDrawingSectionService.canShowInTaskList(detail)).isTrue();

  }
}