package uk.co.ogauthority.pwa.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

public class MetricsProvider {

  private final Timer taskListTimer;
  private final Timer appValidationTimer;
  private final Timer documentGenerationTimer;
  private final Timer documentPreviewTimer;
  private final Timer startAppTimer;
  private final Timer workAreaTabTimer;

  public MetricsProvider(MeterRegistry registry) {
    this.taskListTimer = registry.timer("pwa.taskListTimer");
    this.appValidationTimer = registry.timer("pwa.appValidationTimer");
    this.documentGenerationTimer = registry.timer("pwa.documentGenerationTimer");
    this.documentPreviewTimer = registry.timer("pwa.documentPreviewTimer");
    this.startAppTimer = registry.timer("pwa.startAppTimer");
    this.workAreaTabTimer = registry.timer("pwa.workAreaTabTimer");
  }

  public Timer getTaskListTimer() {
    return taskListTimer;
  }

  public Timer getAppValidationTimer() {
    return appValidationTimer;
  }

  public Timer getDocumentGenerationTimer() {
    return documentGenerationTimer;
  }

  public Timer getDocumentPreviewTimer() {
    return documentPreviewTimer;
  }

  public Timer getStartAppTimer() {
    return startAppTimer;
  }

  public Timer getWorkAreaTabTimer() {
    return workAreaTabTimer;
  }


}
