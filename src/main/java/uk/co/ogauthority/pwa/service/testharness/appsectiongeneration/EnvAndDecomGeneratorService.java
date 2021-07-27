package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;

import java.util.EnumSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.enums.DecommissioningCondition;
import uk.co.ogauthority.pwa.model.entity.enums.EnvironmentalCondition;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.pwaapplications.shared.EnvDecomQuestion;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.EnvironmentalDecommissioningForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadEnvironmentalDecommissioningService;
import uk.co.ogauthority.pwa.service.testharness.TestHarnessAppFormService;
import uk.co.ogauthority.pwa.service.testharness.TestHarnessAppFormServiceParams;

@Service
@Profile("development")
class EnvAndDecomGeneratorService implements TestHarnessAppFormService {

  private final PadEnvironmentalDecommissioningService padEnvironmentalDecommissioningService;

  private final ApplicationTask linkedAppFormTask = ApplicationTask.ENVIRONMENTAL_DECOMMISSIONING;


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
