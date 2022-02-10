package uk.co.ogauthority.pwa.controller.appsummary;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.ApplicationVersionAccessRequester;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.ApplicationVersionRequestType;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/pipeline-mapping-guidance")
@PwaAppProcessingPermissionCheck(permissions = PwaAppProcessingPermission.VIEW_APPLICATION_SUMMARY)
public class ApplicationPipelineDataMapGuidanceController {

  private final ApplicationVersionAccessRequester applicationVersionAccessRequester;
  private final String serviceName;
  private final String regulatorMapsAndToolsUrl;
  private final String regulatorMapsAndToolsLabel;
  private final String offshoreMapLabel;

  @Autowired
  public ApplicationPipelineDataMapGuidanceController(
      ApplicationVersionAccessRequester applicationVersionAccessRequester,
      @Value("${service.name}") String serviceName,
      @Value("${oga.interactivemapsandtools.link}") String regulatorMapsAndToolsUrl,
      @Value("${oga.interactivemapsandtools.label}") String regulatorMapsAndToolsLabel,
      @Value("${oga.interactivemapsandtools.offshoremap.label}") String offshoreMapLabel) {
    this.applicationVersionAccessRequester = applicationVersionAccessRequester;
    this.serviceName = serviceName;
    this.regulatorMapsAndToolsUrl = regulatorMapsAndToolsUrl;
    this.regulatorMapsAndToolsLabel = regulatorMapsAndToolsLabel;
    this.offshoreMapLabel = offshoreMapLabel;
  }

  @GetMapping
  public ModelAndView renderMappingGuidance(@PathVariable("applicationId") Integer applicationId,
                                            @PathVariable("applicationType")
                                    @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            PwaAppProcessingContext processingContext) {

    var availableAppVersions = applicationVersionAccessRequester.getAvailableAppVersionRequestTypesBy(
        processingContext);

    var pipelineDownloadOptions = createPipelineDataDownloadOptions(processingContext, availableAppVersions);

    return new ModelAndView("pwaApplication/appProcessing/appSummary/pipelineMappingGuidance")
        .addObject("caseSummaryView", processingContext.getCaseSummaryView())
        .addObject("pipelineDataDownloadOptionItems", pipelineDownloadOptions)
        .addObject("serviceName", serviceName)
        .addObject("regulatorMapsAndToolsUrl", regulatorMapsAndToolsUrl)
        .addObject("regulatorMapsAndToolsLabel", regulatorMapsAndToolsLabel)
        .addObject("offshoreMapLabel", offshoreMapLabel);

  }

  private List<PipelineDataDownloadOptionItem> createPipelineDataDownloadOptions(
      PwaAppProcessingContext pwaAppProcessingContext,
      Set<ApplicationVersionRequestType> applicationVersionRequestTypeSet) {
    return applicationVersionRequestTypeSet.stream()
        .map(applicationVersionRequestType -> new PipelineDataDownloadOptionItem(
            applicationVersionRequestType.getDisplayNum(),
            "Download " + applicationVersionRequestType.getDisplayString().toLowerCase(),
            ReverseRouter.route(on(ApplicationPipelineSpatialDataRestController.class).getLatestAvailableAppPipelinesForUserAsGeoJson(
                pwaAppProcessingContext.getMasterPwaApplicationId(),
                pwaAppProcessingContext.getApplicationType(),
                applicationVersionRequestType,
                null
            ))
        ))
        .sorted(Comparator.comparing(PipelineDataDownloadOptionItem::getDisplayOrder))
        .collect(Collectors.toList());

  }

}
