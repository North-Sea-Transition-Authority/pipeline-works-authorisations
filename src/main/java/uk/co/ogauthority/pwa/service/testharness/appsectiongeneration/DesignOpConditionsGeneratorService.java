package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.DesignOpConditionsForm;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinetechinfo.PadDesignOpConditionsService;
import uk.co.ogauthority.pwa.util.forminputs.minmax.MinMaxInput;

@Service
@Profile("development")
public class DesignOpConditionsGeneratorService {

  private final PadDesignOpConditionsService padDesignOpConditionsService;


  @Autowired
  public DesignOpConditionsGeneratorService(
      PadDesignOpConditionsService padDesignOpConditionsService) {
    this.padDesignOpConditionsService = padDesignOpConditionsService;
  }


  public void generateDesignOpConditions(PwaApplicationDetail pwaApplicationDetail) {

    var padDesignOpConditions = padDesignOpConditionsService.getDesignOpConditionsEntity(pwaApplicationDetail);
    var form = createDesignOpConditionsForm();
    padDesignOpConditionsService.saveEntityUsingForm(
        form, padDesignOpConditions);
  }


  private DesignOpConditionsForm createDesignOpConditionsForm() {

    var form = new DesignOpConditionsForm();
    form.setTemperatureOpMinMax(new MinMaxInput("-1", "2"));
    form.setTemperatureDesignMinMax(new MinMaxInput("-3", "4"));
    form.setPressureOpMinMax(new MinMaxInput("5", "6"));
    form.setPressureDesignMax("7");
    form.setFlowrateOpMinMax(new MinMaxInput("9.22", "9.33"));
    form.setFlowrateDesignMinMax(new MinMaxInput("14.33", "14.34"));
    form.setUvalueDesign("44");

    return form;
  }

}
