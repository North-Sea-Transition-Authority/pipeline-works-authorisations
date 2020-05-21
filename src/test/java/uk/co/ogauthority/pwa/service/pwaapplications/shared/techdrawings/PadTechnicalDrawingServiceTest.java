package uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationFilePurpose;
import uk.co.ogauthority.pwa.model.entity.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawing;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.techdetails.PipelineDrawingForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.techdrawings.PadTechnicalDrawingRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.util.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.techdrawings.PipelineDrawingValidator;

@RunWith(MockitoJUnitRunner.class)
public class PadTechnicalDrawingServiceTest {

  private PadTechnicalDrawingService padTechnicalDrawingService;

  @Mock
  private PadTechnicalDrawingRepository padTechnicalDrawingRepository;

  @Mock
  private PadTechnicalDrawingLinkService padTechnicalDrawingLinkService;

  @Mock
  private PadFileService padFileService;

  @Mock
  private PadPipelineService padPipelineService;

  @Mock
  private PipelineDrawingValidator pipelineDrawingValidator;

  @Mock
  private SpringValidatorAdapter springValidatorAdapter;

  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {
    padTechnicalDrawingService = new PadTechnicalDrawingService(padTechnicalDrawingRepository,
        padTechnicalDrawingLinkService, padFileService, padPipelineService, pipelineDrawingValidator,
        springValidatorAdapter);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 100);
  }

  @Test
  public void getDrawings() {
    var list = List.of(new PadTechnicalDrawing());
    when(padTechnicalDrawingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(list);
    var result = padTechnicalDrawingService.getDrawings(pwaApplicationDetail);
    assertThat(list).isEqualTo(result);
  }

  @Test
  public void addDrawing() {
    var form = new PipelineDrawingForm();
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "desc", Instant.now())));
    form.setReference("ref");

    var padFile = new PadFile(pwaApplicationDetail, "1", ApplicationFilePurpose.PIPELINE_DRAWINGS, ApplicationFileLinkStatus.FULL);
    when(padFileService.getPadFileByPwaApplicationDetailAndFileId(pwaApplicationDetail, "1")).thenReturn(padFile);
    padTechnicalDrawingService.addDrawing(pwaApplicationDetail, form);

    var captor = ArgumentCaptor.forClass(PadTechnicalDrawing.class);
    verify(padTechnicalDrawingRepository, times(1)).save(captor.capture());
    verify(padTechnicalDrawingLinkService, times(1)).linkDrawing(pwaApplicationDetail, form, captor.getValue());

    assertThat(captor.getValue()).extracting(PadTechnicalDrawing::getFile, PadTechnicalDrawing::getReference, PadTechnicalDrawing::getPwaApplicationDetail)
        .containsExactly(padFile, "ref", pwaApplicationDetail);
  }

}