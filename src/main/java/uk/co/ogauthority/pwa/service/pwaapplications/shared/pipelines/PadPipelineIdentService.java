package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdent;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadPipelineIdentRepository;
import uk.co.ogauthority.pwa.util.CoordinateUtils;

@Service
public class PadPipelineIdentService {

  private final PadPipelineIdentRepository repository;
  private final PadPipelineIdentDataService identDataService;

  @Autowired
  public PadPipelineIdentService(PadPipelineIdentRepository repository,
                                 PadPipelineIdentDataService identDataService) {
    this.repository = repository;
    this.identDataService = identDataService;
  }

  public Optional<PadPipelineIdent> getMaxIdent(PadPipeline pipeline) {
    return repository.findTopByPadPipelineOrderByIdentNoDesc(pipeline);
  }

  @Transactional
  public void addIdent(PadPipeline pipeline, PipelineIdentForm form) {

    var numberOfIdents = repository.countAllByPadPipeline(pipeline);
    var ident = new PadPipelineIdent(pipeline, numberOfIdents.intValue() + 1);

    saveEntityUsingForm(ident, form);

    identDataService.addIdentData(ident, form.getDataForm());

  }

  public void saveEntityUsingForm(PadPipelineIdent ident, PipelineIdentForm form) {

    ident.setFromLocation(form.getFromLocation());
    ident.setFromCoordinates(CoordinateUtils.coordinatePairFromForm(form.getFromCoordinateForm()));

    ident.setToLocation(form.getToLocation());
    ident.setToCoordinates(CoordinateUtils.coordinatePairFromForm(form.getToCoordinateForm()));

    ident.setLength(form.getLength());

    repository.save(ident);

  }

}
