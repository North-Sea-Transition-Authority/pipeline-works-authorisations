package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationService;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Service
public class ApplicationChargeRequestMetadataService {

  private static final int METADATA_MAX_VALUE_LENGTH = 100;

  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final PadProjectInformationService padProjectInformationService;

  public ApplicationChargeRequestMetadataService(PortalOrganisationsAccessor portalOrganisationsAccessor,
                                                 PadProjectInformationService padProjectInformationService) {
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.padProjectInformationService = padProjectInformationService;
  }

  public Map<String, String> getMetadataMapForDetail(PwaApplicationDetail detail) {
    var organisationUnit = portalOrganisationsAccessor
        .getOrganisationUnitById(detail.getPwaApplication().getApplicantOrganisationUnitId());

    var applicantOrgName = organisationUnit
        .map(PortalOrganisationUnit::getName)
        .orElse("");

    var applicantOrgRegNumber = organisationUnit
        .map(orgUnit -> orgUnit.getRegisteredNumber() != null
            ? orgUnit.getRegisteredNumber()
            : orgUnit.getForeignRegisteredNumber())
        .orElse("");

    var projectInfo = padProjectInformationService.getPadProjectInformationData(detail);

    var metadataMap = new HashMap<>(Map.of(
        "Applicant organisation", applicantOrgName,
        "Applicant org reg number", applicantOrgRegNumber,
        "Project name", projectInfo.getProjectName()
    ));

    metadataMap.forEach((key, value) -> {
      metadataMap.put(key, StringUtils.abbreviate(value, METADATA_MAX_VALUE_LENGTH));
    });

    return metadataMap;
  }
}
