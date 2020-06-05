package uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdepositsdrawings;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationFilePurpose;
import uk.co.ogauthority.pwa.model.entity.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdepositdrawings.PadDepositDrawing;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdepositdrawings.PadDepositDrawingLink;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDeposit;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PermanentDepositDrawingView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositDrawingForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadPermanentDepositRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.permanentdepositdrawings.PadDepositDrawingLinkRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.permanentdepositdrawings.PadDepositDrawingRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdepositdrawings.DepositDrawingsService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.PermanentDepositService;
import uk.co.ogauthority.pwa.util.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.PermanentDepositsDrawingValidator;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DepositDrawingsServiceTest {

  private DepositDrawingsService depositDrawingsService;

  @Mock
  private PermanentDepositService permanentDepositService;

  @Mock
  private PadDepositDrawingRepository padDepositDrawingRepository;

  @Mock
  private PadPermanentDepositRepository padPermanentDepositRepository;

  @Mock
  private PadDepositDrawingLinkRepository padDepositDrawingLinkRepository;

  @Mock
  private PadFileService padFileService;

  @Mock
  private SpringValidatorAdapter springValidatorAdapter;

  @Mock
  private PermanentDepositsDrawingValidator validator;

  private PwaApplicationDetail pwaApplicationDetail;

  public DepositDrawingsServiceTest() {
  }

  @Before
  public void setUp() {
    depositDrawingsService = new DepositDrawingsService(permanentDepositService, padDepositDrawingRepository, padPermanentDepositRepository,
        padDepositDrawingLinkRepository, validator, springValidatorAdapter, padFileService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 100);
  }


  @Test
  public void addDrawing() {
    var form = new PermanentDepositDrawingForm();
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "desc", Instant.now())));
    form.setReference("ref");
    form.setSelectedDeposits(Set.of("1"));

    var padFile = new PadFile(pwaApplicationDetail, "1", ApplicationFilePurpose.DEPOSIT_DRAWINGS, ApplicationFileLinkStatus.FULL);
    when(padFileService.getPadFileByPwaApplicationDetailAndFileId(pwaApplicationDetail, "1")).thenReturn(padFile);

    var padPermanentDeposit = new PadPermanentDeposit();
    padPermanentDeposit.setId(1);
    //when(padPermanentDepositRepository.findById(Integer.parseInt("1"))).thenReturn(Optional.of(padPermanentDeposit));
    when(permanentDepositService.getDepositById(1)).thenReturn(Optional.of(padPermanentDeposit));
    depositDrawingsService.addDrawing(pwaApplicationDetail, form, new WebUserAccount());

    var captor = ArgumentCaptor.forClass(PadDepositDrawing.class);
    verify(padDepositDrawingRepository, times(1)).save(captor.capture());

    assertThat(captor.getValue()).extracting(PadDepositDrawing::getFile, PadDepositDrawing::getReference, PadDepositDrawing::getPwaApplicationDetail)
        .containsExactly(padFile, "ref", pwaApplicationDetail);

    var captorDrawingLink = ArgumentCaptor.forClass(PadDepositDrawingLink.class);
    verify(padDepositDrawingLinkRepository, times(1)).save(captorDrawingLink.capture());
  }


  @Test
  public void getDepositDrawingSummaryViews() {
    var depositDrawing = new PadDepositDrawing();
    depositDrawing.setReference("ref");
    depositDrawing.setPwaApplicationDetail(pwaApplicationDetail);
    depositDrawing.setFile(new PadFile(pwaApplicationDetail, "1", ApplicationFilePurpose.DEPOSIT_DRAWINGS,
        ApplicationFileLinkStatus.FULL));
    depositDrawing.setId(1);

    var drawingLink = new PadDepositDrawingLink();
    drawingLink.setPadDepositDrawing(depositDrawing);
    var padPermanentDeposit = new PadPermanentDeposit();
    padPermanentDeposit.setReference("my ref");
    drawingLink.setPadPermanentDeposit(padPermanentDeposit);

    var drawingList = List.of(depositDrawing);
    var fileView = new UploadedFileView("1", "1", 0L, "desc", Instant.now(), "#");

    when(padDepositDrawingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(drawingList);
    when(padDepositDrawingLinkRepository.getAllByPadDepositDrawingIn(drawingList))
        .thenReturn(List.of(drawingLink));
    when(padFileService.getUploadedFileViews(pwaApplicationDetail, ApplicationFilePurpose.DEPOSIT_DRAWINGS,
        ApplicationFileLinkStatus.FULL)).thenReturn(List.of(fileView));

    var result = depositDrawingsService.getDepositDrawingSummaryViews(pwaApplicationDetail);
    PermanentDepositDrawingView summaryView = result.get(0);
    assertThat(summaryView.getFileId()).isEqualTo(fileView.getFileId());
    assertThat(summaryView.getDocumentDescription()).isEqualTo(fileView.getFileDescription());
    assertThat(summaryView.getFileName()).isEqualTo(fileView.getFileName());
    assertThat(summaryView.getDepositReferences()).hasSize(1);
    assertThat(summaryView.getReference()).isEqualTo(depositDrawing.getReference());
  }


  @Test
  public void getDepositDrawingSummaryViews_multipleViews() {
    var depositDrawing = new PadDepositDrawing();
    depositDrawing.setReference("ref");
    depositDrawing.setPwaApplicationDetail(pwaApplicationDetail);
    depositDrawing.setFile(new PadFile(pwaApplicationDetail, "1", ApplicationFilePurpose.DEPOSIT_DRAWINGS,
        ApplicationFileLinkStatus.FULL));
    depositDrawing.setId(1);

    var drawingLink1 = new PadDepositDrawingLink();
    drawingLink1.setPadDepositDrawing(depositDrawing);
    var padPermanentDeposit = new PadPermanentDeposit();
    padPermanentDeposit.setReference("my ref");
    drawingLink1.setPadPermanentDeposit(padPermanentDeposit);
    var drawingLink2 = new PadDepositDrawingLink();
    drawingLink2.setPadDepositDrawing(depositDrawing);
    padPermanentDeposit = new PadPermanentDeposit();
    padPermanentDeposit.setReference("my ref2");
    drawingLink2.setPadPermanentDeposit(padPermanentDeposit);

    var drawingList = List.of(depositDrawing);
    var fileView = new UploadedFileView("1", "1", 0L, "desc", Instant.now(), "#");

    when(padDepositDrawingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(drawingList);
    when(padDepositDrawingLinkRepository.getAllByPadDepositDrawingIn(drawingList))
        .thenReturn(List.of(drawingLink1, drawingLink2));
    when(padFileService.getUploadedFileViews(pwaApplicationDetail, ApplicationFilePurpose.DEPOSIT_DRAWINGS,
        ApplicationFileLinkStatus.FULL)).thenReturn(List.of(fileView));

    var result = depositDrawingsService.getDepositDrawingSummaryViews(pwaApplicationDetail);
    PermanentDepositDrawingView summaryView = result.get(0);
    assertThat(summaryView.getFileId()).isEqualTo(fileView.getFileId());
    assertThat(summaryView.getDocumentDescription()).isEqualTo(fileView.getFileDescription());
    assertThat(summaryView.getFileName()).isEqualTo(fileView.getFileName());
    assertThat(summaryView.getDepositReferences()).hasSize(2);
    assertThat(summaryView.getReference()).isEqualTo(depositDrawing.getReference());
  }



  @Test
  public void isDrawingReferenceUniqueWithId_true() {
    var entity = new PadDepositDrawing();
    entity.setId(1);
    assertThat(depositDrawingsService.isDrawingReferenceUnique("my new ref", pwaApplicationDetail)).isTrue();
  }

  @Test
  public void isDrawingReferenceUniqueWithId_false() {
    var entity = new PadDepositDrawing();
    entity.setId(2);
    when(padDepositDrawingRepository.findByPwaApplicationDetailAndReferenceIgnoreCase(pwaApplicationDetail,"myRef")).thenReturn(Optional.of(entity));
    assertThat(depositDrawingsService.isDrawingReferenceUnique("myRef", pwaApplicationDetail)).isFalse();
  }


  @Test
  public void isDrawingReferenceUniqueNoId_true() {
    when(padDepositDrawingRepository.findByPwaApplicationDetailAndReferenceIgnoreCase(pwaApplicationDetail,"myRef")).thenReturn(Optional.empty());
    assertThat(depositDrawingsService.isDrawingReferenceUnique("myRef",  pwaApplicationDetail)).isTrue();
  }

  @Test
  public void isDrawingReferenceUnique_false() {
    var entity = new PadDepositDrawing();
    when(padDepositDrawingRepository.findByPwaApplicationDetailAndReferenceIgnoreCase(pwaApplicationDetail,"myRef")).thenReturn(Optional.of(entity));
    assertThat(depositDrawingsService.isDrawingReferenceUnique("myRef", pwaApplicationDetail)).isFalse();
  }




}