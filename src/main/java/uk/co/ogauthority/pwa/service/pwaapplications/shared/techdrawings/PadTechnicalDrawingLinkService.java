package uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawing;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawingLink;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.techdetails.PipelineDrawingForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.techdrawings.PadTechnicalDrawingLinkRepository;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
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

  public void linkDrawing(PwaApplicationDetail detail, PipelineDrawingForm form, PadTechnicalDrawing technicalDrawing) {
    Map<Integer, PadPipeline> pipelines = padPipelineService.getByIdList(detail, form.getPipelineIds())
        .stream()
        .collect(StreamUtils.toLinkedHashMap(PadPipeline::getId, pipeline -> pipeline));

    var linkList = new ArrayList<PadTechnicalDrawingLink>();
    form.getPipelineIds().forEach(pipelineId -> {
      var link = new PadTechnicalDrawingLink();
      link.setPipeline(pipelines.get(pipelineId));
      link.setTechnicalDrawing(technicalDrawing);
      linkList.add(link);
    });
    padTechnicalDrawingLinkRepository.saveAll(linkList);
  }
}
