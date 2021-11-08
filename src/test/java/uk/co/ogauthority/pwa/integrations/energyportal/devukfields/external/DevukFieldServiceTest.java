package uk.co.ogauthority.pwa.integrations.energyportal.devukfields.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfields.internal.DevukFieldRepository;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;

@RunWith(MockitoJUnitRunner.class)
public class DevukFieldServiceTest {

  @Mock
  private DevukFieldRepository devukFieldRepository;

  private DevukFieldService devukFieldService;

  @Before
  public void setUp() {
    devukFieldService = new DevukFieldService(devukFieldRepository);
  }

  @Test
  public void getByOrganisationUnitAndStatusRange() {
    var orgUnit = new PortalOrganisationUnit();
    var field = new DevukField();
    var statusCodes = List.of(100, 200, 300);
    when(devukFieldRepository.findAllByOperatorOuIdAndStatusIn(orgUnit.getOuId(), statusCodes)).thenReturn(
        List.of(field));
    assertThat(devukFieldService.getByOrganisationUnitWithStatusCodes(orgUnit, statusCodes)).containsExactly(field);
    verify(devukFieldRepository, times(1)).findAllByOperatorOuIdAndStatusIn(orgUnit.getOuId(), statusCodes);
  }

  @Test
  public void getByStatusRange() {
    var field = new DevukField();
    var statusCodes = List.of(100, 200, 300);
    when(devukFieldRepository.findAllByStatusIn(statusCodes)).thenReturn(
            List.of(field));
    assertThat(devukFieldService.getByStatusCodes(statusCodes)).containsExactly(field);
    verify(devukFieldRepository, times(1)).findAllByStatusIn(statusCodes);
  }

  @Test
  public void findById() {
    var field = new DevukField();
    when(devukFieldRepository.findById(1)).thenReturn(Optional.of(field));
    assertThat(devukFieldService.findById(1)).isEqualTo(field);
  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void findById_Invalid() {
    when(devukFieldRepository.findById(2)).thenReturn(Optional.empty());
    devukFieldService.findById(2);
  }
}