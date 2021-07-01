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
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.viewfactories.ApplicationPipelineGeoJsonViewFactory;
import uk.co.ogauthority.pwa.util.FileDownloadUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;
import uk.co.ogauthority.pwa.util.geojson.GeoJsonFeatureCollection;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/pipelines-geojson")
public class ApplicationPipelineSpatialDataRestController {


  private final ApplicationPipelineGeoJsonViewFactory applicationPipelineGeoJsonViewFactory;

  @Autowired
  public ApplicationPipelineSpatialDataRestController(ApplicationPipelineGeoJsonViewFactory applicationPipelineGeoJsonViewFactory) {

    this.applicationPipelineGeoJsonViewFactory = applicationPipelineGeoJsonViewFactory;
  }

  @GetMapping("/latest")
  @PwaAppProcessingPermissionCheck(permissions = PwaAppProcessingPermission.VIEW_APPLICATION_SUMMARY)
  public ResponseEntity<GeoJsonFeatureCollection> getLatestAvailableAppPipelinesForUserAsGeoJson(
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("applicationType")
      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
      PwaAppProcessingContext processingContext
  ) {

    var pipelineGeoJsonFeatures = applicationPipelineGeoJsonViewFactory
        .createApplicationPipelinesAsLineFeatures(processingContext.getApplicationDetail());

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
            processingContext.getApplicationDetail().getVersionNo(),
            formattedInstant
        )
    );
  }

}
