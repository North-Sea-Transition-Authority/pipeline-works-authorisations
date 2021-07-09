package uk.co.ogauthority.pwa.service.appprocessing.appprocessingwarning;

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

  public static NonBlockingTasksWarning withWarning(String incompleteTasksWarningText,
                                                    NonBlockingWarningReturnMessage returnMessage) {
    return new NonBlockingTasksWarning(true, incompleteTasksWarningText, returnMessage);
  }

  public static NonBlockingTasksWarning withoutWarning() {
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
