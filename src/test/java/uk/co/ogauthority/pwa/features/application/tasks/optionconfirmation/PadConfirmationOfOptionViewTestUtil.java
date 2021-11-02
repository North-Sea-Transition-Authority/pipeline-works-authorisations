package uk.co.ogauthority.pwa.features.application.tasks.optionconfirmation;


public class PadConfirmationOfOptionViewTestUtil {

  private PadConfirmationOfOptionViewTestUtil() {
    //no instantiation
  }

  public static PadConfirmationOfOptionView createFrom(String workType, String workDesc){
    return new PadConfirmationOfOptionView(workType, workDesc);
  }
}