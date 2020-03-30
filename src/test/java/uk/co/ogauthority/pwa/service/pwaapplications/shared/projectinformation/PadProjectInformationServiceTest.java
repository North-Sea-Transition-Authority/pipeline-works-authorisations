package uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformationFile;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.initial.ProjectInformationForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadProjectInformationRepository;

@RunWith(MockitoJUnitRunner.class)
public class PadProjectInformationServiceTest {

  private final static String FILE_ID = "1234567u8oplkjmnhbgvfc";

  @Mock
  private PadProjectInformationRepository padProjectInformationRepository;

  @Mock
  private ProjectInformationFileService projectInformationFileService;

  @Mock
  private ProjectInformationEntityMappingService projectInformationEntityMappingService;


  private PadProjectInformationService service;
  private PadProjectInformation padProjectInformation;
  private ProjectInformationForm form;
  private PwaApplicationDetail pwaApplicationDetail;
  private LocalDate date;
  private WebUserAccount user = new WebUserAccount(1);

  @Before
  public void setUp() {
    service = new PadProjectInformationService(
        padProjectInformationRepository,
        projectInformationFileService,
        projectInformationEntityMappingService
    );

    date = LocalDate.now();

    pwaApplicationDetail = new PwaApplicationDetail();
    padProjectInformation = ProjectInformationTestUtils.buildEntity(date);
    padProjectInformation.setPwaApplicationDetail(pwaApplicationDetail);
    form = ProjectInformationTestUtils.buildForm(date);

  }


  @Test
  public void getPadProjectInformationData_WithExisting() {
    when(padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(
        Optional.of(padProjectInformation));
    var result = service.getPadProjectInformationData(pwaApplicationDetail);
    assertThat(result).isEqualTo(padProjectInformation);
  }

  @Test
  public void getPadProjectInformationData_NoExisting() {
    when(padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.empty());
    var result = service.getPadProjectInformationData(pwaApplicationDetail);
    assertThat(result).isNotEqualTo(padProjectInformation);
    assertThat(result.getPwaApplicationDetail()).isEqualTo(pwaApplicationDetail);
  }


  @Test
  public void saveEntityUsingForm_verifyServiceInteractions() {

    service.saveEntityUsingForm(padProjectInformation, form, user);

    verify(projectInformationEntityMappingService, times(1)).setEntityValuesUsingForm(padProjectInformation, form);
    verify(projectInformationFileService, times(1)).updateOrDeleteLinkedFilesUsingForm(
        this.padProjectInformation.getPwaApplicationDetail(),
        form,
        user
    );
    verify(padProjectInformationRepository, times(1)).save(padProjectInformation);

  }

  @Test
  public void mapEntityToForm_verifyServiceInteractions_andUploadedFilesGetUpdated() {
    var returnedList = List.of(new UploadFileWithDescriptionForm(FILE_ID, "desc", Instant.now()));
    when(projectInformationFileService.getUploadedFileListAsFormList(pwaApplicationDetail,
        ApplicationFileLinkStatus.FULL))
        .thenReturn(returnedList);

    service.mapEntityToForm(padProjectInformation, form, ApplicationFileLinkStatus.FULL);

    verify(projectInformationEntityMappingService, times(1)).mapProjectInformationDataToForm(padProjectInformation,
        form);
    verify(projectInformationFileService, times(1)).getUploadedFileListAsFormList(
        pwaApplicationDetail,
        ApplicationFileLinkStatus.FULL
    );

    assertThat(form.getUploadedFileWithDescriptionForms()).isEqualTo(returnedList);

  }

  @Test
  public void getUpdatedProjectInformationFileViewsWhenFileOnForm_verifyServiceInteraction() {
    service.getUpdatedProjectInformationFileViewsWhenFileOnForm(pwaApplicationDetail, form);
    verify(projectInformationFileService, times(1)).getUpdatedProjectInformationFileViewsWhenFileOnForm(
        pwaApplicationDetail, form
    );

  }

  @Test
  public void getProjectInformationFile_verifyServiceInteraction() {
    service.getProjectInformationFile(FILE_ID, pwaApplicationDetail);
    verify(projectInformationFileService, times(1)).getProjectInformationFile(
        FILE_ID, pwaApplicationDetail
    );
  }


  @Test
  public void deleteUploadedFileLink_verifyServiceInteraction() {
    var testFile = new PadProjectInformationFile();
    when(projectInformationFileService.getProjectInformationFile(FILE_ID, pwaApplicationDetail)).thenReturn(
        testFile
    );
    service.deleteUploadedFileLink(FILE_ID, pwaApplicationDetail);

    verify(projectInformationFileService, times(1)).getProjectInformationFile(
        FILE_ID,
        pwaApplicationDetail
    );

    verify(projectInformationFileService, times(1)).deleteProjectInformationFileLink(
        testFile
    );

  }

  @Test
  public void createUploadedFileLink_verifyServiceInteraction() {
    service.createUploadedFileLink(FILE_ID, pwaApplicationDetail);
    verify(projectInformationFileService, times(1)).createAndSaveProjectInformationFile(
        pwaApplicationDetail, FILE_ID
    );
  }

}