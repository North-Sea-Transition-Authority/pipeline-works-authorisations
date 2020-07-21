ALTER TABLE ${datasource.user}.pwa_application_details ADD (
    partner_letters_required INTEGER CHECK(partner_letters_required IN (0, 1)),
    partner_letters_confirmed INTEGER CHECK(partner_letters_confirmed IN (0, 1))
    );


