package uk.co.ogauthority.pwa.features.feemanagement.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.ogauthority.pwa.model.form.feeperiod.FeePeriodForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

public class FeeManagementUrlFactory {

  public FeeManagementUrlFactory() {
  }

  public String getFeePeriodChargesUrl(int id) {
    return ReverseRouter.route(on(FeeManagementController.class)
        .renderFeePeriodDetail(null, id));
  }

  public String getFeePeriodEditUrl(int id) {
    return ReverseRouter.route(on(FeeManagementController.class)
        .renderEditPeriodForm(null, id, new FeePeriodForm()));
  }
}
