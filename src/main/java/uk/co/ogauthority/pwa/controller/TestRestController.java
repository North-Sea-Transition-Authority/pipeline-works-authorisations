package uk.co.ogauthority.pwa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetailIdent;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetailIdentData;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetailMigrationData;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineDetailIdentDataRepository;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineDetailIdentRepository;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineDetailMigrationDataRepository;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineDetailRepository;
import uk.co.ogauthority.pwa.repository.pipelines.PipelineRepository;

/**
 * Test/debug controller for various session related endpoints.
 * Can be removed before production release.
 */
@RestController
public class TestRestController {

  private final PipelineRepository pipelineRepository;
  private final PipelineDetailRepository pipelineDetailRepository;
  private final PipelineDetailIdentRepository pipelineDetailIdentRepository;
  private final PipelineDetailIdentDataRepository pipelineDetailIdentDataRepository;
  private final PipelineDetailMigrationDataRepository pipelineDetailMigrationDataRepository;

  @Autowired
  public TestRestController(PipelineRepository pipelineRepository,
                            PipelineDetailRepository pipelineDetailRepository,
                            PipelineDetailIdentRepository pipelineDetailIdentRepository,
                            PipelineDetailIdentDataRepository pipelineDetailIdentDataRepository,
                            PipelineDetailMigrationDataRepository pipelineDetailMigrationDataRepository) {
    this.pipelineRepository = pipelineRepository;
    this.pipelineDetailRepository = pipelineDetailRepository;
    this.pipelineDetailIdentRepository = pipelineDetailIdentRepository;
    this.pipelineDetailIdentDataRepository = pipelineDetailIdentDataRepository;
    this.pipelineDetailMigrationDataRepository = pipelineDetailMigrationDataRepository;
  }

  @GetMapping("/pipelines")
  public Iterable<Pipeline> pipelines() {
    return pipelineRepository.findAll();
  }

  @GetMapping("/pipeline-details")
  public Iterable<PipelineDetail> pipelineDetails() {
    return pipelineDetailRepository.findAll();
  }

  @GetMapping("/pipeline-detail-idents")
  public Iterable<PipelineDetailIdent> pipelineDetailIdents() {
    return pipelineDetailIdentRepository.findAll();
  }

  @GetMapping("/pipeline-detail-ident-data")
  public Iterable<PipelineDetailIdentData> pipelineDetailIdentData() {
    return pipelineDetailIdentDataRepository.findAll();
  }

  @GetMapping("/pipeline-detail-migration-data")
  public Iterable<PipelineDetailMigrationData> pipelineDetailMigrationData() {
    return pipelineDetailMigrationDataRepository.findAll();
  }

}
