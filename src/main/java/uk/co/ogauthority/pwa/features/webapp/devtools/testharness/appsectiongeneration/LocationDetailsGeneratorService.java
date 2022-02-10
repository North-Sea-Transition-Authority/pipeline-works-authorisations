package uk.co.ogauthority.pwa.features.webapp.devtools.testharness.appsectiongeneration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.HseSafetyZone;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.LocationDetailsForm;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.LocationDetailsQuestion;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.PadLocationDetailsService;
import uk.co.ogauthority.pwa.features.application.tasks.locationdetails.PsrNotification;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Service
@Profile("test-harness")
class LocationDetailsGeneratorService implements TestHarnessAppFormService {

  private final PadLocationDetailsService padLocationDetailsService;

  private static final ApplicationTask linkedAppFormTask = ApplicationTask.LOCATION_DETAILS;


  @Autowired
  public LocationDetailsGeneratorService(
      PadLocationDetailsService padLocationDetailsService) {
    this.padLocationDetailsService = padLocationDetailsService;
  }

  @Override
  public ApplicationTask getLinkedAppFormTask() {
    return linkedAppFormTask;
  }


  @Override
  public void generateAppFormData(TestHarnessAppFormServiceParams appFormServiceParams) {

    var form = createForm(appFormServiceParams.getApplicationDetail());
    var locationDetail = padLocationDetailsService.getLocationDetailsForDraft(appFormServiceParams.getApplicationDetail());
    padLocationDetailsService.saveEntityUsingForm(locationDetail, form);
  }


  private LocationDetailsForm createForm(PwaApplicationDetail pwaApplicationDetail) {

    var form = new LocationDetailsForm();

    var requiredQuestions = padLocationDetailsService.getRequiredQuestions(pwaApplicationDetail.getPwaApplicationType());

    if (requiredQuestions.contains(LocationDetailsQuestion.APPROXIMATE_PROJECT_LOCATION_FROM_SHORE)) {
      form.setApproximateProjectLocationFromShore("50m");
    }

    if (requiredQuestions.contains(LocationDetailsQuestion.WITHIN_SAFETY_ZONE)) {
      form.setWithinSafetyZone(HseSafetyZone.NO);
    }

    if (requiredQuestions.contains(LocationDetailsQuestion.PSR_NOTIFICATION)) {
      form.setPsrNotificationSubmittedOption(PsrNotification.NOT_REQUIRED);
      form.setPsrNotificationNotRequiredReason("My reason for why a PSR notification is not required");
    }


    if (requiredQuestions.contains(LocationDetailsQuestion.DIVERS_USED)) {
      form.setDiversUsed(false);
    }

    if (requiredQuestions.contains(LocationDetailsQuestion.TRANSPORTS_MATERIALS_TO_SHORE)) {
      form.setTransportsMaterialsToShore(false);
    }

    if (requiredQuestions.contains(LocationDetailsQuestion.FACILITIES_OFFSHORE)) {
      form.setFacilitiesOffshore(true);
    }

    if (requiredQuestions.contains(LocationDetailsQuestion.ROUTE_SURVEY_UNDERTAKEN)) {
      form.setRouteSurveyUndertaken(false);
      form.setRouteSurveyNotUndertakenReason("My reason for why a pipeline route survey has not been undertaken");
    }

    if (requiredQuestions.contains(LocationDetailsQuestion.WITHIN_LIMITS_OF_DEVIATION)) {
      form.setWithinLimitsOfDeviation(true);
    }

    return form;
  }




}
