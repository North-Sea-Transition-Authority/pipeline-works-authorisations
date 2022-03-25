package uk.co.ogauthority.pwa.controller;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.pwacontext.PwaContextService;

@TestConfiguration
public class PwaMvcTestConfiguration {

  @MockBean
  protected PwaApplicationContextService applicationContextService;

  @MockBean
  protected PwaAppProcessingContextService appProcessingContextService;

  @MockBean
  protected PwaContextService pwaContextService;

}
