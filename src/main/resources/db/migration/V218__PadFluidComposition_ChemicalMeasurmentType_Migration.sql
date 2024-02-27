UPDATE ${datasource.user}.pad_fluid_composition_info pfci
SET pfci.fluid_composition_option = 'MOLE_PERCENTAGE'
WHERE pfci.fluid_composition_option = 'HIGHER_AMOUNT';
