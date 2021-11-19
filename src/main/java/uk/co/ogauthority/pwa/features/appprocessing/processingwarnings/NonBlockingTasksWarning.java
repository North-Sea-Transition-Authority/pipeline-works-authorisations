package uk.co.ogauthority.pwa.features.appprocessing.processingwarnings;

public class NonBlockingTasksWarning {

  private final boolean tasksHaveWarnings;
  private final String incompleteTasksWarningText;
  private final NonBlockingWarningReturnMessage returnMessage;

  public NonBlockingTasksWarning(boolean tasksHaveWarnings,
                                 String incompleteTasksWarningText,
                                 NonBlockingWarningReturnMessage returnMessage) {
    this.tasksHaveWarnings = tasksHaveWarnings;
    this.incompleteTasksWarningText = incompleteTasksWarningText;
    this.returnMessage = returnMessage;
  }

  static NonBlockingTasksWarning withWarning(String incompleteTasksWarningText,
                                             NonBlockingWarningReturnMessage returnMessage) {
    return new NonBlockingTasksWarning(true, incompleteTasksWarningText, returnMessage);
  }

  static NonBlockingTasksWarning withoutWarning() {
    return new NonBlockingTasksWarning(false, null, null);
  }


  public boolean getTasksHaveWarnings() {
    return tasksHaveWarnings;
  }

  public String getIncompleteTasksWarningText() {
    return incompleteTasksWarningText;
  }

  public NonBlockingWarningReturnMessage getReturnMessage() {
    return returnMessage;
  }

}
