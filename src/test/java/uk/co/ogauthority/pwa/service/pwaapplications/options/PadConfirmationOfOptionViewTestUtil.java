package uk.co.ogauthority.pwa.service.pwaapplications.options;



public class PadConfirmationOfOptionViewTestUtil {

  private PadConfirmationOfOptionViewTestUtil() {
    //no instantiation
  }

  public static PadConfirmationOfOptionView createFrom(String workType, String workDesc){
    return new PadConfirmationOfOptionView(workType, workDesc);
  }
}