package uk.co.ogauthority.pwa.service.users;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.UserAccountService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.internal.WebUserAccountRepository;

@RunWith(MockitoJUnitRunner.class)
public class UserAccountServiceTest {

  private static final int WUA_ID = 1;

  @Mock
  private WebUserAccountRepository webUserAccountRepository;

  private UserAccountService userAccountService;

  @Before
  public void setup(){
    userAccountService = new UserAccountService(WUA_ID, webUserAccountRepository);
  }

  @Test
  public void getSystemWebUserAccount_serviceInteractions() {
    when(webUserAccountRepository.findById(any())).thenReturn(Optional.of(new WebUserAccount()));
    var wua = userAccountService.getSystemWebUserAccount();
    verify(webUserAccountRepository, times(1)).findById(WUA_ID);
  }
}