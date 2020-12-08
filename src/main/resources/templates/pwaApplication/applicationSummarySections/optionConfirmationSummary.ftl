<#include '../../pwaLayoutImports.ftl'>
<#import '../shared/options/optionConfirmationSummary.ftl' as optionsConfirmationSummary>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="view" type="uk.co.ogauthority.pwa.service.pwaapplications.options.PadConfirmationOfOptionView" -->


<div class="pwa-application-summary-section">
    <h2 class="govuk-heading-l" id="optionConfirmation">${sectionDisplayText}</h2>

    <@optionsConfirmationSummary.summary view/>

</div>

