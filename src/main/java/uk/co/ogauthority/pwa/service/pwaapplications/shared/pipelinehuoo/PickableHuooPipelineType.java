package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import uk.co.ogauthority.pwa.model.dto.pipelines.IdentLocationInclusionMode;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentPoint;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentifier;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineSegment;

/**
 * <p>Represents the types of pipeline that can be picked when assigning HUOO roles.</p>
 *
 * <p>Would probably be better to have the enum only contain the values, and then a separate static PickablePipelineIdFactory class
 * to contains all the String construction, object wrapping, and String decoding.</p>
 *
 * <p>A separate static factory class would probably allow us to hide the PipelineIdentifier implementation class APIs
 * that are currently exposed.</p>
 */
public enum PickableHuooPipelineType {

  FULL("FULL##ID:[0-9]{1,16}"),
  SPLIT("SPLIT##ID:[0-9]{1,16}##(FROM_INC|FROM_EXC):.{1,200}##(TO_INC|TO_EXC):.{1,200}"),
  // keep unknown as makes processing decisions easy
  UNKNOWN(".*");

  private static final String PROPERTY_DELIMITER = "##";
  private static final String FROM_INC = "FROM_INC";
  private static final String FROM_EXC = "FROM_EXC";
  private static final String TO_INC = "TO_INC";
  private static final String TO_EXC = "TO_EXC";
  private static final String ID = "ID";
  private static final String PROPERTY_VALUE_DELIMITER = ":";

  private final String regexMatcher;

  PickableHuooPipelineType(String regexMatcher) {
    this.regexMatcher = regexMatcher;
  }

  private String getRegexMatcher() {
    return regexMatcher;
  }


  public static String createPickableString(PipelineIdentifier pipelineIdentifier) {
    var pickableStringVisitor = new PipelineIdentifierPickableStringVisitor();
    pipelineIdentifier.accept(pickableStringVisitor);
    return pickableStringVisitor.getPickableString();
  }

  /**
   * Intentionally package private so {@link PickableHuooPipelineType#createPickableString} is used instead.
   */
  static String createPickableStringFrom(PipelineId pipelineId) {
    return FULL + createPipelineIdProperty(pipelineId);
  }


  /**
   * Intentionally package private so {@link PickableHuooPipelineType#createPickableString} is used instead.
   */
  static String createPickableStringFrom(PipelineSegment pipelineSegment) {
    var fromProperty = pipelineSegment.getFromPointMode().equals(IdentLocationInclusionMode.INCLUSIVE)
        ? FROM_INC : FROM_EXC;

    var toProperty = pipelineSegment.getToPointMode().equals(IdentLocationInclusionMode.INCLUSIVE)
        ? TO_INC : TO_EXC;

    return SPLIT + createPipelineIdProperty(pipelineSegment) +
        createStringProperty(fromProperty, pipelineSegment.getFromPoint().getLocationName()) +
        createStringProperty(toProperty, pipelineSegment.getToPoint().getLocationName());
  }


  public static PickableHuooPipelineType getTypeIdFromString(String idString) {
    if (idString.matches(FULL.getRegexMatcher())) {
      return FULL;
    } else if (idString.matches(SPLIT.getRegexMatcher())) {
      return SPLIT;
    }
    return UNKNOWN;
  }

  private static String createPipelineIdProperty(PipelineIdentifier pipelineIdentifier) {
    return createStringProperty(ID, String.valueOf(pipelineIdentifier.getPipelineIdAsInt()));
  }

  private static String createStringProperty(String property, String value) {
    return PROPERTY_DELIMITER + property + PROPERTY_VALUE_DELIMITER + value;
  }

  public static Optional<PipelineIdentifier> decodeString(String idString) {

    var type = PickableHuooPipelineType.getTypeIdFromString(idString);
    if (UNKNOWN.equals(type)) {
      return Optional.empty();
    }

    List<String> tokens = Arrays.asList(idString.split(PROPERTY_DELIMITER));

    var pipelineId = extractPipelineId(tokens);
    if (FULL.equals(type)) {
      return pipelineId;
    }

    // can only be SPLIT pipeline now so can do outside of if-else
    var fromPoint = extractPipelineIdentPoint(tokens, FROM_INC, FROM_EXC);
    var toPoint = extractPipelineIdentPoint(tokens, TO_INC, TO_EXC);

    // not sure this is very good tbh. at least nastiness is contained here, and not exported to calling code?
    return !(pipelineId.isEmpty() || fromPoint.isEmpty() || toPoint.isEmpty())
        ? Optional.of(PipelineSegment.from(pipelineId.get().getPipelineId(), fromPoint.get(), toPoint.get()))
        : Optional.empty();
  }

  private static Optional<PipelineIdentifier> extractPipelineId(List<String> tokens) {
    var idProperty = ID + PROPERTY_VALUE_DELIMITER;
    return tokens.stream()
        .filter(s -> s.startsWith(idProperty))
        .map(s -> s.substring(idProperty.length()))
        .map(Integer::valueOf)
        // have to make sure the map uses the interface not the implementation class so method signature is a match
        .<PipelineIdentifier>map(PipelineId::new)
        .findFirst();
  }


  private static Optional<PipelineIdentPoint> extractPipelineIdentPoint(List<String> tokens, String inclusiveMarker,
                                                                        String exclusiveMarker) {
    // potentially perf implications here if executed many times as lots of strings added together.
    var inclusiveMarkerProperty = inclusiveMarker + PROPERTY_VALUE_DELIMITER;
    var exclusiveMarkerProperty = exclusiveMarker + PROPERTY_VALUE_DELIMITER;

    return tokens.stream()
        // cannot have static property variables on enum that are referenced in enum constructor,
        // so some duplication unavoidable with type regex matcher
        .filter(s -> s.matches("(" + inclusiveMarker + "|" + exclusiveMarker + "):.{1,200}"))
        .map(s -> {
          if (s.startsWith(inclusiveMarkerProperty)) {
            return PipelineIdentPoint.inclusivePoint(s.substring(inclusiveMarkerProperty.length()));
          } else {
            return PipelineIdentPoint.exclusivePoint(s.substring(exclusiveMarkerProperty.length()));
          }

        })
        .findFirst();
  }

  public Stream<PickableHuooPipelineType> stream() {
    return Arrays.stream(PickableHuooPipelineType.values());
  }

}