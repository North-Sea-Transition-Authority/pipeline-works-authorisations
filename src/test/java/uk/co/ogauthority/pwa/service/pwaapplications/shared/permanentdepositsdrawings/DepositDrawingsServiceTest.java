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
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdepositdrawings.PadDepositDrawingLink;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDeposit;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositDrawingsForm;
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
    var form = new PermanentDepositDrawingsForm();
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "desc", Instant.now())));
    form.setReference("ref");
    form.setSelectedDeposits(Set.of("1"));

    var padFile = new PadFile(pwaApplicationDetail, "1", ApplicationFilePurpose.DEPOSIT_DRAWINGS, ApplicationFileLinkStatus.FULL);
    when(padFileService.getPadFileByPwaApplicationDetailAndFileId(pwaApplicationDetail, "1")).thenReturn(padFile);

    var padPermanentDeposit = new PadPermanentDeposit();
    padPermanentDeposit.setId(1);
    when(padPermanentDepositRepository.findById(Integer.parseInt("1"))).thenReturn(Optional.of(padPermanentDeposit));
    depositDrawingsService.addDrawing(pwaApplicationDetail, form);

    var captor = ArgumentCaptor.forClass(PadDepositDrawing.class);
    verify(padDepositDrawingRepository, times(1)).save(captor.capture());

    assertThat(captor.getValue()).extracting(PadDepositDrawing::getFile, PadDepositDrawing::getReference, PadDepositDrawing::getPwaApplicationDetail)
        .containsExactly(padFile, "ref", pwaApplicationDetail);

    var captorDrawingLink = ArgumentCaptor.forClass(PadDepositDrawingLink.class);
    verify(padDepositDrawingLinkRepository, times(1)).save(captorDrawingLink.capture());
  }



  @Test
  public void isDrawingReferenceUniqueWithId_true() {
    var entity = new PadDepositDrawing();
    entity.setId(1);
    when(padDepositDrawingRepository.findByPwaApplicationDetailAndReferenceIgnoreCase(pwaApplicationDetail,"my existing ref")).thenReturn(Optional.of(entity));
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