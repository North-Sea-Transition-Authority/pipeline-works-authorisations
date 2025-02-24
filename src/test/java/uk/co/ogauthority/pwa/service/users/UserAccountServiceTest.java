package uk.co.ogauthority.pwa.service.users;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.UserAccountService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.internal.WebUserAccountRepository;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

  private static final int WUA_ID = 1;

  @Mock
  private WebUserAccountRepository webUserAccountRepository;

  private UserAccountService userAccountService;

  @BeforeEach
  void setup(){
    userAccountService = new UserAccountService(WUA_ID, webUserAccountRepository);
  }

  @Test
  void getSystemWebUserAccount_serviceInteractions() {
    when(webUserAccountRepository.findById(any())).thenReturn(Optional.of(new WebUserAccount()));
    var wua = userAccountService.getSystemWebUserAccount();
    verify(webUserAccountRepository, times(1)).findById(WUA_ID);
  }
}