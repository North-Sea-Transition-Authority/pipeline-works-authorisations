package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipielinetechinfo;

import java.util.Set;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadPipelineTechInfo;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadPipelineTechInfo_;
import uk.co.ogauthority.pwa.testutils.ObjectTestUtils;

public class PadPipelineTechInfoTestUtil {

  private PadPipelineTechInfoTestUtil() {
    //no instantiation
  }

  public static PadPipelineTechInfo createPadPipelineTechInfo(PwaApplicationDetail pwaApplicationDetail){

    var td = new PadPipelineTechInfo();
    td.setPwaApplicationDetail(pwaApplicationDetail);
    td.setCorrosionDescription("corrosian desc");
    td.setEstimatedFieldLife(10);
    td.setPipelineDesignedToStandards(true);
    td.setPipelineStandardsDescription("standards description");
    td.setPlannedPipelineTieInPoints(true);
    td.setTieInPointsDescription("tie in point desc");

    ObjectTestUtils.assertAllFieldsNotNull(
        td,
        PadPipelineTechInfo.class,
        Set.of(PadPipelineTechInfo_.ID)
        );
    return td;
  }
}
