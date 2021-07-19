package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration.pipelinegenerator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.RandomUtils;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.LatitudeCoordinate;
import uk.co.ogauthority.pwa.model.location.LongitudeCoordinate;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;

public class PipelineTestHarnessUtil {

  public static Random random = new Random();


  private static final List<String> PIPELINE_LOCATIONS = Arrays.asList(
      "Arkwright Subsea Facility", "Scott JD Platform", "Brigantine BG", "Telford SSIV Manifold", "Nevis South Well BP1",
      "Corvette Platform", "Nevis South Well BP2", "Beryl Alpha Riser Tower", "Pierce A11 Gas Distribution Manifold",
      "Galahad Platform", "Beryl Alpha Riser Tower", "Buzzard Platform P", "Nevis South Well", "Telford Central Manifold",
      "Gas Export SSIV", "Scott Water Inj Manifold", "Machar Gas Lift Manifold", "Gas Lift Manifold",
      "Gryphon Umb Jumper New Mnfold", "Framo Compact Manifold"
  );


  static PipelineType getRandomPipelineType() {
    var pipelineTypes = PipelineType.streamDisplayValues().collect(Collectors.toList());
    return pipelineTypes.get(random.nextInt(pipelineTypes.size()));
  }

  static String getRandomPipelineLocation() {
    return PIPELINE_LOCATIONS.get(random.nextInt(PIPELINE_LOCATIONS.size()));
  }

  static CoordinatePair getRandomCoordinates() {

    var latitudeDirections = Stream.of(LatitudeDirection.values()).collect(Collectors.toList());
    var latitudeCoords = new LatitudeCoordinate(
        RandomUtils.nextInt(45, 65),
        RandomUtils.nextInt(0, 60),
        BigDecimal.valueOf(RandomUtils.nextDouble(0, 60)).setScale(2, RoundingMode.HALF_EVEN),
        latitudeDirections.get(random.nextInt(latitudeDirections.size())));


    var longitudeDirections = Stream.of(LongitudeDirection.values()).collect(Collectors.toList());
    var longitudeCoords = new LongitudeCoordinate(
        RandomUtils.nextInt(0, 31),
        RandomUtils.nextInt(0, 60),
        BigDecimal.valueOf(RandomUtils.nextDouble(0, 60)).setScale(2, RoundingMode.HALF_EVEN),
        longitudeDirections.get(random.nextInt(longitudeDirections.size())));

    return new CoordinatePair(latitudeCoords, longitudeCoords);
  }


}
