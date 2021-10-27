package uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings;

import java.util.Arrays;
import java.util.Set;
import org.apache.commons.lang3.RandomUtils;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawing;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawingLink;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawingLink_;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.techdrawings.PadTechnicalDrawing_;
import uk.co.ogauthority.pwa.testutils.ObjectTestUtils;

public class PadTechnicalDrawingTestUtil {

  private PadTechnicalDrawingTestUtil(){}

  public static PadTechnicalDrawing createPadTechnicalDrawing(PwaApplicationDetail pwaApplicationDetail, PadFile padFile){

    var td = new PadTechnicalDrawing();
    td.setPwaApplicationDetail(pwaApplicationDetail);
    td.setFile(padFile);
    //dont care what the reference is
    td.setReference(Arrays.toString(RandomUtils.nextBytes(10)));

    ObjectTestUtils.assertAllFieldsNotNull(td, PadTechnicalDrawing.class, Set.of(PadTechnicalDrawing_.ID));
    return td;
  }

  public static PadTechnicalDrawingLink createPadTechnicalDrawingLink(PadTechnicalDrawing padTechnicalDrawing,
                                                                      PadPipeline padPipeline){

    var link = new PadTechnicalDrawingLink();
    link.setPipeline(padPipeline);
    link.setTechnicalDrawing(padTechnicalDrawing);

    ObjectTestUtils.assertAllFieldsNotNull(link, PadTechnicalDrawingLink.class, Set.of(PadTechnicalDrawingLink_.ID));
    return link;
  }


}
