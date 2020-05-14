package uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdent;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdentData;

public interface PadPipelineIdentDataRepository extends CrudRepository<PadPipelineIdentData, Integer> {

  List<PadPipelineIdentData> getAllByPadPipelineIdentIn(List<PadPipelineIdent> idents);

}
