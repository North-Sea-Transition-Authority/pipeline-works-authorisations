package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;

import java.util.EnumSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom.DecommissioningCondition;
import uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom.EnvDecomQuestion;
import uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom.EnvironmentalCondition;
import uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom.EnvironmentalDecommissioningForm;
import uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom.PadEnvironmentalDecommissioningService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Service
@Profile("test-harness")
class EnvAndDecomGeneratorService implements TestHarnessAppFormService {

  private final PadEnvironmentalDecommissioningService padEnvironmentalDecommissioningService;

  private static final ApplicationTask linkedAppFormTask = ApplicationTask.ENVIRONMENTAL_DECOMMISSIONING;


  @Autowired
  public EnvAndDecomGeneratorService(
      PadEnvironmentalDecommissioningService padEnvironmentalDecommissioningService) {
    this.padEnvironmentalDecommissioningService = padEnvironmentalDecommissioningService;
  }

  @Override
  public ApplicationTask getLinkedAppFormTask() {
    return linkedAppFormTask;
  }


  @Override
  public void generateAppFormData(TestHarnessAppFormServiceParams appFormServiceParams) {

    var form = createForm(appFormServiceParams.getApplicationDetail());
    var padEnvDecom = padEnvironmentalDecommissioningService.getEnvDecomData(appFormServiceParams.getApplicationDetail());
    padEnvironmentalDecommissioningService.saveEntityUsingForm(padEnvDecom, form);
  }

  private EnvironmentalDecommissioningForm createForm(PwaApplicationDetail pwaApplicationDetail) {

    var form = new EnvironmentalDecommissioningForm();

    var requiredQuestions = padEnvironmentalDecommissioningService.getAvailableQuestions(pwaApplicationDetail);

    if (requiredQuestions.contains(EnvDecomQuestion.TRANS_BOUNDARY)) {
      form.setTransboundaryEffect(true);
    }

    if (requiredQuestions.contains(EnvDecomQuestion.BEIS_EMT_PERMITS)) {
      form.setEmtHasSubmittedPermits(true);
      form.setEmtHasOutstandingPermits(false);
      form.setPermitsSubmitted("My submitted permits");
    }

    if (requiredQuestions.contains(EnvDecomQuestion.ACKNOWLEDGEMENTS)) {
      form.setEnvironmentalConditions(EnumSet.allOf(EnvironmentalCondition.class));
    }

    if (requiredQuestions.contains(EnvDecomQuestion.DECOMMISSIONING)) {
      form.setDecommissioningConditions(EnumSet.allOf(DecommissioningCondition.class));
    }

    return form;
  }




}
