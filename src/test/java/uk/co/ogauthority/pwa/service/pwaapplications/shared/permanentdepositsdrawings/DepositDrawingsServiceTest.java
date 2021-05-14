package uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdepositsdrawings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdepositdrawings.PadDepositDrawing;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdepositdrawings.PadDepositDrawingLink;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDeposit;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDepositTestUtil;
import uk.co.ogauthority.pwa.model.form.fds.ErrorItem;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositDrawingForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PermanentDepositDrawingView;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.permanentdepositdrawings.PadDepositDrawingLinkRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.permanentdepositdrawings.PadDepositDrawingRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.fileupload.PadFileTestUtil;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.DepositDrawingsService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.PermanentDepositService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.PermanentDepositsDrawingValidator;


@RunWith(MockitoJUnitRunner.class)
public class DepositDrawingsServiceTest {

  private DepositDrawingsService depositDrawingsService;

  @Mock
  private PermanentDepositService permanentDepositService;

  @Mock
  private PadDepositDrawingRepository padDepositDrawingRepository;

  @Mock
  private PadDepositDrawingLinkRepository padDepositDrawingLinkRepository;

  @Mock
  private PadFileService padFileService;

  @Mock
  private SpringValidatorAdapter springValidatorAdapter;

  @Mock
  private PermanentDepositsDrawingValidator validator;

  private PwaApplicationDetail pwaApplicationDetail;


  @Before
  public void setUp() {
    depositDrawingsService = new DepositDrawingsService(padDepositDrawingRepository,
        padDepositDrawingLinkRepository, validator, springValidatorAdapter, padFileService, permanentDepositService);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 100);
  }


  @Test
  public void addDrawing() {
    var form = new PermanentDepositDrawingForm();
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "desc", Instant.now())));
    form.setReference("ref");
    form.setSelectedDeposits(Set.of("1"));

    var padFile = new PadFile(pwaApplicationDetail, "1", ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS, ApplicationFileLinkStatus.FULL);
    when(padFileService.getPadFileByPwaApplicationDetailAndFileId(pwaApplicationDetail, "1")).thenReturn(padFile);

    var padPermanentDeposit = new PadPermanentDeposit();
    padPermanentDeposit.setId(1);
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
  public void editDrawing() {
    var form = new PermanentDepositDrawingForm();
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "desc", Instant.now())));
    form.setReference("ref");
    form.setSelectedDeposits(Set.of("2"));

    var padFile = new PadFile(pwaApplicationDetail, "1", ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS, ApplicationFileLinkStatus.FULL);
    when(padFileService.getPadFileByPwaApplicationDetailAndFileId(pwaApplicationDetail, "1")).thenReturn(padFile);

    var depositDrawing = new PadDepositDrawing();
    depositDrawing.setId(1);
    when(padDepositDrawingRepository.findById(1)).thenReturn(Optional.of(depositDrawing));

    var padPermanentDeposit = new PadPermanentDeposit();
    padPermanentDeposit.setId(2);
    when(permanentDepositService.getDepositById(2)).thenReturn(Optional.of(padPermanentDeposit));
    when(padDepositDrawingRepository.save(any(PadDepositDrawing.class))).thenReturn(depositDrawing);
    depositDrawingsService.editDepositDrawing(1, pwaApplicationDetail, form, new WebUserAccount());


    var captor = ArgumentCaptor.forClass(PadDepositDrawing.class);
    verify(padDepositDrawingRepository, times(1)).save(captor.capture());

    assertThat(captor.getValue()).extracting(PadDepositDrawing::getFile, PadDepositDrawing::getReference, PadDepositDrawing::getPwaApplicationDetail)
        .containsExactly(padFile, "ref", pwaApplicationDetail);

    var captorDrawingLink = ArgumentCaptor.forClass(PadDepositDrawingLink.class);
    verify(padDepositDrawingLinkRepository, times(1)).save(captorDrawingLink.capture());

    assertThat(captorDrawingLink.getValue()).extracting(PadDepositDrawingLink::getPadDepositDrawing, PadDepositDrawingLink::getPadPermanentDeposit)
        .containsExactly(depositDrawing, padPermanentDeposit);
  }


  @Test
  public void getDepositDrawingSummaryViews() {
    var depositDrawing = new PadDepositDrawing();
    depositDrawing.setReference("ref");
    depositDrawing.setPwaApplicationDetail(pwaApplicationDetail);
    depositDrawing.setFile(new PadFile(pwaApplicationDetail, "1", ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS,
        ApplicationFileLinkStatus.FULL));
    depositDrawing.setId(1);

    var drawingLink = new PadDepositDrawingLink();
    drawingLink.setPadDepositDrawing(depositDrawing);
    var padPermanentDeposit = new PadPermanentDeposit();
    padPermanentDeposit.setReference("my ref");
    drawingLink.setPadPermanentDeposit(padPermanentDeposit);

    var fileView = new UploadedFileView("1", "1", 0L, "desc", Instant.now(), "#");

    when(padDepositDrawingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(depositDrawing));
    when(padDepositDrawingLinkRepository.getAllByPadDepositDrawingIn(List.of(depositDrawing)))
        .thenReturn(List.of(drawingLink));
    when(padFileService.getUploadedFileViews(pwaApplicationDetail, ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS,
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
  public void getDepositDrawingSummaryViews_viewsWithAndWithoutDeposits() {
    var depositDrawing = new PadDepositDrawing();
    depositDrawing.setReference("drawing ref");
    depositDrawing.setPwaApplicationDetail(pwaApplicationDetail);
    depositDrawing.setFile(new PadFile(pwaApplicationDetail, "1", ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS,
        ApplicationFileLinkStatus.FULL));
    depositDrawing.setId(1);


    var depositDrawing2 = new PadDepositDrawing();
    depositDrawing2.setReference("drawing ref 2");
    depositDrawing2.setPwaApplicationDetail(pwaApplicationDetail);
    depositDrawing2.setFile(new PadFile(pwaApplicationDetail, "1", ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS,
        ApplicationFileLinkStatus.FULL));
    depositDrawing2.setId(2);

    var drawingLink1 = new PadDepositDrawingLink();
    drawingLink1.setPadDepositDrawing(depositDrawing);
    var padPermanentDeposit = new PadPermanentDeposit();
    padPermanentDeposit.setReference("my ref");
    drawingLink1.setPadPermanentDeposit(padPermanentDeposit);
    var drawingLink2 = new PadDepositDrawingLink();
    drawingLink2.setPadDepositDrawing(depositDrawing);
    var padPermanentDeposit2 = new PadPermanentDeposit();
    padPermanentDeposit2.setReference("my ref2");
    drawingLink2.setPadPermanentDeposit(padPermanentDeposit2);

    var fileView = new UploadedFileView("1", "1", 0L, "desc", Instant.now(), "#");

    when(padDepositDrawingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(depositDrawing, depositDrawing2));
    when(padDepositDrawingLinkRepository.getAllByPadDepositDrawingIn(List.of(depositDrawing, depositDrawing2)))
        .thenReturn(List.of(drawingLink1, drawingLink2));
    when(padFileService.getUploadedFileViews(pwaApplicationDetail, ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS,
        ApplicationFileLinkStatus.FULL)).thenReturn(List.of(fileView));

    var result = depositDrawingsService.getDepositDrawingSummaryViews(pwaApplicationDetail);
    PermanentDepositDrawingView summaryView = result.get(0);
    assertThat(summaryView.getFileId()).isEqualTo(fileView.getFileId());
    assertThat(summaryView.getDocumentDescription()).isEqualTo(fileView.getFileDescription());
    assertThat(summaryView.getFileName()).isEqualTo(fileView.getFileName());
    assertThat(summaryView.getDepositReferences()).hasSize(2);
    assertThat(summaryView.getReference()).isEqualTo(depositDrawing.getReference());

    summaryView = result.get(1);
    assertThat(summaryView.getDepositReferences()).hasSize(0);
  }

  @Test
  public void removeDepositFromDrawing() {
    var deposit = new PadPermanentDeposit();
    when(padDepositDrawingLinkRepository.getAllByPadPermanentDepositIn(List.of(deposit))).thenReturn(List.of(new PadDepositDrawingLink()));
    depositDrawingsService.removeDepositFromDrawing(deposit);
    verify(padDepositDrawingLinkRepository, times(1)).deleteAll(any());
  }


  @Test
  public void removeDrawingAndFile_noEntityFound() {
    var entity = new PadDepositDrawing();
    when(padDepositDrawingRepository.findById(1)).thenReturn(Optional.of(entity));
    when(padDepositDrawingLinkRepository.getAllByPadDepositDrawing(entity)).thenReturn(List.of(new PadDepositDrawingLink()));
    depositDrawingsService.removeDrawingAndFile(1, new WebUserAccount());
    verify(padDepositDrawingLinkRepository, times(1)).deleteAll(any());
    verify(padDepositDrawingRepository, times(1)).delete(any());
  }

  @Test
  public void removeDrawingAndFile_noFileFound() {
    var entity = new PadDepositDrawing();
    entity.setId(1);
    entity.setReference("ref");
    when(padDepositDrawingRepository.findById(1)).thenReturn(Optional.of(entity));

    var drawingLink = new PadDepositDrawingLink();
    drawingLink.setPadDepositDrawing(entity);
    when(padDepositDrawingLinkRepository.getAllByPadDepositDrawing(entity)).thenReturn(List.of(new PadDepositDrawingLink()));
    depositDrawingsService.removeDrawingAndFile(1, new WebUserAccount());

    verify(padDepositDrawingLinkRepository, times(1)).deleteAll(any());
    verify(padDepositDrawingRepository, times(1)).delete(any());
    verify(padFileService,times(0)).processFileDeletion(any(), any());
  }


  @Test
  public void getDepositDrawingSummaryScreenValidationResult_depositsDoNotAllHaveDrawings_invalid() {

    var deposit = PadPermanentDepositTestUtil.createPadDepositWithAllFieldsPopulated(pwaApplicationDetail);
    when(permanentDepositService.getPermanentDeposits(pwaApplicationDetail)).thenReturn(List.of(deposit));

    var summaryResult = depositDrawingsService.getDepositDrawingSummaryScreenValidationResult(pwaApplicationDetail);

    assertThat(summaryResult.isSectionComplete()).isFalse();
    assertThat(summaryResult.getSectionIncompleteError()).isNotEmpty();
  }

  @Test
  public void getDepositDrawingSummaryScreenValidationResult_drawingsIncomplete_invalid() {

    var padFile = PadFileTestUtil.createPadFileWithRandomFileIdAndData(
        pwaApplicationDetail, ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS).getPadFile();
    var depositDrawing = PadPermanentDepositTestUtil.createPadDepositDrawing(pwaApplicationDetail, padFile);
    when(padDepositDrawingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(List.of(depositDrawing));

    var fileView = new UploadedFileView("1","_", 1L,"_", Instant.now(),"_");
    when(padFileService.getUploadedFileView(pwaApplicationDetail, padFile.getFileId(),
        ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS, ApplicationFileLinkStatus.FULL)).thenReturn(fileView);

    doAnswer(invocation -> {
      BindingResult result = invocation.getArgument(1);
      result.rejectValue("selectedDeposits", "fake.code", "fake message");
      return result;
    }).when(validator).validate(any(), any(), any(), any(), any());

    var summaryResult = depositDrawingsService.getDepositDrawingSummaryScreenValidationResult(pwaApplicationDetail);

    assertThat(summaryResult.isSectionComplete()).isFalse();
    assertThat(summaryResult.getInvalidObjectIds()).containsExactly(String.valueOf(depositDrawing.getId()));
    assertThat(summaryResult.getIdPrefix()).isEqualTo("deposit-drawing-");
    assertThat(summaryResult.getErrorItems()).extracting(ErrorItem::getDisplayOrder, ErrorItem::getFieldName, ErrorItem::getErrorMessage)
        .containsExactly(
            tuple(1, "deposit-drawing-" + depositDrawing.getId(), depositDrawing.getReference() + " has errors")
        );
  }

  @Test
  public void getDepositDrawingSummaryScreenValidationResult_depositsAllHaveDrawings_allDrawingsComplete_valid() {

    var deposit = PadPermanentDepositTestUtil.createPadDepositWithAllFieldsPopulated(pwaApplicationDetail);
    when(permanentDepositService.getPermanentDeposits(pwaApplicationDetail)).thenReturn(List.of(deposit));
    when(padDepositDrawingLinkRepository.getAllByPadPermanentDeposit(deposit)).thenReturn(List.of(new PadDepositDrawingLink()));

    var padFile = PadFileTestUtil.createPadFileWithRandomFileIdAndData(
        pwaApplicationDetail, ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS).getPadFile();
    var depositDrawing = PadPermanentDepositTestUtil.createPadDepositDrawing(pwaApplicationDetail, padFile);
    when(padDepositDrawingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(List.of(depositDrawing));

    var fileView = new UploadedFileView("1","_", 1L,"_", Instant.now(),"_");
    when(padFileService.getUploadedFileView(pwaApplicationDetail, padFile.getFileId(),
        ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS, ApplicationFileLinkStatus.FULL)).thenReturn(fileView);

    var summaryResult = depositDrawingsService.getDepositDrawingSummaryScreenValidationResult(pwaApplicationDetail);

    assertThat(summaryResult.isSectionComplete()).isTrue();
    assertThat(summaryResult.getInvalidObjectIds()).isEmpty();
    assertThat(summaryResult.getErrorItems()).isEmpty();
  }


  @Test
  public void isComplete_valid() {
    var depositDrawing = new PadDepositDrawing();
    var padFile = new PadFile();
    padFile.setId(1);
    padFile.setFileId("1");
    depositDrawing.setFile(padFile);
    when(padDepositDrawingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(List.of(depositDrawing));

    var fileView = new UploadedFileView("1","_", 1L,"_", Instant.now(),"_");
    when(padFileService.getUploadedFileView(pwaApplicationDetail,"1",
        ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS, ApplicationFileLinkStatus.FULL)).thenReturn(fileView);

    var deposit = new PadPermanentDeposit();
    deposit.setId(1);
    deposit.setReference("ref 1");
    when(permanentDepositService.getPermanentDeposits(pwaApplicationDetail)).thenReturn(List.of(deposit));
    when(padDepositDrawingLinkRepository.getAllByPadPermanentDeposit(deposit)).thenReturn(List.of(new PadDepositDrawingLink()));

    assertTrue(depositDrawingsService.isComplete(pwaApplicationDetail));
  }

  @Test
  public void isComplete_noFileLinkedInvalid() {
    var deposit1 = new PadPermanentDeposit();
    deposit1.setId(1);
    var deposit2 = new PadPermanentDeposit();
    deposit2.setId(2);

    assertFalse(depositDrawingsService.isComplete(pwaApplicationDetail));
  }

  @Test
  public void isComplete_depositWithoutDrawing() {
    var deposit1 = new PadPermanentDeposit();
    deposit1.setId(1);
    var deposit2 = new PadPermanentDeposit();
    deposit2.setId(2);

    var depositLink = new PadDepositDrawingLink();
    depositLink.setPadPermanentDeposit(deposit1);
    when(permanentDepositService.getPermanentDeposits(pwaApplicationDetail)).thenReturn(List.of(deposit1, deposit2));
    when(padDepositDrawingLinkRepository.getAllByPadPermanentDeposit(deposit1)).thenReturn(List.of());

    assertFalse(depositDrawingsService.isComplete(pwaApplicationDetail));
  }

  @Test
  public void isComplete_valid_multipleDrawingsOnSameDeposit() {
    var depositDrawing = new PadDepositDrawing();
    var padFile = new PadFile();
    padFile.setId(1);
    padFile.setFileId("1");
    depositDrawing.setFile(padFile);
    when(padDepositDrawingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(List.of(depositDrawing));

    var fileView = new UploadedFileView("1","_",Long.valueOf(1),"_", Instant.now(),"_");
    when(padFileService.getUploadedFileView(pwaApplicationDetail,"1",
        ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS, ApplicationFileLinkStatus.FULL)).thenReturn(fileView);

    var deposit1 = new PadPermanentDeposit();
    deposit1.setId(1);
    var deposit2 = new PadPermanentDeposit();
    deposit2.setId(2);
    when(permanentDepositService.getPermanentDeposits(pwaApplicationDetail)).thenReturn(List.of(deposit1, deposit2));

    var depositLink1 = new PadDepositDrawingLink();
    depositLink1.setPadPermanentDeposit(deposit1);
    var depositLink2 = new PadDepositDrawingLink();
    depositLink2.setPadPermanentDeposit(deposit2);
    depositLink2.setPadPermanentDeposit(deposit1);
    when(padDepositDrawingLinkRepository.getAllByPadPermanentDeposit(deposit1)).thenReturn(List.of(depositLink1, depositLink2));
    when(padDepositDrawingLinkRepository.getAllByPadPermanentDeposit(deposit2)).thenReturn(List.of(depositLink2));

    assertTrue(depositDrawingsService.isComplete(pwaApplicationDetail));
  }


  @Test
  public void isDrawingReferenceUniqueWithId_true() {
    assertThat(depositDrawingsService.isDrawingReferenceUnique("my new ref", 1, pwaApplicationDetail)).isTrue();
  }

  @Test
  public void isDrawingReferenceUniqueWithId_false() {
    var entity = new PadDepositDrawing();
    entity.setId(2);
    when(padDepositDrawingRepository.findByPwaApplicationDetailAndReferenceIgnoreCase(pwaApplicationDetail,"myRef")).thenReturn(Optional.of(entity));
    assertThat(depositDrawingsService.isDrawingReferenceUnique("myRef", 1, pwaApplicationDetail)).isFalse();
  }


  @Test
  public void isDrawingReferenceUniqueNoId_true() {
    when(padDepositDrawingRepository.findByPwaApplicationDetailAndReferenceIgnoreCase(pwaApplicationDetail,"myRef")).thenReturn(Optional.empty());
    assertThat(depositDrawingsService.isDrawingReferenceUnique("myRef", null, pwaApplicationDetail)).isTrue();
  }

  @Test
  public void isDrawingReferenceUnique_false() {
    var entity = new PadDepositDrawing();
    when(padDepositDrawingRepository.findByPwaApplicationDetailAndReferenceIgnoreCase(pwaApplicationDetail,"myRef")).thenReturn(Optional.of(entity));
    assertThat(depositDrawingsService.isDrawingReferenceUnique("myRef", null, pwaApplicationDetail)).isFalse();
  }

  @Test
  public void cleanupData() {

    var drawing1 = new PadDepositDrawing();
    var file1 = new PadFile();
    file1.setId(1);
    drawing1.setFile(file1);

    var drawing2 = new PadDepositDrawing();
    var file2 = new PadFile();
    file2.setId(2);
    drawing2.setFile(file2);

    when(padDepositDrawingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(List.of(drawing1, drawing2));

    depositDrawingsService.cleanupData(pwaApplicationDetail);

    verify(padFileService, times(1)).cleanupFiles(pwaApplicationDetail, ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS, List.of(1, 2));

  }

  @Test
  public void getDepositAndDrawingMapForDeposits() {
    var deposit1 = new PadPermanentDeposit();
    deposit1.setId(1);
    var deposit2 = new PadPermanentDeposit();
    deposit2.setId(2);

    var depositDrawing1 = new PadDepositDrawing();
    var depositDrawing2 = new PadDepositDrawing();

    var deposit1AndDrawing1 = PadPermanentDepositTestUtil.createPadDepositDrawingLink(deposit1, depositDrawing1);
    var deposit1AndDrawing2 = PadPermanentDepositTestUtil.createPadDepositDrawingLink(deposit1, depositDrawing2);
    var deposit2AndDrawing1 = PadPermanentDepositTestUtil.createPadDepositDrawingLink(deposit2, depositDrawing1);

    when(padDepositDrawingLinkRepository.getAllByPadPermanentDepositIn(List.of(deposit1, deposit2)))
        .thenReturn(List.of(deposit1AndDrawing1, deposit1AndDrawing2, deposit2AndDrawing1));

    var depositForDepositDrawingsMap = depositDrawingsService.getDepositAndDrawingLinksMapForDeposits(List.of(deposit1, deposit2));

    assertThat(depositForDepositDrawingsMap.get(deposit1)).isEqualTo(List.of(deposit1AndDrawing1,deposit1AndDrawing2));
    assertThat(depositForDepositDrawingsMap.get(deposit2)).isEqualTo(List.of(deposit2AndDrawing1));
  }

  @Test
  public void removeDepositsFromDrawings_serviceInteraction() {
    var deposit = new PadPermanentDeposit();
    depositDrawingsService.removeDepositsFromDrawings(List.of(deposit));
    verify(padDepositDrawingLinkRepository, times(1)).getAllByPadPermanentDepositIn(List.of(deposit));
  }


  @Test
  public void canShowInTaskList_whenPermanentDepositsTaskShown_andPermanentDepositsAdded(){
    when(permanentDepositService.canShowInTaskList(pwaApplicationDetail)).thenReturn(true);
    when(permanentDepositService.hasPermanentDepositBeenMade(pwaApplicationDetail)).thenReturn(true);

    assertThat(depositDrawingsService.canShowInTaskList(pwaApplicationDetail)).isTrue();

  }

  @Test
  public void canShowInTaskList_whenPermanentDepositsTaskIsNotShown_andLegacyPermanentDepositsAdded(){
    when(permanentDepositService.hasPermanentDepositBeenMade(pwaApplicationDetail)).thenReturn(true);
    when(permanentDepositService.canShowInTaskList(pwaApplicationDetail)).thenReturn(false);

    assertThat(depositDrawingsService.canShowInTaskList(pwaApplicationDetail)).isFalse();

  }
}