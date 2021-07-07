package uk.co.ogauthority.pwa.service.appprocessing.appprocessingwarning;

public class NonBlockingTasksWarning {

  private final boolean tasksHaveWarnings;
  private final String incompleteTasksWarningText;
  private final String returnUrl;

  public NonBlockingTasksWarning(boolean tasksHaveWarnings,
                                 String incompleteTasksWarningText,
                                 String returnUrl) {
    this.tasksHaveWarnings = tasksHaveWarnings;
    this.incompleteTasksWarningText = incompleteTasksWarningText;
    this.returnUrl = returnUrl;
  }


  public boolean getTasksHaveWarnings() {
    return tasksHaveWarnings;
  }

  public String getIncompleteTasksWarningText() {
    return incompleteTasksWarningText;
  }

  public String getReturnUrl() {
    return returnUrl;
  }


}
