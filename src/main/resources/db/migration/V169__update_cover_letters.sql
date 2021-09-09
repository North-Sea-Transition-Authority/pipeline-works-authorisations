INSERT INTO ${datasource.user}.template_text (text_type, text) VALUES (
  'HUOO_CONSENT_EMAIL_COVER_LETTER',
  '??Additional paragraphs if time tracked or other information required??'
);

INSERT INTO ${datasource.user}.template_text (text_type, text) VALUES (
  'DEPCON_CONSENT_EMAIL_COVER_LETTER',
  '??Additional paragraphs if time tracked or other information required??'
);

UPDATE ${datasource.user}.template_text
SET text = 'On completion of the construction of the pipeline, you must confirm to OGA that you have completed construction and whether the work have been constructed in accordance with ??paragraph 7?? of the PWA issued to you including the details set out in Table "A" attached to the authorisation. This information must be provided in the as-built notifications area of the PWA service.

??Further you must notify OGA the first time the pipeline(s) are brought into use quoting the pipeline numbers and the relevant PWA number.??

??Additional paragraphs if time tracked or other information required??'
WHERE text_type = 'VARIATION_CONSENT_EMAIL_COVER_LETTER';

UPDATE ${datasource.user}.template_text
SET text =
    '# Future Activities Associated with pipeline(s)

The PWA is a working document which should be made readily available to all those involved with the construction and use of the pipeline(s).

Under certain terms and conditions of the PWA, prior consent is necessary for future activities including:
- Modifications to the pipelines ??(term 7);??
- Consent to deposit materials ??(paragraph 8 of Schedule 2).??

# Reminders

Your attention is drawn also to the following:
- Paragraph 15 of Schedule 2 of the PWA sets out certain requirements to provide information.
- You should give the Hydrographer of the Navy at least 5 weeks prior notification of offshore activities so they may prepare notices to mariners and update Admiralty Charts.
- You should contact the Radio Navigation Warnings Section 24 hours before pipelaying is due to commence.  The contact numbers are: - Duty Officer Tel 01823 337900 ext 3289 or 01823 353448 (direct).  Fax 01823 322352. Email: navwarnings@btconnect.com.
- FRS/CEFAS and the fishing organisations require 28 days notice prior to construction.
- The OGA provides information on all new infrastructure developments on the OGA website.  Therefore on completion of the construction or siting of any installation and/or any sub-sea facilities, including pipelines, please e-mail "as laid" data to the NDR (NDRhelp@slb.com) with details of all the relevant locations of infrastructure associated with this development.
This information should be provided in the form of metadata descriptions and as laid coordinates, which indicate the position of all relevant field infrastructure including details of sub-sea riser connections, sub-sea functions and any associated pipeline, including any point at which the direction of the pipeline changes and where there are significant features.
For details on standard template submission formats for this, please contact the NDR direct (NDRhelp@slb.com).
- On completion of the construction of the pipeline, you must confirm to the OGA that you have completed construction and whether the work has been constructed in accordance with ??paragraph 7?? of the PWA including the details set out in Table "A" attached to the PWA.  This information must be provided in the as-built notifications area of the PWA service.

PWAs no longer include specific safety-related information, which is the responsibility of the Health and Safety Executive under the Pipelines Safety Regulations 1996 (S.I. 1996 No. 825).

??Additional paragraphs if time tracked or other information required??'
WHERE text_type = 'INITIAL_CONSENT_EMAIL_COVER_LETTER';