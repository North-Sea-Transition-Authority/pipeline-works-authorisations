package uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation.PadProjectInformationService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation.ProjectInformationTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationChargeRequestMetadataServiceTest {

  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Mock
  private PadProjectInformationService padProjectInformationService;

  private ApplicationChargeRequestMetadataService applicationChargeRequestMetadataService;

  private final PwaApplicationDetail detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
  private final PortalOrganisationUnit organisationUnit = PortalOrganisationTestUtils.generateOrganisationUnit(1, "SHELL U.K. LIMITED");
  private final PadProjectInformation projectInfo = ProjectInformationTestUtils.buildEntity(LocalDate.now());

  @Before
  public void setUp() throws Exception {

    applicationChargeRequestMetadataService = new ApplicationChargeRequestMetadataService(
        portalOrganisationsAccessor,
        padProjectInformationService);

    when(portalOrganisationsAccessor.getOrganisationUnitById(OrganisationUnitId.fromInt(organisationUnit.getOuId())))
        .thenReturn(Optional.of(organisationUnit));

    when(padProjectInformationService.getPadProjectInformationData(detail)).thenReturn(projectInfo);

    detail.getPwaApplication().setApplicantOrganisationUnitId(OrganisationUnitId.fromInt(organisationUnit.getOuId()));

  }

  @Test
  public void getMetadataMapForDetail() {

    var resultMap = applicationChargeRequestMetadataService.getMetadataMapForDetail(detail);

    var expectedMap = Map.of(
        "APPLICANT_ORGANISATION", organisationUnit.getName(),
        "PROJECT_NAME", projectInfo.getProjectName()
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
  public void getMetadataMapForDetail_longValuesTruncated() throws IllegalAccessException {

    projectInfo.setProjectName(getRepeatedValue("test"));
    FieldUtils.writeField(organisationUnit, "name", getRepeatedValue("name"), true);

    assertThat(projectInfo.getProjectName().length()).isEqualTo(104);

    var resultMap = applicationChargeRequestMetadataService.getMetadataMapForDetail(detail);

    var expectedMap = Map.of(
        "APPLICANT_ORGANISATION", getTruncatedValue("name"),
        "PROJECT_NAME", getTruncatedValue("test")
    );

    assertThat(resultMap).isEqualTo(expectedMap);

    assertThat(resultMap).allSatisfy((key, value) -> {
      assertThat(value.length()).isEqualTo(100);
    });

  }

}