package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.enums.LocationDetailsQuestion;
import uk.co.ogauthority.pwa.model.entity.enums.locationdetails.HseSafetyZone;
import uk.co.ogauthority.pwa.model.entity.enums.locationdetails.PsrNotification;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadLocationDetails;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.location.PadLocationDetailsService;

@Service
@Profile("development")
public class LocationDetailsGeneratorService {

  private final PadLocationDetailsService padLocationDetailsService;


  @Autowired
  public LocationDetailsGeneratorService(
      PadLocationDetailsService padLocationDetailsService) {
    this.padLocationDetailsService = padLocationDetailsService;
  }



  public void generateLocationDetails(PwaApplicationDetail pwaApplicationDetail) {

    var padLocationDetails = new PadLocationDetails();
    setLocationDetailsData(pwaApplicationDetail, padLocationDetails);
    padLocationDetailsService.save(padLocationDetails);
  }


  private void setLocationDetailsData(PwaApplicationDetail pwaApplicationDetail,
                                      PadLocationDetails padLocationDetails) {

    var requiredQuestions = padLocationDetailsService.getRequiredQuestions(pwaApplicationDetail.getPwaApplicationType());

    padLocationDetails.setPwaApplicationDetail(pwaApplicationDetail);

    if (requiredQuestions.contains(LocationDetailsQuestion.APPROXIMATE_PROJECT_LOCATION_FROM_SHORE)) {
      padLocationDetails.setApproximateProjectLocationFromShore("50m");
    }

    if (requiredQuestions.contains(LocationDetailsQuestion.WITHIN_SAFETY_ZONE)) {
      padLocationDetails.setWithinSafetyZone(HseSafetyZone.NO);
    }

    if (requiredQuestions.contains(LocationDetailsQuestion.PSR_NOTIFICATION)) {
      padLocationDetails.setPsrNotificationSubmittedOption(PsrNotification.NOT_REQUIRED);
      padLocationDetails.setPsrNotificationNotRequiredReason("My reason for why a PSR notification is not required");
    }


    if (requiredQuestions.contains(LocationDetailsQuestion.DIVERS_USED)) {
      padLocationDetails.setDiversUsed(false);
    }

    if (requiredQuestions.contains(LocationDetailsQuestion.TRANSPORTS_MATERIALS_TO_SHORE)) {
      padLocationDetails.setTransportsMaterialsToShore(false);
    }

    if (requiredQuestions.contains(LocationDetailsQuestion.FACILITIES_OFFSHORE)) {
      padLocationDetails.setFacilitiesOffshore(true);
    }

    if (requiredQuestions.contains(LocationDetailsQuestion.ROUTE_SURVEY_UNDERTAKEN)) {
      padLocationDetails.setRouteSurveyUndertaken(false);
      padLocationDetails.setRouteSurveyNotUndertakenReason("My reason for why a pipeline route survey has not been undertaken");
    }

    if (requiredQuestions.contains(LocationDetailsQuestion.WITHIN_LIMITS_OF_DEVIATION)) {
      padLocationDetails.setWithinLimitsOfDeviation(true);
    }


  }




}
