package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationService;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.internal.PwaAppChargeRequest;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.internal.PwaAppChargeRequestItemRepository;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Service
public class ApplicationChargeRequestMetadataService {

  private static final int METADATA_MAX_VALUE_LENGTH = 100;

  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final PadProjectInformationService padProjectInformationService;
  private final PwaAppChargeRequestItemRepository pwaAppChargeRequestItemRepository;

  public ApplicationChargeRequestMetadataService(
      PortalOrganisationsAccessor portalOrganisationsAccessor,
      PadProjectInformationService padProjectInformationService,
      PwaAppChargeRequestItemRepository pwaAppChargeRequestItemRepository
  ) {
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.padProjectInformationService = padProjectInformationService;
    this.pwaAppChargeRequestItemRepository = pwaAppChargeRequestItemRepository;
  }

  public Map<String, String> getMetadataMapForDetail(
      PwaApplicationDetail detail,
      PwaAppChargeRequest pwaAppChargeRequest
  ) {
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

    var chargeRequestItems = pwaAppChargeRequestItemRepository.findAllByPwaAppChargeRequest(pwaAppChargeRequest);

    for (int i = 0; i < chargeRequestItems.size(); i++) {
      var lineNumber = i + 1;
      var item = chargeRequestItems.get(i);

      // Concat the fee type with the application type for the category, e.g. DEFAULT/INITIAL, DEFAULT/CAT_1_VARIATION, FAST_TRACK/INITIAL
      var category = "%s/%s".formatted(item.getPwaApplicationFeeType(), detail.getPwaApplication().getApplicationType());
      metadataMap.put("Fee line %d category".formatted(lineNumber), category);

      metadataMap.put("Fee line %d description".formatted(lineNumber), item.getDescription());
      metadataMap.put("Fee line %d amount pence".formatted(lineNumber), Integer.toString(item.getPennyAmount()));
    }

    metadataMap.forEach((key, value) -> {
      metadataMap.put(key, StringUtils.abbreviate(value, METADATA_MAX_VALUE_LENGTH));
    });

    return metadataMap;
  }
}
