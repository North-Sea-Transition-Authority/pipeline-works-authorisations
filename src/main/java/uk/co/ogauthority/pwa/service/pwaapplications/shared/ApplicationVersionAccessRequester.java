package uk.co.ogauthority.pwa.service.pwaapplications.shared;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.exception.ViewNotFoundException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.PadVersionLookup;
import uk.co.ogauthority.pwa.repository.pwaapplications.search.PadVersionLookupRepository;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;

@Service
public class ApplicationVersionAccessRequester {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationVersionAccessRequester.class);

  private final PadVersionLookupRepository padVersionLookupRepository;
  private final PwaApplicationDetailService pwaApplicationDetailService;

  @Autowired
  public ApplicationVersionAccessRequester(PadVersionLookupRepository padVersionLookupRepository,
                                           PwaApplicationDetailService pwaApplicationDetailService) {
    this.padVersionLookupRepository = padVersionLookupRepository;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
  }

  public Set<ApplicationVersionRequestType> getAvailableAppVersionRequestTypesBy(PwaAppProcessingContext pwaAppProcessingContext) {

    var padVersionLookup = getPadVersionLookupOrError(pwaAppProcessingContext.getPwaApplication());

    return getRequestTypesUsingContextAndLookup(pwaAppProcessingContext, padVersionLookup);
  }

  private Set<ApplicationVersionRequestType> getAvailableVersionRequestTypesForContext(PwaAppProcessingContext pwaAppProcessingContext) {
    return Arrays.stream(ApplicationVersionRequestType.values())
        .filter(applicationVersionRequestType -> pwaAppProcessingContext.hasAnyProcessingPermission(
            applicationVersionRequestType.getAccessibleByPermissions().toArray(PwaAppProcessingPermission[]::new))
        )
        .collect(Collectors.toCollection(() -> EnumSet.noneOf(ApplicationVersionRequestType.class)));
  }

  private void removeInvalidVersionRequestsUsingVersionLookup(PadVersionLookup padVersionLookup,
                                                              Set<ApplicationVersionRequestType> applicationVersionRequestTypes) {
    if (applicationVersionRequestTypes.contains(ApplicationVersionRequestType.CURRENT_DRAFT)
        && padVersionLookup.getMaxDraftVersionNo() == null) {
      applicationVersionRequestTypes.remove(ApplicationVersionRequestType.CURRENT_DRAFT);
    }

    if (applicationVersionRequestTypes.contains(ApplicationVersionRequestType.LAST_SUBMITTED)
        && padVersionLookup.getLatestSubmittedVersionNo() == null) {
      applicationVersionRequestTypes.remove(ApplicationVersionRequestType.LAST_SUBMITTED);
    }

    if (applicationVersionRequestTypes.contains(ApplicationVersionRequestType.LAST_SATISFACTORY)
        && padVersionLookup.getLatestConfirmedSatisfactoryVersionNo() == null) {
      applicationVersionRequestTypes.remove(ApplicationVersionRequestType.LAST_SATISFACTORY);
    }
  }

  private PadVersionLookup getPadVersionLookupOrError(PwaApplication pwaApplication) {
    return padVersionLookupRepository
        .findByPwaApplicationId(pwaApplication.getId())
        .orElseThrow(() ->
            new ViewNotFoundException("Expected to find PadVersionLookup for appId:" + pwaApplication.getId()));
  }

  private Set<ApplicationVersionRequestType> getRequestTypesUsingContextAndLookup(PwaAppProcessingContext pwaAppProcessingContext,
                                                                                  PadVersionLookup padVersionLookup) {
    var versionRequestTypes = getAvailableVersionRequestTypesForContext(pwaAppProcessingContext);

    removeInvalidVersionRequestsUsingVersionLookup(padVersionLookup, versionRequestTypes);

    return Collections.unmodifiableSet(versionRequestTypes);
  }

  public Optional<PwaApplicationDetail> getPwaApplicationDetailWhenAvailable(PwaAppProcessingContext pwaAppProcessingContext,
                                                                             ApplicationVersionRequestType applicationVersionRequestType) {

    var application = pwaAppProcessingContext.getPwaApplication();
    var padVersionLookup = getPadVersionLookupOrError(application);

    var availableVersionRequestTypes = getRequestTypesUsingContextAndLookup(pwaAppProcessingContext, padVersionLookup);

    if (!availableVersionRequestTypes.contains(applicationVersionRequestType)) {
      LOGGER.debug("AppVersionRequestType {} not found in {}", applicationVersionRequestType, availableVersionRequestTypes);
      return Optional.empty();
    }

    switch (applicationVersionRequestType) {
      case CURRENT_DRAFT:
        return Optional.of(
            pwaApplicationDetailService.getDetailByVersionNo(application, padVersionLookup.getMaxDraftVersionNo()));
      case LAST_SUBMITTED:
        return Optional.of(
            pwaApplicationDetailService.getDetailByVersionNo(application, padVersionLookup.getLatestSubmittedVersionNo()));
      case LAST_SATISFACTORY:
        return Optional.of(
            pwaApplicationDetailService.getDetailByVersionNo(application, padVersionLookup.getLatestConfirmedSatisfactoryVersionNo()));
      default:
        throw new UnsupportedOperationException("Unhandled applicationVersionRequestType: " + applicationVersionRequestType);
    }

  }

}
