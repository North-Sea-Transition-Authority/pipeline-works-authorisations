package uk.co.ogauthority.pwa.model.entity.enums.permanentdeposits;

import uk.co.ogauthority.pwa.service.enums.projectinformation.PermanentDepositRadioOption;

import java.util.Arrays;
import java.util.List;

public enum MaterialUnitType {

    CONCRETE_MATTRESSES("m"),
    ROCK("1-5"),
    GROUT_BAGS("kg"),
    OTHER("size");

    private final String displayText;

    MaterialUnitType(String displayText) {
        this.displayText = displayText;
    }

    public String getDisplayText() {
        return displayText;
    }

    public static List<MaterialUnitType> asList() {
        return Arrays.asList(MaterialUnitType.values());
    }
}
