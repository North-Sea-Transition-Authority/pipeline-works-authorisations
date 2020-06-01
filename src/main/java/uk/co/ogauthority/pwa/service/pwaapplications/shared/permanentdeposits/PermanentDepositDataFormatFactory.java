package uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits;

import java.util.List;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.util.CoordinateUtils;


public class PermanentDepositDataFormatFactory {

  List<PermanentDepositsForm> forms;

  public PermanentDepositDataFormatFactory(List<PermanentDepositsForm> forms) {
    this.forms = forms;
  }

  public CoordinatePair getFromCoordinatesPairFromForm(int formIndex) {
    return CoordinateUtils.coordinatePairFromForm(forms.get(formIndex).getFromCoordinateForm());
  }

  public CoordinatePair getToCoordinatesPairFromForm(int formIndex) {
    return CoordinateUtils.coordinatePairFromForm(forms.get(formIndex).getToCoordinateForm());
  }

}
