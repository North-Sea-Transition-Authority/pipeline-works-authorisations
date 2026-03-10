package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformation;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationService;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.ProjectInformationTestUtils;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.internal.PwaAppChargeRequest;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.internal.PwaAppChargeRequestItem;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.internal.PwaAppChargeRequestItemRepository;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.PwaApplicationFeeType;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class ApplicationChargeRequestMetadataServiceTest {

  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Mock
  private PadProjectInformationService padProjectInformationService;

  @Mock
  private PwaAppChargeRequestItemRepository pwaAppChargeRequestItemRepository;

  private ApplicationChargeRequestMetadataService applicationChargeRequestMetadataService;

  private final PwaApplicationDetail detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
  private final PortalOrganisationUnit organisationUnit = PortalOrganisationTestUtils.generateOrganisationUnit(1, "SHELL U.K. LIMITED");
  private final PadProjectInformation projectInfo = ProjectInformationTestUtils.buildEntity(LocalDate.now());
  private final PwaAppChargeRequest pwaAppChargeRequest = PwaAppChargeRequestTestUtil.createChargeRequest(detail.getPwaApplication());

  @BeforeEach
  void setUp() throws Exception {

    applicationChargeRequestMetadataService = new ApplicationChargeRequestMetadataService(
        portalOrganisationsAccessor,
        padProjectInformationService,
        pwaAppChargeRequestItemRepository
    );

    when(portalOrganisationsAccessor.getOrganisationUnitById(OrganisationUnitId.fromInt(organisationUnit.getOuId())))
        .thenReturn(Optional.of(organisationUnit));

    when(padProjectInformationService.getPadProjectInformationData(detail)).thenReturn(projectInfo);

    detail.getPwaApplication().setApplicantOrganisationUnitId(OrganisationUnitId.fromInt(organisationUnit.getOuId()));
  }

  @Test
  void getMetadataMapForDetail() {
    var defaultChargeRequestItem = new PwaAppChargeRequestItem(null, PwaApplicationFeeType.DEFAULT, "Fee item 1", 10000);
    var fastTrackChargeRequestItem = new PwaAppChargeRequestItem(null, PwaApplicationFeeType.FAST_TRACK, "Fee item 2", 5000);

    when(pwaAppChargeRequestItemRepository.findAllByPwaAppChargeRequest(pwaAppChargeRequest))
        .thenReturn(List.of(defaultChargeRequestItem, fastTrackChargeRequestItem));

    var resultMap = applicationChargeRequestMetadataService.getMetadataMapForDetail(detail, pwaAppChargeRequest);

    var expectedMap = Map.of(
        "Applicant organisation", organisationUnit.getName(),
        "Applicant org reg number", organisationUnit.getRegisteredNumber(),
        "Project name", projectInfo.getProjectName(),
        "Fee line 1 category", "DEFAULT/INITIAL",
        "Fee line 1 description", "Fee item 1",
        "Fee line 1 amount pence", "10000",
        "Fee line 2 category", "FAST_TRACK/INITIAL",
        "Fee line 2 description", "Fee item 2",
        "Fee line 2 amount pence", "5000"
    );

    assertThat(resultMap).isEqualTo(expectedMap);
  }

  @Test
  void getMetadataMapForDetail_organisationUnitDoesNotExist() {
    var chargeRequestItem = new PwaAppChargeRequestItem(null, PwaApplicationFeeType.DEFAULT, "Fee item 1", 10000);

    when(portalOrganisationsAccessor.getOrganisationUnitById(OrganisationUnitId.fromInt(organisationUnit.getOuId())))
        .thenReturn(Optional.empty());
    when(pwaAppChargeRequestItemRepository.findAllByPwaAppChargeRequest(pwaAppChargeRequest))
        .thenReturn(List.of(chargeRequestItem));

    var resultMap = applicationChargeRequestMetadataService.getMetadataMapForDetail(detail, pwaAppChargeRequest);

    var expectedMap = Map.of(
        "Applicant organisation", "",
        "Applicant org reg number", "",
        "Project name", projectInfo.getProjectName(),
        "Fee line 1 category", "DEFAULT/INITIAL",
        "Fee line 1 description", "Fee item 1",
        "Fee line 1 amount pence", "10000"
    );

    assertThat(resultMap).isEqualTo(expectedMap);
  }

  @Test
  void getMetadataMapForDetail_organisationNameIsNull() {
    var chargeRequestItem = new PwaAppChargeRequestItem(null, PwaApplicationFeeType.DEFAULT, "Fee item 1", 10000);
    var orgUnit = PortalOrganisationTestUtils.generateOrganisationUnit(1, null, "12345678", null);

    when(portalOrganisationsAccessor.getOrganisationUnitById(OrganisationUnitId.fromInt(orgUnit.getOuId())))
        .thenReturn(Optional.of(orgUnit));
    when(pwaAppChargeRequestItemRepository.findAllByPwaAppChargeRequest(pwaAppChargeRequest))
        .thenReturn(List.of(chargeRequestItem));

    detail.getPwaApplication().setApplicantOrganisationUnitId(OrganisationUnitId.fromInt(orgUnit.getOuId()));

    var resultMap = applicationChargeRequestMetadataService.getMetadataMapForDetail(detail, pwaAppChargeRequest);

    var expectedMap = Map.of(
        "Applicant organisation", "",
        "Applicant org reg number", "12345678",
        "Project name", projectInfo.getProjectName(),
        "Fee line 1 category", "DEFAULT/INITIAL",
        "Fee line 1 description", "Fee item 1",
        "Fee line 1 amount pence", "10000"
    );

    assertThat(resultMap).isEqualTo(expectedMap);
  }

  @Test
  void getMetadataMapForDetail_registeredNumberIsNull_foreignRegisteredNumberIsNotNull() {
    var chargeRequestItem = new PwaAppChargeRequestItem(null, PwaApplicationFeeType.DEFAULT, "Fee item 1", 10000);
    var orgUnit = PortalOrganisationTestUtils.generateOrganisationUnit(1, "TEST ORG", null, "FR987654");

    when(portalOrganisationsAccessor.getOrganisationUnitById(OrganisationUnitId.fromInt(orgUnit.getOuId())))
        .thenReturn(Optional.of(orgUnit));
    when(pwaAppChargeRequestItemRepository.findAllByPwaAppChargeRequest(pwaAppChargeRequest))
        .thenReturn(List.of(chargeRequestItem));

    detail.getPwaApplication().setApplicantOrganisationUnitId(OrganisationUnitId.fromInt(orgUnit.getOuId()));

    var resultMap = applicationChargeRequestMetadataService.getMetadataMapForDetail(detail, pwaAppChargeRequest);

    var expectedMap = Map.of(
        "Applicant organisation", "TEST ORG",
        "Applicant org reg number", "FR987654",
        "Project name", projectInfo.getProjectName(),
        "Fee line 1 category", "DEFAULT/INITIAL",
        "Fee line 1 description", "Fee item 1",
        "Fee line 1 amount pence", "10000"
    );

    assertThat(resultMap).isEqualTo(expectedMap);
  }

  @Test
  void getMetadataMapForDetail_registeredNumberIsNull_foreignRegisteredNumberIsNull() {
    var chargeRequestItem = new PwaAppChargeRequestItem(null, PwaApplicationFeeType.DEFAULT, "Fee item 1", 10000);
    var orgUnit = PortalOrganisationTestUtils.generateOrganisationUnit(1, "TEST ORG", null, null);

    when(portalOrganisationsAccessor.getOrganisationUnitById(OrganisationUnitId.fromInt(orgUnit.getOuId())))
        .thenReturn(Optional.of(orgUnit));
    when(pwaAppChargeRequestItemRepository.findAllByPwaAppChargeRequest(pwaAppChargeRequest))
        .thenReturn(List.of(chargeRequestItem));

    detail.getPwaApplication().setApplicantOrganisationUnitId(OrganisationUnitId.fromInt(orgUnit.getOuId()));

    var resultMap = applicationChargeRequestMetadataService.getMetadataMapForDetail(detail, pwaAppChargeRequest);

    var expectedMap = Map.of(
        "Applicant organisation", "TEST ORG",
        "Applicant org reg number", "",
        "Project name", projectInfo.getProjectName(),
        "Fee line 1 category", "DEFAULT/INITIAL",
        "Fee line 1 description", "Fee item 1",
        "Fee line 1 amount pence", "10000"
    );

    assertThat(resultMap).isEqualTo(expectedMap);
  }

  private String getRepeatedValue(String value) {
    return StringUtils.repeat(value, 26);
  }

  private String getTruncatedValue(String value) {
    return StringUtils.repeat(value, 24) + value.charAt(0) + "...";
  }

  @Test
  void getMetadataMapForDetail_longValuesTruncated() throws IllegalAccessException {
    var chargeRequestItem = new PwaAppChargeRequestItem(null, PwaApplicationFeeType.DEFAULT, getRepeatedValue("desc"), 10000);

    when(pwaAppChargeRequestItemRepository.findAllByPwaAppChargeRequest(pwaAppChargeRequest))
        .thenReturn(List.of(chargeRequestItem));

    projectInfo.setProjectName(getRepeatedValue("test"));
    FieldUtils.writeField(organisationUnit, "name", getRepeatedValue("name"), true);
    FieldUtils.writeField(organisationUnit, "registeredNumber", getRepeatedValue("1234"), true);

    assertThat(projectInfo.getProjectName().length()).isEqualTo(104);

    var resultMap = applicationChargeRequestMetadataService.getMetadataMapForDetail(detail, pwaAppChargeRequest);

    var expectedMap = Map.of(
        "Applicant organisation", getTruncatedValue("name"),
        "Applicant org reg number", getTruncatedValue("1234"),
        "Project name", getTruncatedValue("test"),
        "Fee line 1 category", "DEFAULT/INITIAL",
        "Fee line 1 description", getTruncatedValue("desc"),
        "Fee line 1 amount pence", Integer.toString(10000)
    );

    assertThat(resultMap).isEqualTo(expectedMap);

    assertThat(resultMap).allSatisfy((key, value) ->
      assertThat(value.length()).isLessThanOrEqualTo(100));

  }
}