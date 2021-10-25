package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.viewfactories;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PhysicalPipelineState;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.util.geojson.GeoJsonFeature;
import uk.co.ogauthority.pwa.util.geojson.GeoJsonFeatureCollection;
import uk.co.ogauthority.pwa.util.geojson.GeoJsonFeatureFactory;

@Service
public class ApplicationPipelineGeoJsonViewFactory {

  private final PadPipelineService pipelineService;
  private final GeoJsonFeatureFactory geoJsonFeatureFactory;

  @Autowired
  public ApplicationPipelineGeoJsonViewFactory(PadPipelineService pipelineService,
                                               GeoJsonFeatureFactory geoJsonFeatureFactory) {
    this.pipelineService = pipelineService;
    this.geoJsonFeatureFactory = geoJsonFeatureFactory;
  }


  public GeoJsonFeatureCollection createApplicationPipelinesAsLineFeatures(PwaApplicationDetail pwaApplicationDetail) {
    var applicationPipelineOverviews = pipelineService.getApplicationPipelineOverviews(pwaApplicationDetail);

    var pipelineGeoJsonFeatures = applicationPipelineOverviews.stream()
        .filter(overview -> overview.getPipelineStatus().hasPhysicalPipelineState(PhysicalPipelineState.ON_SEABED))
        .sorted(Comparator.comparing(PipelineOverview::getPipelineNumber))
        .map(this::pipelineOverviewToLineFeature)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());

    return geoJsonFeatureFactory.createFeatureCollection(pipelineGeoJsonFeatures);

  }


  private Optional<GeoJsonFeature> pipelineOverviewToLineFeature(PipelineOverview pipelineOverview) {

    if (!(pipelineOverview.getFromCoordinates().hasValue() && pipelineOverview.getToCoordinates().hasValue())) {
      return Optional.empty();
    }

    var feature = geoJsonFeatureFactory.createSimpleLineFeature(
        pipelineOverview.getFromCoordinates(), pipelineOverview.getToCoordinates()
    );
    feature.addProperty("Pipeline number", pipelineOverview.getPipelineNumber());
    if (pipelineOverview.getTemporaryPipelineNumber() != null) {
      feature.addProperty("Temporary pipeline number", pipelineOverview.getTemporaryPipelineNumber());
    }
    feature.addProperty("Status on application", pipelineOverview.getPipelineStatus().getDisplayText());
    feature.addProperty("From location", pipelineOverview.getFromLocation());
    feature.addProperty("From location (WGS84)", pipelineOverview.getFromCoordinates().getDisplayString());
    feature.addProperty("From location decimal degrees (WGS84)",
        String.format(
            "[%s , %s]",
            pipelineOverview.getFromCoordinates().getLatitudeDecimalDegrees(),
            pipelineOverview.getFromCoordinates().getLongitudeDecimalDegrees()
        )
    );
    feature.addProperty("To location", pipelineOverview.getToLocation());
    feature.addProperty("To location (WGS84)", pipelineOverview.getToCoordinates().getDisplayString());
    feature.addProperty("To location decimal degrees (WGS84)",
        // general display of coords is [lat, long]. Interestingly geoJson coordinates HAVE to be done in [long, lat] order.
        String.format(
            "[%s , %s]",
            pipelineOverview.getToCoordinates().getLatitudeDecimalDegrees(),
            pipelineOverview.getToCoordinates().getLongitudeDecimalDegrees()
        )
    );
    feature.addProperty("Length (m)", pipelineOverview.getLength().toPlainString());

    return Optional.of(feature);

  }

}
