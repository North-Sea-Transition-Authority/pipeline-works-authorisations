package uk.co.ogauthority.pwa.integrations.energyportal.devukfields.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfields.internal.DevukFieldRepository;

@ExtendWith(MockitoExtension.class)
class DevukFieldServiceTest {

  @Mock
  private DevukFieldRepository devukFieldRepository;

  private DevukFieldService devukFieldService;

  @BeforeEach
  void setUp() {
    devukFieldService = new DevukFieldService(devukFieldRepository);
  }

  @Test
  void getByStatusRange() {
    var field = new DevukField();
    var statusCodes = List.of(9999);
    when(devukFieldRepository.findAllByStatusNotIn(statusCodes)).thenReturn(List.of(field));
    assertThat(devukFieldService.getAllFields()).containsExactly(field);
    verify(devukFieldRepository, times(1)).findAllByStatusNotIn(statusCodes);
  }

  @Test
  void findById() {
    var field = new DevukField();
    when(devukFieldRepository.findById(1)).thenReturn(Optional.of(field));
    assertThat(devukFieldService.findById(1)).isEqualTo(field);
  }

  @Test
  void findById_Invalid() {
    when(devukFieldRepository.findById(2)).thenReturn(Optional.empty());
    assertThrows(PwaEntityNotFoundException.class, () ->
      devukFieldService.findById(2));
  }

}