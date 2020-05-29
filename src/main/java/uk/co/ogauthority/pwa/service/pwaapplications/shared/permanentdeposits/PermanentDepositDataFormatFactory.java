package uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits;

import java.util.List;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PermanentDepositsOverview;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.util.CoordinateUtils;


public class PermanentDepositDataFormatFactory {

  List<PermanentDepositsOverview> views;

  public PermanentDepositDataFormatFactory(List<PermanentDepositsOverview> forms) {
    this.views = forms;
  }

  public CoordinatePair getFromCoordinatesPairFromForm(int viewIndex) {
    return CoordinateUtils.coordinatePairFromForm(views.get(viewIndex).getFromCoordinateForm());
  }

  public CoordinatePair getToCoordinatesPairFromForm(int viewIndex) {
    return CoordinateUtils.coordinatePairFromForm(views.get(viewIndex).getToCoordinateForm());
  }

}
