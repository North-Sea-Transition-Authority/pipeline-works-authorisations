package uk.co.ogauthority.pwa.model.entity.enums.permanentdeposits;

import uk.co.ogauthority.pwa.service.enums.projectinformation.PermanentDepositRadioOption;

import java.util.Arrays;
import java.util.List;

public enum MaterialType {

    CONCRETE_MATTRESSES("Concrete Mattresses"),
    ROCK("Rock"),
    GROUT_BAGS("Grout Bags"),
    OTHER("Other");

    private final String displayText;

    MaterialType(String displayText) {
        this.displayText = displayText;
    }

    public String getDisplayText() {
        return displayText;
    }

    public static List<MaterialType> asList() {
        return Arrays.asList(MaterialType.values());
    }
}
