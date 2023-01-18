package uk.co.ogauthority.pwa.features.application.tasks.projectextension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformation;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@RunWith(MockitoJUnitRunner.class)
public class PadProjectExtensionServiceTest {

  @Mock
  PadFileService padFileService;

  @Mock
  PadProjectInformationService padProjectInformationService;

  @Mock
  ProjectExtensionValidator projectExtensionValidator;

  private PwaApplicationDetail pwaApplicationDetail;

  PadProjectExtensionService projectExtensionService;

  @Before
  public void setup() {
    projectExtensionService = new PadProjectExtensionService(
        padFileService,
        padProjectInformationService,
        projectExtensionValidator);

    when(padProjectInformationService.getPadProjectInformationData(any(PwaApplicationDetail.class)))
        .thenReturn(getProjectInformation());
  }

  @Test
  public void isProjectExtensionRequired_overMaxPeriod() {
    pwaApplicationDetail = new PwaApplicationDetail();
    assertTrue(projectExtensionService.canShowInTaskList(pwaApplicationDetail));
  }

  @Test
  public void isProjectExtensionRequired_overMaxPeriod_NotExtendable() {
    var projectInformation = getProjectInformation();

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
  public void isProjectExtensionRequired_underMaxPeriod() {
    var projectInformation = getProjectInformation();
    projectInformation.setLatestCompletionTimestamp(
        LocalDateTime.of(2020, 7, 2, 3, 4, 5)
            .toInstant(ZoneOffset.ofTotalSeconds(0)));
    when(padProjectInformationService.getPadProjectInformationData(any(PwaApplicationDetail.class)))
        .thenReturn(projectInformation);

    pwaApplicationDetail = new PwaApplicationDetail();
    assertFalse(projectExtensionService.canShowInTaskList(pwaApplicationDetail));
  }

  @Test
  public void isProjectExtensionComplete_verifyServiceInteraction() {
    projectExtensionService.isComplete(getProjectInformation().getPwaApplicationDetail());
    verify(padFileService).getAllByPwaApplicationDetailAndPurpose(getProjectInformation().getPwaApplicationDetail(),
        ApplicationDetailFilePurpose.PROJECT_EXTENSION);
  }

  private PadProjectInformation getProjectInformation() {
    var pwaAplication = new PwaApplication();
    pwaAplication.setApplicationType(PwaApplicationType.INITIAL);

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
