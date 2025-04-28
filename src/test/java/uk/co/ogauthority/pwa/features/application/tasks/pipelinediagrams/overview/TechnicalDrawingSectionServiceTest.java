package uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.overview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasks.optionconfirmation.PadOptionConfirmedService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.admiralty.AdmiraltyChartDocumentForm;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.admiralty.AdmiraltyChartFileService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PadTechnicalDrawing;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PadTechnicalDrawingService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PipelineSchematicsErrorCode;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.umbilical.UmbilicalCrossSectionService;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.generic.SummaryForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class TechnicalDrawingSectionServiceTest {

  @Mock
  private AdmiraltyChartFileService admiraltyChartFileService;

  @Mock
  private PadTechnicalDrawingService padTechnicalDrawingService;

  @Mock
  private UmbilicalCrossSectionService umbilicalCrossSectionService;

  @Mock
  private PadOptionConfirmedService padOptionConfirmedService;

  @Mock
  private PadFileManagementService padFileManagementService;

  private TechnicalDrawingSectionService technicalDrawingSectionService;
  private PwaApplicationDetail detail;

  @BeforeEach
  void setUp() {
    technicalDrawingSectionService = new TechnicalDrawingSectionService(
        admiraltyChartFileService,
        padTechnicalDrawingService,
        umbilicalCrossSectionService,
        padOptionConfirmedService,
        padFileManagementService);
    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
  }

  @Test
  void validate_serviceInteraction_cantUploadAdmiraltyDocuments() {
    var form = new AdmiraltyChartDocumentForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(admiraltyChartFileService.canUploadDocuments(detail)).thenReturn(false);
    technicalDrawingSectionService.validate(form, bindingResult, ValidationType.FULL, detail);
    verify(padTechnicalDrawingService, times(1)).validateSection(any(BindingResult.class), eq(detail));
    verify(admiraltyChartFileService, never()).validate(any(), any(), any(), any());
  }

  @Test
  void validate_serviceInteraction_canUploadAdmiraltyDocuments() {
    var form = new AdmiraltyChartDocumentForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(admiraltyChartFileService.canUploadDocuments(detail)).thenReturn(true);
    technicalDrawingSectionService.validate(form, bindingResult, ValidationType.FULL, detail);
    verify(padTechnicalDrawingService, times(1)).validateSection(any(BindingResult.class), eq(detail));
    verify(admiraltyChartFileService, times(1)).validate(any(), any(BindingResult.class), eq(ValidationType.FULL), eq(detail));
  }

  @Test
  void getValidationSummary_noError() {

    var bindingResult = new BeanPropertyBindingResult(new SummaryForm(), "form");
    var validationSummary = technicalDrawingSectionService.getValidationSummary(bindingResult);
    assertThat(validationSummary.isComplete()).isTrue();
    assertThat(validationSummary.getErrorMessage()).isNull();
  }

  @Test
  void getValidationSummary_technicalDrawingError() {

    var bindingResult = new BeanPropertyBindingResult(new SummaryForm(), "form");
    String[] errorCodes = {PipelineSchematicsErrorCode.TECHNICAL_DRAWINGS.getErrorCode()};
    bindingResult.addError(new ObjectError("summaryForm", errorCodes, null, ""));
    var validationSummary = technicalDrawingSectionService.getValidationSummary(bindingResult);
    assertThat(validationSummary.isComplete()).isFalse();
    assertThat(validationSummary.getErrorMessage()).isEqualTo("All pipelines must be linked to a drawing");
  }

  @Test
  void getValidationSummary_admiraltyChartError() {

    var bindingResult = new BeanPropertyBindingResult(new SummaryForm(), "form");
    String[] errorCodes = {PipelineSchematicsErrorCode.ADMIRALTY_CHART.getErrorCode()};
    bindingResult.addError(new ObjectError("summaryForm", errorCodes, null, ""));
    var validationSummary = technicalDrawingSectionService.getValidationSummary(bindingResult);
    assertThat(validationSummary.isComplete()).isFalse();
    assertThat(validationSummary.getErrorMessage()).isEqualTo("An admiralty chart must be provided");
  }

  @Test
  void getValidationSummary_technicalDrawingAndAdmiraltyChartError() {

    var bindingResult = new BeanPropertyBindingResult(new SummaryForm(), "form");
    String[] errorCodes = {PipelineSchematicsErrorCode.TECHNICAL_DRAWINGS.getErrorCode(),
        PipelineSchematicsErrorCode.ADMIRALTY_CHART.getErrorCode()};
    bindingResult.addError(new ObjectError("summaryForm", errorCodes, null, ""));
    var validationSummary = technicalDrawingSectionService.getValidationSummary(bindingResult);
    assertThat(validationSummary.isComplete()).isFalse();
    assertThat(validationSummary.getErrorMessage()).isEqualTo(
        "An admiralty chart must be provided, and all pipelines must be linked to a drawing");
  }

  @Test
  void cleanupData_serviceInteractions() {
    technicalDrawingSectionService.cleanupData(detail);
    verify(padTechnicalDrawingService, times(1)).cleanupData(detail);

  }

  @Test
  void canShowInTaskList_notOptionsVariation() {
    var notOptions = EnumSet.allOf(PwaApplicationType.class);
    notOptions.remove(PwaApplicationType.OPTIONS_VARIATION);

    for (PwaApplicationType type : notOptions) {
      detail.getPwaApplication().setApplicationType(type);
      assertThat(technicalDrawingSectionService.canShowInTaskList(detail)).isTrue();
    }

  }

  @Test
  void canShowInTaskList_OptionsVariation_optionsNotComplete() {
    when(padOptionConfirmedService.approvedOptionConfirmed(detail)).thenReturn(false);

    detail.getPwaApplication().setApplicationType(PwaApplicationType.OPTIONS_VARIATION);

    assertThat(technicalDrawingSectionService.canShowInTaskList(detail)).isFalse();

  }

  @Test
  void canShowInTaskList_OptionsVariation_optionsComplete() {
    when(padOptionConfirmedService.approvedOptionConfirmed(detail)).thenReturn(true);

    detail.getPwaApplication().setApplicationType(PwaApplicationType.OPTIONS_VARIATION);

    assertThat(technicalDrawingSectionService.canShowInTaskList(detail)).isTrue();

  }

  @Test
  void getAvailableMailMergeFields() {

    PwaApplicationType.stream().forEach(appType -> {

      var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(appType);

      var mergeFields = technicalDrawingSectionService.getAvailableMailMergeFields(detail.getPwaApplicationType());

      var expectedMergeFields = new ArrayList<MailMergeFieldMnem>();

      if (MailMergeFieldMnem.PL_DRAWING_REF_LIST.appTypeIsSupported(appType)) {
        expectedMergeFields.add(MailMergeFieldMnem.PL_DRAWING_REF_LIST);
      }

      if (MailMergeFieldMnem.ADMIRALTY_CHART_REF.appTypeIsSupported(appType)) {
        expectedMergeFields.add(MailMergeFieldMnem.ADMIRALTY_CHART_REF);
      }

      assertThat(mergeFields).containsExactlyInAnyOrderElementsOf(expectedMergeFields);

    });

  }

  @Test
  void resolveMailMergeFields() {

    var drawing1 = new PadTechnicalDrawing();
    drawing1.setReference("draw1");

    var drawing2 = new PadTechnicalDrawing();
    drawing2.setReference("draw2");

    when(padTechnicalDrawingService.getDrawings(any())).thenReturn(List.of(drawing1, drawing2));

    var uploadedFile = new UploadedFile();
    uploadedFile.setDescription("admiralty desc");

    when(padFileManagementService.getUploadedFiles(any(PwaApplicationDetail.class), eq(FileDocumentType.ADMIRALTY_CHART)))
        .thenReturn(List.of(uploadedFile));

    PwaApplicationType.stream().forEach(appType -> {

      var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(appType);

      var mergeFieldsMap = technicalDrawingSectionService.resolveMailMergeFields(detail);

      var expectedMergeFieldsMap = new HashMap<MailMergeFieldMnem, String>();

      if (MailMergeFieldMnem.PL_DRAWING_REF_LIST.appTypeIsSupported(appType)) {
        expectedMergeFieldsMap.put(MailMergeFieldMnem.PL_DRAWING_REF_LIST, "draw1, draw2");
      }

      if (MailMergeFieldMnem.ADMIRALTY_CHART_REF.appTypeIsSupported(appType)) {
        expectedMergeFieldsMap.put(MailMergeFieldMnem.ADMIRALTY_CHART_REF, uploadedFile.getDescription());
      }

      assertThat(mergeFieldsMap).containsExactlyInAnyOrderEntriesOf(expectedMergeFieldsMap);

    });

  }

}