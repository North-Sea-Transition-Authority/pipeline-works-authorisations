package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.enums.fluidcomposition.Chemical;
import uk.co.ogauthority.pwa.model.entity.enums.fluidcomposition.FluidCompositionOption;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadFluidCompositionInfo;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelinetechinfo.PadFluidCompositionInfoRepository;

@Service
@Profile("development")
public class FluidCompositionGeneratorService {

  private final PadFluidCompositionInfoRepository padFluidCompositionInfoRepository;


  @Autowired
  public FluidCompositionGeneratorService(
      PadFluidCompositionInfoRepository padFluidCompositionInfoRepository) {
    this.padFluidCompositionInfoRepository = padFluidCompositionInfoRepository;
  }


  public void generateFluidComposition(PwaApplicationDetail pwaApplicationDetail) {

    var fluidCompositionEntities = createFluidCompositionEntities(pwaApplicationDetail);
    padFluidCompositionInfoRepository.saveAll(fluidCompositionEntities);
  }


  private List<PadFluidCompositionInfo> createFluidCompositionEntities(PwaApplicationDetail pwaApplicationDetail) {

    var fluidCompositionEntities = Chemical.asList().stream()
        .map(chemical -> {
          var fluidComposition =  new PadFluidCompositionInfo();
          fluidComposition.setPwaApplicationDetail(pwaApplicationDetail);
          fluidComposition.setChemicalName(chemical);
          fluidComposition.setFluidCompositionOption(FluidCompositionOption.NONE);
          return fluidComposition;
        })
        .collect(Collectors.toList());

    var firstFluidComposition = fluidCompositionEntities.get(0);
    firstFluidComposition.setFluidCompositionOption(FluidCompositionOption.HIGHER_AMOUNT);
    firstFluidComposition.setMoleValue(BigDecimal.valueOf(99));

    return fluidCompositionEntities;
  }

}
