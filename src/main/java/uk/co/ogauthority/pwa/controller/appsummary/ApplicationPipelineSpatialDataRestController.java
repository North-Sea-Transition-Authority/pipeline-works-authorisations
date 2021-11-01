package uk.co.ogauthority.pwa.controller.appsummary;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.geojsonview.ApplicationPipelineGeoJsonViewFactory;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.ApplicationVersionAccessRequester;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.ApplicationVersionRequestType;
import uk.co.ogauthority.pwa.util.FileDownloadUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;
import uk.co.ogauthority.pwa.util.geojson.GeoJsonFeatureCollection;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/pipelines-geojson")
public class ApplicationPipelineSpatialDataRestController {

  private static final String DEFAULT_APP_VER_REQUEST_TYPE = ApplicationVersionRequestType.LAST_SUBMITTED.name();

  private final ApplicationPipelineGeoJsonViewFactory applicationPipelineGeoJsonViewFactory;
  private final ApplicationVersionAccessRequester applicationVersionAccessRequester;

  @Autowired
  public ApplicationPipelineSpatialDataRestController(
      ApplicationPipelineGeoJsonViewFactory applicationPipelineGeoJsonViewFactory,
      ApplicationVersionAccessRequester applicationVersionAccessRequester) {

    this.applicationPipelineGeoJsonViewFactory = applicationPipelineGeoJsonViewFactory;
    this.applicationVersionAccessRequester = applicationVersionAccessRequester;
  }

  @GetMapping
  @PwaAppProcessingPermissionCheck(permissions = PwaAppProcessingPermission.VIEW_APPLICATION_SUMMARY)
  public ResponseEntity<GeoJsonFeatureCollection> getLatestAvailableAppPipelinesForUserAsGeoJson(
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("applicationType")
      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
      // forced to use string value instead of enum value due to compile time requirement of annotation
      @RequestParam(name = "appVersionRequestType", defaultValue = "LAST_SUBMITTED") ApplicationVersionRequestType appVersionRequestType,
      PwaAppProcessingContext processingContext
  ) {

    var requestedAppDetail = applicationVersionAccessRequester
        .getPwaApplicationDetailWhenAvailable(processingContext, appVersionRequestType)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            String.format("Could not find appId:%s detail using versionRequestType %s", applicationId, appVersionRequestType))
        );

    var pipelineGeoJsonFeatures = applicationPipelineGeoJsonViewFactory
        .createApplicationPipelinesAsLineFeatures(requestedAppDetail);

    var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH.mm.ss")
        .withLocale(Locale.UK)
        .withZone(ZoneId.systemDefault());

    var instant = Instant.now();
    var formattedInstant = formatter.format(instant);

    return FileDownloadUtils.getCustomMediaTypeObjectAsResponse(
        pipelineGeoJsonFeatures,
        "application/geo+json",
        String.format(
            "%s_v%s_%s.geojson",
            processingContext.getPwaApplication().getAppReference().replace('/', '-'),
            requestedAppDetail.getVersionNo(),
            formattedInstant
        )
    );
  }

}
