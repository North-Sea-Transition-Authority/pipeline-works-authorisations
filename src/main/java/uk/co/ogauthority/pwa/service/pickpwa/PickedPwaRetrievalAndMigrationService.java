package uk.co.ogauthority.pwa.service.pickpwa;

import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.masterpwa.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwa.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.migration.MigrationMasterPwa;
import uk.co.ogauthority.pwa.service.masterpwa.MasterPwaAuthorisationService;
import uk.co.ogauthority.pwa.service.migration.MigrationDataAccessor;
import uk.co.ogauthority.pwa.service.migration.PipelineAuthorisationMigrationService;

@Service
public class PickedPwaRetrievalAndMigrationService {

  private final MasterPwaAuthorisationService masterPwaAuthorisationService;
  private final PipelineAuthorisationMigrationService pipelineAuthorisationMigrationService;
  private final MigrationDataAccessor migrationDataAccessor;

  @Autowired
  public PickedPwaRetrievalAndMigrationService(MasterPwaAuthorisationService masterPwaAuthorisationService,
                                               PipelineAuthorisationMigrationService pipelineAuthorisationMigrationService,
                                               MigrationDataAccessor migrationDataAccessor) {
    this.masterPwaAuthorisationService = masterPwaAuthorisationService;
    this.pipelineAuthorisationMigrationService = pipelineAuthorisationMigrationService;
    this.migrationDataAccessor = migrationDataAccessor;
  }


  public List<PickablePwaDto> getPickablePwasWhereAuthorised(WebUserAccount webUserAccount) {
    List<MasterPwaDetail> masterPwas = masterPwaAuthorisationService.getMasterPwasWhereUserIsAuthorised(webUserAccount);
    List<MigrationMasterPwa> migrationPwas = migrationDataAccessor.getMasterPwasWhereUserIsAuthorisedAndNotMigrated(
        webUserAccount
    );

    List<PickablePwaDto> pickablePwaDtos = new ArrayList<>();
    for (MasterPwaDetail masterPwaDetail : masterPwas) {
      pickablePwaDtos.add(PickablePwaDto.from(masterPwaDetail));
    }

    for (MigrationMasterPwa migrationPwa : migrationPwas) {
      pickablePwaDtos.add(PickablePwaDto.from(migrationPwa));
    }

    return pickablePwaDtos;
  }

  @Transactional
  public MasterPwa getOrMigratePickedPwa(PickablePwa pickedPwaForVariation, WebUserAccount user) {

    switch (pickedPwaForVariation.getPickablePwaSource()) {
      case MASTER:
        return masterPwaAuthorisationService.getMasterPwaIfAuthorised(
            pickedPwaForVariation.getContentId(),
            user
        );
      case MIGRATION: {
        MigrationMasterPwa migrationMasterPwa = migrationDataAccessor
            .getMasterPwaWhereUserIsAuthorisedAndNotMigratedByPadId(user, pickedPwaForVariation.getContentId());
        return pipelineAuthorisationMigrationService.migrate(migrationMasterPwa).getMasterPwa();
      }
      default:
        throw new IllegalStateException("Unexpected value: " + pickedPwaForVariation.toString());
    }

  }

}
