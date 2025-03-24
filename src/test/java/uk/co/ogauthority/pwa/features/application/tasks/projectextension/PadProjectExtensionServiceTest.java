package uk.co.ogauthority.pwa.features.application.tasks.projectextension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformation;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationService;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PadProjectExtensionServiceTest {

  @Mock
  PadProjectInformationService padProjectInformationService;

  @Mock
  ProjectExtensionValidator projectExtensionValidator;

  @Mock
  PadFileManagementService padFileManagementService;

  private PwaApplicationDetail pwaApplicationDetail;

  PadProjectExtensionService projectExtensionService;

  @BeforeEach
  void setup() {
    projectExtensionService = new PadProjectExtensionService(
        padProjectInformationService,
        projectExtensionValidator,
        padFileManagementService
    );

    when(padProjectInformationService.getPadProjectInformationData(any(PwaApplicationDetail.class)))
        .thenReturn(getProjectInformation(PwaApplicationType.INITIAL));
  }

  @Test
  void isProjectExtensionRequired_overMaxPeriod_NotExtendable() {
    var projectInformation = getProjectInformation(PwaApplicationType.INITIAL);

    var applicationDetail = projectInformation.getPwaApplicationDetail();

    var application = applicationDetail.getPwaApplication();
    application.setApplicationType(PwaApplicationType.DECOMMISSIONING);

    applicationDetail.setPwaApplication(application);
    projectInformation.setPwaApplicationDetail(applicationDetail);

    when(padProjectInformationService.getPadProjectInformationData(any(PwaApplicationDetail.class)))
        .thenReturn(projectInformation);

    pwaApplicationDetail = new PwaApplicationDetail();
    assertFalse(projectExtensionService.canShowInTaskList(pwaApplicationDetail));
  }

  @Test
  void isProjectExtensionRequired_underMaxPeriod() {
    var projectInformation = getProjectInformation(PwaApplicationType.INITIAL);
    projectInformation.setLatestCompletionTimestamp(
        LocalDateTime.of(2020, 7, 2, 3, 4, 5)
            .toInstant(ZoneOffset.ofTotalSeconds(0)));
    when(padProjectInformationService.getPadProjectInformationData(any(PwaApplicationDetail.class)))
        .thenReturn(projectInformation);

    pwaApplicationDetail = new PwaApplicationDetail();
    assertFalse(projectExtensionService.canShowInTaskList(pwaApplicationDetail));
  }

  @Test
  void canShowInTaskList_notExtendable() {
    when(padProjectInformationService.getPadProjectInformationData(any(PwaApplicationDetail.class)))
        .thenReturn(getProjectInformation(PwaApplicationType.CAT_2_VARIATION));
    assertFalse(projectExtensionService
        .canShowInTaskList(getProjectInformation(PwaApplicationType.CAT_2_VARIATION).getPwaApplicationDetail()));

    when(padProjectInformationService.getPadProjectInformationData(any(PwaApplicationDetail.class)))
        .thenReturn(getProjectInformation(PwaApplicationType.DEPOSIT_CONSENT));
    assertFalse(projectExtensionService
        .canShowInTaskList(getProjectInformation(PwaApplicationType.DEPOSIT_CONSENT).getPwaApplicationDetail()));

    when(padProjectInformationService.getPadProjectInformationData(any(PwaApplicationDetail.class)))
        .thenReturn(getProjectInformation(PwaApplicationType.OPTIONS_VARIATION));
    assertFalse(projectExtensionService
        .canShowInTaskList(getProjectInformation(PwaApplicationType.OPTIONS_VARIATION).getPwaApplicationDetail()));

    when(padProjectInformationService.getPadProjectInformationData(any(PwaApplicationDetail.class)))
        .thenReturn(getProjectInformation(PwaApplicationType.HUOO_VARIATION));
    assertFalse(projectExtensionService
        .canShowInTaskList(getProjectInformation(PwaApplicationType.HUOO_VARIATION).getPwaApplicationDetail()));

    when(padProjectInformationService.getPadProjectInformationData(any(PwaApplicationDetail.class)))
        .thenReturn(getProjectInformation(PwaApplicationType.DECOMMISSIONING));
    assertFalse(projectExtensionService
        .canShowInTaskList(getProjectInformation(PwaApplicationType.DECOMMISSIONING).getPwaApplicationDetail()));
  }

  @Test
  void canShowInTaskList_NoStartTime() {
    var projectInformation = getProjectInformation(PwaApplicationType.INITIAL);
    projectInformation.setProposedStartTimestamp(null);
    when(padProjectInformationService.getPadProjectInformationData(any(PwaApplicationDetail.class)))
        .thenReturn(projectInformation);

    assertFalse(projectExtensionService
        .canShowInTaskList(getProjectInformation(PwaApplicationType.INITIAL).getPwaApplicationDetail()));
  }

  @Test
  void canShowInTaskList_NoEndTime() {
    var projectInformation = getProjectInformation(PwaApplicationType.INITIAL);
    projectInformation.setLatestCompletionTimestamp(null);
    when(padProjectInformationService.getPadProjectInformationData(any(PwaApplicationDetail.class)))
        .thenReturn(projectInformation);

    assertFalse(projectExtensionService
        .canShowInTaskList(getProjectInformation(PwaApplicationType.INITIAL).getPwaApplicationDetail()));
  }

  @Test
  void canShowInTaskList_underMaxPeriod() {
    var projectInformation = getProjectInformation(PwaApplicationType.INITIAL);
    projectInformation.setLatestCompletionTimestamp(
        LocalDateTime.of(2020, 6, 2, 3, 4, 5)
            .toInstant(ZoneOffset.ofTotalSeconds(0)));
    when(padProjectInformationService.getPadProjectInformationData(any(PwaApplicationDetail.class)))
        .thenReturn(projectInformation);

    assertFalse(projectExtensionService
        .canShowInTaskList(getProjectInformation(PwaApplicationType.INITIAL).getPwaApplicationDetail()));
  }

  @Test
  void isProjectExtensionRequired_overMaxPeriod() {
    pwaApplicationDetail = new PwaApplicationDetail();
    assertTrue(projectExtensionService.canShowInTaskList(pwaApplicationDetail));
  }

  @Test
  void getProjectTimelineGuidance() {
    assertThat(projectExtensionService.getProjectTimelineGuidance(getProjectInformation(PwaApplicationType.INITIAL).getPwaApplicationDetail()))
        .isEqualTo("For example, 31 3 2023 \n" +
            "This must be within 12 months of the proposed start of works date. " +
            "\n Unless prior approval has been received from the Consents and Authorisations Manager.");

    assertThat(projectExtensionService.getProjectTimelineGuidance(getProjectInformation(PwaApplicationType.OPTIONS_VARIATION).getPwaApplicationDetail()))
        .isEqualTo("For example, 31 3 2023 \n" +
            "This must be within 6 months of the proposed start of works date. ");

    assertThat(projectExtensionService.getProjectTimelineGuidance(getProjectInformation(PwaApplicationType.CAT_2_VARIATION).getPwaApplicationDetail()))
        .isEqualTo("For example, 31 3 2023 \n" +
            "This must be within 12 months of the proposed start of works date. ");
  }

  @Test
  void copySectionInformation() {
    pwaApplicationDetail = new PwaApplicationDetail();
    var pwaApplicationDetail2 = new PwaApplicationDetail();

    projectExtensionService.copySectionInformation(pwaApplicationDetail, pwaApplicationDetail2);

    verify(padFileManagementService).copyUploadedFiles(pwaApplicationDetail, pwaApplicationDetail2, FileDocumentType.PROJECT_EXTENSION);
  }

  @Test
  void removeExtensionsForProject() {
    pwaApplicationDetail = new PwaApplicationDetail();
    var pwaApplicationContext = new PwaApplicationContext(pwaApplicationDetail, new WebUserAccount(), Set.of());

    var uploadedFile = new UploadedFile();

    when(padFileManagementService.getUploadedFiles(pwaApplicationDetail, FileDocumentType.PROJECT_EXTENSION))
        .thenReturn(List.of(uploadedFile));

    projectExtensionService.removeExtensionsForProject(pwaApplicationContext);

    verify(padFileManagementService).deleteUploadedFile(uploadedFile);
  }

  private PadProjectInformation getProjectInformation(PwaApplicationType applicationType) {
    var pwaAplication = new PwaApplication();
    pwaAplication.setApplicationType(applicationType);

    var pwaApplicationDetail = new PwaApplicationDetail();
    pwaApplicationDetail.setPwaApplication(pwaAplication);

    var projectInformation = new PadProjectInformation();
    projectInformation.setPwaApplicationDetail(pwaApplicationDetail);
    projectInformation.setProposedStartTimestamp(
        LocalDateTime.of(2020, 1, 2, 3, 4, 5)
        .toInstant(ZoneOffset.ofTotalSeconds(0)));
    projectInformation.setLatestCompletionTimestamp(
        LocalDateTime.of(2022, 1, 2, 3, 4, 5)
          .toInstant(ZoneOffset.ofTotalSeconds(0)));
    return projectInformation;
  }
}
