package uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

  private TechnicalDrawingSectionService technicalDrawingSectionService;
  private PwaApplicationDetail detail;

  @Before
  public void setUp() {
    technicalDrawingSectionService = new TechnicalDrawingSectionService(admiraltyChartFileService,
        padTechnicalDrawingService, umbilicalCrossSectionService, padFileService);
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
}