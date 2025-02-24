package uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.internal.PearsBlockRepository;

@ExtendWith(MockitoExtension.class)
class PearsBlockServiceTest {

  @Mock
  private PearsBlockRepository pearsBlockRepository;

  @Mock
  private EntityManager entityManager;

  private PearsBlockService pearsBlockService;

  private PearsLicence licence = new PearsLicence(1, "P", 1, "P1", LicenceStatus.EXTANT);

  private PearsBlock licensedBlock = new PearsBlock(
      "licenceKey1",
      licence,
      "1/2/3",
      "3",
      "2",
      "1",
      BlockLocation.OFFSHORE);

  private PearsBlock unlicensedBlock = new PearsBlock(
      "unlicencedKey",
      null,
      "4/5/6",
      "4",
      "5",
      "6",
      BlockLocation.OFFSHORE);

  @BeforeEach
  void setup() {
    pearsBlockService = new PearsBlockService(pearsBlockRepository, entityManager);
  }

  @Test
  void findOffshorePickablePearsBlocks_whenBlockFounds() {

    var foundBlocks = List.of(
        licensedBlock,
        unlicensedBlock
    );

    TypedQuery mockQuery = mock(TypedQuery.class);
    //noinspection unchecked
    when(entityManager.createQuery(any(), any())).thenReturn(mockQuery);
    when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
    when(mockQuery.setFirstResult(anyInt())).thenReturn(mockQuery);
    when(mockQuery.setMaxResults(anyInt())).thenReturn(mockQuery);
    when(mockQuery.getResultList()).thenReturn(foundBlocks);

    var firstBlock = pearsBlockService.findOffshorePickablePearsBlocks("test", PageRequest.of(0, 10)).get(0);
    var secondBlock = pearsBlockService.findOffshorePickablePearsBlocks("test", PageRequest.of(0, 10)).get(1);

    assertThat(firstBlock.getKey()).isEqualTo("1/2/3 (P1)");
    assertThat(firstBlock.getData()).isEqualTo("licenceKey1");
    assertThat(secondBlock.getKey()).isEqualTo("4/5/6 (Unlicensed)");
    assertThat(secondBlock.getData()).isEqualTo("unlicencedKey");

  }

  @Test
  void getExtantOrUnlicensedOffshorePearsBlockByCompositeKey_whenLicenceIsExtant_andBlockOffshore() {
    when(pearsBlockRepository.findByCompositeKeyAndBlockLocation(
        licensedBlock.getCompositeKey(),
        BlockLocation.OFFSHORE
    ))
        .thenReturn(Optional.of(licensedBlock));

    assertThat(pearsBlockService.getExtantOrUnlicensedOffshorePearsBlockByCompositeKey(licensedBlock.getCompositeKey()))
        .contains(licensedBlock);
  }

  @Test
  void getExtantOrUnlicensedOffshorePearsBlockByCompositeKey_whenLicenceIsNotExtand_andBlockOffshore() {
    when(pearsBlockRepository.findByCompositeKeyAndBlockLocation(
        licensedBlock.getCompositeKey(),
        BlockLocation.OFFSHORE
    ))
        .thenReturn(Optional.of(licensedBlock));

    licence.setLicenceStatus(LicenceStatus.SURRENDERED);
    assertThat(pearsBlockService.getExtantOrUnlicensedOffshorePearsBlockByCompositeKey(licensedBlock.getCompositeKey()))
        .isEmpty();
  }

  @Test
  void getExtantOrUnlicensedOffshorePearsBlockByCompositeKeyOrError_whenBlockNotFoundAtAll() {
    assertThrows(PwaEntityNotFoundException.class, () ->

      pearsBlockService.getExtantOrUnlicensedOffshorePearsBlockByCompositeKeyOrError("unknown"));
  }

  @Test
  void getExtantOrUnlicensedOffshorePearsBlockByCompositeKeyOrError_whenBlockFoundButLicenceNotExtant() {
    when(pearsBlockRepository.findByCompositeKeyAndBlockLocation(
        licensedBlock.getCompositeKey(),
        BlockLocation.OFFSHORE
    ))
        .thenReturn(Optional.of(licensedBlock));

    licence.setLicenceStatus(LicenceStatus.SURRENDERED);
    assertThrows(PwaEntityNotFoundException.class, () ->

      pearsBlockService.getExtantOrUnlicensedOffshorePearsBlockByCompositeKeyOrError(licensedBlock.getCompositeKey()));
  }
}