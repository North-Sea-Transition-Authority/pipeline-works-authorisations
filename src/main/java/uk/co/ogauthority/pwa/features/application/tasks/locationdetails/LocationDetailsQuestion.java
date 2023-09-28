package uk.co.ogauthority.pwa.features.application.tasks.locationdetails;


import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

public enum LocationDetailsQuestion {

  APPROXIMATE_PROJECT_LOCATION_FROM_SHORE,
  WITHIN_SAFETY_ZONE,
  PSR_NOTIFICATION,
  DIVERS_USED,
  FACILITIES_OFFSHORE,
  TRANSPORTS_MATERIALS_TO_SHORE,
  TRANSPORTS_MATERIALS_FROM_SHORE,
  ROUTE_SURVEY_UNDERTAKEN,
  WITHIN_LIMITS_OF_DEVIATION,
  ROUTE_DOCUMENTS;


  public static Set<LocationDetailsQuestion> getAllExcluding(LocationDetailsQuestion... locationDetailsQuestions) {
    var excludedQuestions = EnumSet.noneOf(LocationDetailsQuestion.class);
    excludedQuestions.addAll(Arrays.asList(locationDetailsQuestions));
    return EnumSet.complementOf(excludedQuestions);
  }

}
