package uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawing;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawingLink;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.techdrawings.PadTechnicalDrawingLinkRepository;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Service
public class PadTechnicalDrawingLinkService {

  private final PadTechnicalDrawingLinkRepository padTechnicalDrawingLinkRepository;
  private final PadPipelineService padPipelineService;

  @Autowired
  public PadTechnicalDrawingLinkService(
      PadTechnicalDrawingLinkRepository padTechnicalDrawingLinkRepository,
      PadPipelineService padPipelineService) {
    this.padTechnicalDrawingLinkRepository = padTechnicalDrawingLinkRepository;
    this.padPipelineService = padPipelineService;
  }

  public List<PadTechnicalDrawingLink> getLinksFromDrawingList(List<PadTechnicalDrawing> drawings) {
    return padTechnicalDrawingLinkRepository.getAllByTechnicalDrawingIn(drawings);
  }

  public List<PadTechnicalDrawingLink> getLinksFromDrawing(PadTechnicalDrawing drawing) {
    return padTechnicalDrawingLinkRepository.getAllByTechnicalDrawing(drawing);
  }

  public List<PadTechnicalDrawingLink> getLinksFromAppDetail(PwaApplicationDetail pwaApplicationDetail) {
    return padTechnicalDrawingLinkRepository.getAllByTechnicalDrawing_PwaApplicationDetail(pwaApplicationDetail);
  }

  public List<PadPipelineKeyDto> getLinkedPipelineIds(PwaApplicationDetail detail) {
    return padTechnicalDrawingLinkRepository.getLinkedPipelineIdsByDetail(detail);
  }

  @Transactional
  public void linkDrawing(PwaApplicationDetail detail, List<Integer> padPipelineIds,
                          PadTechnicalDrawing technicalDrawing) {
    Map<Integer, PadPipeline> pipelines = padPipelineService.getByIdList(detail, padPipelineIds)
        .stream()
        .collect(StreamUtils.toLinkedHashMap(PadPipeline::getId, pipeline -> pipeline));

    var linkList = new ArrayList<PadTechnicalDrawingLink>();
    padPipelineIds.forEach(pipelineId -> {
      var link = new PadTechnicalDrawingLink();
      link.setPipeline(pipelines.get(pipelineId));
      link.setTechnicalDrawing(technicalDrawing);
      linkList.add(link);
    });
    padTechnicalDrawingLinkRepository.saveAll(linkList);
  }

  @Transactional
  public void unlinkDrawing(PwaApplicationDetail detail, PadTechnicalDrawing technicalDrawing) {
    var links = getLinksFromDrawingList(List.of(technicalDrawing));
    padTechnicalDrawingLinkRepository.deleteAll(links);
  }

  @Transactional
  public void removeAllPipelineLinks(PwaApplicationDetail detail, PadPipeline padPipeline) {
    var links = padTechnicalDrawingLinkRepository.getAllByTechnicalDrawing_PwaApplicationDetailAndPipeline(
        detail,
        padPipeline
    );
    padTechnicalDrawingLinkRepository.deleteAll(links);
  }

}
