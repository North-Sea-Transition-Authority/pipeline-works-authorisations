package uk.co.ogauthority.pwa.features.application.tasks.huoo;

import java.util.List;

public class PadHuooSummaryViewTestUtil {

  private PadHuooSummaryViewTestUtil() {
    throw new UnsupportedOperationException("no util for you!");
  }

  public static PadHuooSummaryView getEmptySummaryView(){
    return new PadHuooSummaryView(List.of(), List.of(), true);

  }
}