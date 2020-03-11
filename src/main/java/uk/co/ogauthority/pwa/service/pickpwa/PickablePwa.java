package uk.co.ogauthority.pwa.service.pickpwa;

import java.util.Optional;
import org.flywaydb.core.internal.util.StringUtils;
import uk.co.ogauthority.pwa.model.entity.masterpwa.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.migration.MigrationMasterPwa;

public class PickablePwa {
  static final String MASTER_PWA_PREFIX = "MASTER_PWA/";
  static final String MIGRATION_PWA_PREFIX = "MIGRATION_PWA/";

  private final PickablePwaSource pickablePwaSource;
  private final String pickablePwaString;
  private final Integer contentId;

  private PickablePwa(PickablePwaSource pickablePwaSource,
                      String pickedPwaString,
                      Integer contentId) {
    this.pickablePwaString = pickedPwaString;
    this.pickablePwaSource = pickablePwaSource;
    this.contentId = contentId;

  }

  public PickablePwa(String pickedPwaString) {
    this.pickablePwaString = pickedPwaString;
    this.pickablePwaSource = findPwaSourceFromPickableString(pickedPwaString);
    this.contentId = getPickablePwaContentIdFromPickableString(pickedPwaString)
        .orElseThrow(() -> new IllegalArgumentException("Could not decode picked pwa string:" + pickedPwaString));
  }


  PickablePwa(MasterPwaDetail masterPwaDetail) {
    this(PickablePwaSource.MASTER, MASTER_PWA_PREFIX + masterPwaDetail.getMasterPwaId(),
        masterPwaDetail.getMasterPwaId());
  }

  PickablePwa(MigrationMasterPwa migrationMasterPwa) {
    this(PickablePwaSource.MIGRATION, MIGRATION_PWA_PREFIX + migrationMasterPwa.getPadId(),
        migrationMasterPwa.getPadId());
  }

  private PickablePwaSource findPwaSourceFromPickableString(String inputString) {
    if (inputString.startsWith(MASTER_PWA_PREFIX)) {
      return PickablePwaSource.MASTER;
    } else if (inputString.startsWith(MIGRATION_PWA_PREFIX)) {
      return PickablePwaSource.MIGRATION;
    } else {
      return PickablePwaSource.UNKNOWN;
    }

  }

  private Optional<Integer> getPickablePwaContentIdFromPickableString(String inputString) {
    Optional<String> content;
    switch (findPwaSourceFromPickableString(inputString)) {
      case MASTER:
        content = Optional.of(inputString.substring(MASTER_PWA_PREFIX.length()));
        break;
      case MIGRATION:
        content = Optional.of(inputString.substring(MIGRATION_PWA_PREFIX.length()));
        break;
      default:
        content = Optional.empty();
    }

    return content
        .filter(StringUtils::isNumeric)
        .map(Integer::valueOf);

  }

  public static String getMasterPwaPrefix() {
    return MASTER_PWA_PREFIX;
  }

  public static String getMigrationPwaPrefix() {
    return MIGRATION_PWA_PREFIX;
  }

  public PickablePwaSource getPickablePwaSource() {
    return pickablePwaSource;
  }

  public String getPickablePwaString() {
    return pickablePwaString;
  }

  public Integer getContentId() {
    return contentId;
  }


  @Override
  public String toString() {
    return "PickedPwaForVariation{" +
        "pickPwaType=" + pickablePwaSource +
        ", pickedPwaString='" + pickablePwaString + '\'' +
        ", contentId=" + contentId +
        '}';
  }
}
