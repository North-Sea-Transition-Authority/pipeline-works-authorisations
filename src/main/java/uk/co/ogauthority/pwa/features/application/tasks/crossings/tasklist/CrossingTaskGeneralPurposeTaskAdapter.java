package uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist;

import org.apache.commons.lang3.NotImplementedException;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.features.application.tasklist.api.GeneralPurposeApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.types.controller.CrossingTypesController;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings.CrossingAgreementTask;

/**
 * Crossing Agreement tasks do not yet fit directly into the ApplicationTaskFramework but are essentially the same.
 * This class allows a use of the ApplicationTaskService for the purposes of using the ApplicationContext annotations
 * for task access control;
 */
public final class CrossingTaskGeneralPurposeTaskAdapter implements GeneralPurposeApplicationTask {


  private final CrossingAgreementTask crossingAgreementTask;

  public CrossingTaskGeneralPurposeTaskAdapter(CrossingAgreementTask crossingAgreementTask) {
    this.crossingAgreementTask = crossingAgreementTask;
  }

  public CrossingAgreementTask getCrossingAgreementTask() {
    return crossingAgreementTask;
  }

  @Override
  public Class<? extends ApplicationFormSectionService> getServiceClass() {
    return crossingAgreementTask.getSectionClass();
  }

  @Override
  public Class getControllerClass() {
    return CrossingTypesController.class;
  }

  @Override
  public int getDisplayOrder() {
    throw new NotImplementedException("General Purpose Application Task method not implemented");
  }

  @Override
  public String getDisplayName() {
    throw new NotImplementedException("General Purpose Application Task method not implemented");
  }

  @Override
  public String getShortenedDisplayName() {
    throw new NotImplementedException("General Purpose Application Task method not implemented");
  }

  @Override
  public String getTaskLandingPageRoute(PwaApplication pwaApplication) {
    throw new NotImplementedException("General Purpose Application Task method not implemented");
  }
}
