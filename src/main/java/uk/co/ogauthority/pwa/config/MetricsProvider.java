package uk.co.ogauthority.pwa.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

public class MetricsProvider {

  private final Timer taskListTimer;
  private final Timer appSubmissionTimer;
  private final Timer documentGenerationTimer;
  private final Timer startAppTimer;
  private final Timer workAreaTabTimer;
  private final Timer appContextTimer;

  public MetricsProvider(MeterRegistry registry) {
    this.taskListTimer = registry.timer("pwa.taskListTimer");
    this.appSubmissionTimer = registry.timer("pwa.appSubmissionTimer");
    this.documentGenerationTimer = registry.timer("pwa.documentGenerationTimer");
    this.startAppTimer = registry.timer("pwa.startAppTimer");
    this.workAreaTabTimer = registry.timer("pwa.workAreaTabTimer");
    this.appContextTimer = registry.timer("pwa.appContextTimer");
  }

  public Timer getTaskListTimer() {
    return taskListTimer;
  }

  public Timer getAppSubmissionTimer() {
    return appSubmissionTimer;
  }

  public Timer getDocumentGenerationTimer() {
    return documentGenerationTimer;
  }

  public Timer getStartAppTimer() {
    return startAppTimer;
  }

  public Timer getWorkAreaTabTimer() {
    return workAreaTabTimer;
  }

  public Timer getAppContextTimer() {
    return appContextTimer;
  }
}
