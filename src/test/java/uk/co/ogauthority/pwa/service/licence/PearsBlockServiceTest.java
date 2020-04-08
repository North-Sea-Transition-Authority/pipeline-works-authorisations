package uk.co.ogauthority.pwa.service.licence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageRequest;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.BlockLocation;
import uk.co.ogauthority.pwa.model.entity.enums.LicenceStatus;
import uk.co.ogauthority.pwa.model.entity.licence.PearsBlock;
import uk.co.ogauthority.pwa.model.entity.licence.PearsLicence;
import uk.co.ogauthority.pwa.repository.licence.PearsBlockRepository;

@RunWith(MockitoJUnitRunner.class)
public class PearsBlockServiceTest {

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

  @Before
  public void setup() {
    pearsBlockService = new PearsBlockService(pearsBlockRepository, entityManager);

    when(pearsBlockRepository.findByCompositeKeyAndBlockLocation(
        licensedBlock.getCompositeKey(),
        BlockLocation.OFFSHORE
    ))
        .thenReturn(Optional.of(licensedBlock));
  }

  @Test
  public void findOffshorePickablePearsBlocks_whenBlockFounds() {

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
  public void getExtantOrUnlicensedOffshorePearsBlockByCompositeKey_whenLicenceIsExtant_andBlockOffshore() {
    when(pearsBlockRepository.findByCompositeKeyAndBlockLocation(
        licensedBlock.getCompositeKey(),
        BlockLocation.OFFSHORE
    ))
        .thenReturn(Optional.of(licensedBlock));

    assertThat(
        pearsBlockService.getExtantOrUnlicensedOffshorePearsBlockByCompositeKey(licensedBlock.getCompositeKey()).get())
        .isEqualTo(licensedBlock);
  }

  @Test
  public void getExtantOrUnlicensedOffshorePearsBlockByCompositeKey_whenLicenceIsNotExtand_andBlockOffshore() {
    licence.setLicenceStatus(LicenceStatus.SURRENDERED);
    assertThat(pearsBlockService.getExtantOrUnlicensedOffshorePearsBlockByCompositeKey(licensedBlock.getCompositeKey()))
        .isEmpty();
  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void getExtantOrUnlicensedOffshorePearsBlockByCompositeKeyOrError_whenBlockNotFoundAtAll() {

    pearsBlockService.getExtantOrUnlicensedOffshorePearsBlockByCompositeKeyOrError("unknown");
  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void getExtantOrUnlicensedOffshorePearsBlockByCompositeKeyOrError_whenBlockFoundButLicenceNotExtant() {
    licence.setLicenceStatus(LicenceStatus.SURRENDERED);

    pearsBlockService.getExtantOrUnlicensedOffshorePearsBlockByCompositeKeyOrError(licensedBlock.getCompositeKey());
  }
}