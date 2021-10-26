package uk.co.ogauthority.pwa.features.application.tasks.huoo;

import java.util.List;
import java.util.Set;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;

public class HuooSummaryValidationResultTestUtil {
  private HuooSummaryValidationResultTestUtil(){
    throw new UnsupportedOperationException("no util for you!");
  }


  public static HuooSummaryValidationResult validResult(){
    return new HuooSummaryValidationResult(Set.of(), List.of(), Set.of());
  }

  public static HuooSummaryValidationResult invalidResult(){
    return new HuooSummaryValidationResult(Set.of(HuooRole.HOLDER), List.of(), Set.of());
  }

}