package uk.co.ogauthority.pwa.externalapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;

@DataJpaTest
@RunWith(SpringRunner.class)
@ActiveProfiles("integration-test")
@DirtiesContext
@Transactional
public class PipelineDtoRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  private MasterPwaDetail pwaDetail;

  private Pipeline pipeline;

  private PipelineDetail pipelineDetail;
  private PipelineDetail secondPipelineDetail;

  @Autowired
  PipelineDtoRepository pipelineDtoRepository;

  @Before
  public void setUp() {

    var pwa = new MasterPwa();
    var secondPwa = new MasterPwa();
    entityManager.persist(pwa);
    entityManager.persist(secondPwa);

    pwaDetail = new MasterPwaDetail(pwa, null, "pwa detail reference", Instant.now(), null);
    var secondPwaDetail = new MasterPwaDetail(secondPwa, null, "non like match ref", Instant.now(), null);
    entityManager.persist(pwaDetail);
    entityManager.persist(secondPwaDetail);

    pipeline = new Pipeline();
    pipeline.setMasterPwa(pwa);
    var secondPipeline = new Pipeline();
    secondPipeline.setMasterPwa(secondPwa);
    entityManager.persist(pipeline);
    entityManager.persist(secondPipeline);

    pipelineDetail = new PipelineDetail(pipeline);
    pipelineDetail.setPipelineNumber("pipeline number");
    pipelineDetail.setTipFlag(true);
    secondPipelineDetail = new PipelineDetail(secondPipeline);
    secondPipelineDetail.setPipelineNumber("second pipeline number");
    secondPipelineDetail.setTipFlag(true);
    var thirdPipelineDetail = new PipelineDetail(secondPipeline);
    thirdPipelineDetail.setPipelineNumber("non like match number");
    thirdPipelineDetail.setTipFlag(true);
    entityManager.persist(pipelineDetail);
    entityManager.persist(secondPipelineDetail);
  }

  @Test
  public void searchPipelines_searchByPipelineId() {
    var searchedIds = List.of(pipeline.getId());
    var resultingPipelineDtos = pipelineDtoRepository.searchPipelines(searchedIds, null, null, null);

    assertThat(resultingPipelineDtos)
        .extracting(PipelineDto::getId)
        .containsExactly(pipeline.getId());
  }

  @Test
  public void searchPipelines_searchByPipelineNumber() {
    var pipelineNumber = "PIPELINE NUM";
    var resultingPipelineDtos = pipelineDtoRepository.searchPipelines(null, pipelineNumber, null, null);

    assertThat(resultingPipelineDtos)
        .extracting(PipelineDto::getPipelineNumber)
        .containsExactly(pipelineDetail.getPipelineNumber(), secondPipelineDetail.getPipelineNumber());

    pipelineNumber = "pipeline num";
    resultingPipelineDtos = pipelineDtoRepository.searchPipelines(null, pipelineNumber, null, null);

    assertThat(resultingPipelineDtos)
        .extracting(PipelineDto::getPipelineNumber)
        .containsExactly(pipelineDetail.getPipelineNumber(), secondPipelineDetail.getPipelineNumber());
  }

  @Test
  public void searchPipelines_searchByPwaReference() {
    var pwaReference = "REFERENCE";
    var resultingPipelineDtos = pipelineDtoRepository.searchPipelines(null, null, null, pwaReference);

    assertThat(resultingPipelineDtos)
        .extracting(pipelineDto -> pipelineDto.getPwa().getReference())
        .containsExactly(pwaDetail.getReference());

    pwaReference = "reference";
    resultingPipelineDtos = pipelineDtoRepository.searchPipelines(null, null, null, pwaReference);

    assertThat(resultingPipelineDtos)
        .extracting(pipelineDto -> pipelineDto.getPwa().getReference())
        .containsExactly(pwaDetail.getReference());
  }

  @Test
  public void searchPipelines_searchByPwaIds() {
    var pwaIds = List.of(pwaDetail.getMasterPwaId());
    var resultingPipelineDtos = pipelineDtoRepository.searchPipelines(null, null, pwaIds, null);

    assertThat(resultingPipelineDtos)
        .extracting(pipelineDto -> pipelineDto.getPwa().getId())
        .containsExactly(pwaDetail.getMasterPwaId());
  }


  @Test
  public void searchPipelines_whenAllNull_assertAllPipelinesReturned() {
    var resultingPipelineDtos = pipelineDtoRepository.searchPipelines(null, null, null, null);

    assertThat(resultingPipelineDtos)
        .extracting(PipelineDto::getId)
        .containsExactly(pipeline.getId(), secondPipelineDetail.getPipeline().getId());
  }

  @Test
  public void searchPipelines_orderedByPipelineId() {
    var ids = List.of(secondPipelineDetail.getPipeline().getId(), pipeline.getId());
    var resultingPipelineDtos = pipelineDtoRepository.searchPipelines(ids, null, null, null);

    var resultingPipelineIds = resultingPipelineDtos.stream()
        .map(PipelineDto::getId)
        .collect(Collectors.toList());
    var sortedPipelineIds = List.copyOf(resultingPipelineIds).stream()
        .sorted()
        .collect(Collectors.toList());

    assertThat(resultingPipelineIds).isEqualTo(sortedPipelineIds);
  }
}