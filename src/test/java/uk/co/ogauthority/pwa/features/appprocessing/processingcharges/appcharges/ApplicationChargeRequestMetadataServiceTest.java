package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
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

  private ApplicationChargeRequestMetadataService applicationChargeRequestMetadataService;

  private final PwaApplicationDetail detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
  private final PortalOrganisationUnit organisationUnit = PortalOrganisationTestUtils.generateOrganisationUnit(1, "SHELL U.K. LIMITED");
  private final PadProjectInformation projectInfo = ProjectInformationTestUtils.buildEntity(LocalDate.now());

  @BeforeEach
  void setUp() throws Exception {

    applicationChargeRequestMetadataService = new ApplicationChargeRequestMetadataService(
        portalOrganisationsAccessor,
        padProjectInformationService);

    when(portalOrganisationsAccessor.getOrganisationUnitById(OrganisationUnitId.fromInt(organisationUnit.getOuId())))
        .thenReturn(Optional.of(organisationUnit));

    when(padProjectInformationService.getPadProjectInformationData(detail)).thenReturn(projectInfo);

    detail.getPwaApplication().setApplicantOrganisationUnitId(OrganisationUnitId.fromInt(organisationUnit.getOuId()));

  }

  @Test
  void getMetadataMapForDetail() {

    var resultMap = applicationChargeRequestMetadataService.getMetadataMapForDetail(detail);

    var expectedMap = Map.of(
        "Applicant organisation", organisationUnit.getName(),
        "Project name", projectInfo.getProjectName()
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

    projectInfo.setProjectName(getRepeatedValue("test"));
    FieldUtils.writeField(organisationUnit, "name", getRepeatedValue("name"), true);

    assertThat(projectInfo.getProjectName().length()).isEqualTo(104);

    var resultMap = applicationChargeRequestMetadataService.getMetadataMapForDetail(detail);

    var expectedMap = Map.of(
        "Applicant organisation", getTruncatedValue("name"),
        "Project name", getTruncatedValue("test")
    );

    assertThat(resultMap).isEqualTo(expectedMap);

    assertThat(resultMap).allSatisfy((key, value) ->
      assertThat(value.length()).isEqualTo(100));

  }

}