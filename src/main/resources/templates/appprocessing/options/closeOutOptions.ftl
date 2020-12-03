<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->
<#-- @ftlvariable name="padConfirmationOfOptionView" type="uk.co.ogauthority.pwa.service.pwaapplications.options.PadConfirmationOfOptionView" -->


<#include '../../layout.ftl'>
<#import '../../pwaApplication/shared/options/optionConfirmationSummary.ftl' as optionsConfirmationSummary>

<@defaultPage htmlTitle="Close out options variation" topNavigation=true breadcrumbs=true fullWidthColumn=true>

    <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

    <h2 class="govuk-heading-l"> Are you sure you want to close out this options variation?</h2>

    <@fdsInsetText.insetText >
      <p>You may close out this application without processing a consent as the applicant has confirmed that an approved option has not been done.</p>
      <p>Application update requests may be used to allow applicant to change their option confirmation.</p>
    </@fdsInsetText.insetText>

    <@optionsConfirmationSummary.summary view=padConfirmationOfOptionView />

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons primaryButtonText="Close out options" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>
    </@fdsForm.htmlForm>
</@defaultPage>