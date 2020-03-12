package uk.co.ogauthority.pwa.service.pickpwa;

import java.util.Optional;
import org.flywaydb.core.internal.util.StringUtils;
import uk.co.ogauthority.pwa.model.entity.masterpwa.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.migration.MigrationMasterPwa;

public class PickablePwa {

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
    this(PickablePwaSource.MASTER,
        PickablePwaSource.MASTER.getPickableStringPrefix() + masterPwaDetail.getMasterPwaId(),
        masterPwaDetail.getMasterPwaId()
    );
  }

  PickablePwa(MigrationMasterPwa migrationMasterPwa) {
    this(PickablePwaSource.MIGRATION,
        PickablePwaSource.MIGRATION.getPickableStringPrefix() + migrationMasterPwa.getPadId(),
        migrationMasterPwa.getPadId()
    );
  }

  private PickablePwaSource findPwaSourceFromPickableString(String inputString) {
    if (inputString.startsWith(PickablePwaSource.MASTER.getPickableStringPrefix())) {
      return PickablePwaSource.MASTER;
    } else if (inputString.startsWith(PickablePwaSource.MIGRATION.getPickableStringPrefix())) {
      return PickablePwaSource.MIGRATION;
    } else {
      return PickablePwaSource.UNKNOWN;
    }

  }

  private Optional<Integer> getPickablePwaContentIdFromPickableString(String inputString) {
    Optional<String> content;
    switch (findPwaSourceFromPickableString(inputString)) {
      case MASTER:
        content = Optional.of(getPickableStringContentFrom(PickablePwaSource.MASTER, inputString));
        break;
      case MIGRATION:
        content = Optional.of(getPickableStringContentFrom(PickablePwaSource.MIGRATION, inputString));
        break;
      default:
        content = Optional.empty();
    }

    return content
        .filter(StringUtils::isNumeric)
        .map(Integer::valueOf);

  }

  private String getPickableStringContentFrom(PickablePwaSource source, String pickedString) {
    return pickedString.substring(source.getPickableStringPrefix().length());
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
