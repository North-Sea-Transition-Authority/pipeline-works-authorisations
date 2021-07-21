package uk.co.ogauthority.pwa.service.testharness;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import uk.co.ogauthority.pwa.energyportal.service.teams.PortalTeamAccessor;
import uk.co.ogauthority.pwa.model.form.testharness.GenerateApplicationForm;
import uk.co.ogauthority.pwa.service.person.PersonService;
import uk.co.ogauthority.pwa.validators.testharness.GenerateApplicationValidator;

@RunWith(MockitoJUnitRunner.class)
public class TestHarnessServiceTest {

  @Mock
  private GenerateApplicationValidator generateApplicationValidator;

  @Mock
  private PortalTeamAccessor portalTeamAccessor;

  @Mock
  private PersonService personService;

  @Mock
  private Scheduler scheduler;

  private TestHarnessService testHarnessService;


  @Before
  public void setup(){
    testHarnessService = new TestHarnessService(generateApplicationValidator, portalTeamAccessor, personService, scheduler);
  }

  @Test
  public void scheduleGenerateApplicationJob_verifyServiceInteractions() throws SchedulerException {

    testHarnessService.scheduleGenerateApplicationJob(new GenerateApplicationForm());

    verify(scheduler).scheduleJob(any(), any());
  }


}