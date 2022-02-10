package uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings;

import java.util.Arrays;
import java.util.Set;
import org.apache.commons.lang3.RandomUtils;
import uk.co.ogauthority.pwa.features.application.files.PadFile;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
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
