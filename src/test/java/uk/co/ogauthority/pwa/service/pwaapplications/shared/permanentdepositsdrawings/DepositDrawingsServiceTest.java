package uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdepositsdrawings;

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
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdepositdrawings.PadDepositDrawing;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositDrawingsForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.permanentdepositdrawings.PadDepositDrawingRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdepositdrawings.DepositDrawingsService;
import uk.co.ogauthority.pwa.util.PwaApplicationTestUtil;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DepositDrawingsServiceTest {

  private DepositDrawingsService depositDrawingsService;

  @Mock
  private PadDepositDrawingRepository padDepositDrawingRepository;

  @Mock
  private PadFileService padFileService;
  
  @Mock
  private SpringValidatorAdapter springValidatorAdapter;

  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {
    depositDrawingsService = new DepositDrawingsService(padDepositDrawingRepository,
        springValidatorAdapter, padFileService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 100);
  }


  @Test
  public void addDrawing() {
    var form = new PermanentDepositDrawingsForm();
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "desc", Instant.now())));
    form.setReference("ref");

    var padFile = new PadFile(pwaApplicationDetail, "1", ApplicationFilePurpose.PIPELINE_DRAWINGS, ApplicationFileLinkStatus.FULL);
    when(padFileService.getPadFileByPwaApplicationDetailAndFileId(pwaApplicationDetail, "1")).thenReturn(padFile);
    depositDrawingsService.addDrawing(pwaApplicationDetail, form);

    var captor = ArgumentCaptor.forClass(PadDepositDrawing.class);
    verify(padDepositDrawingRepository, times(1)).save(captor.capture());

    assertThat(captor.getValue()).extracting(PadDepositDrawing::getFile, PadDepositDrawing::getReference, PadDepositDrawing::getPwaApplicationDetail)
        .containsExactly(padFile, "ref", pwaApplicationDetail);
  }

}