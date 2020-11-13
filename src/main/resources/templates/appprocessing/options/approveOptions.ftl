<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->

<#include '../../layout.ftl'>

<@defaultPage htmlTitle="Approve options" topNavigation=true breadcrumbs=true fullWidthColumn=true>

    <@fdsError.errorSummary errorItems=errorList />

    <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />


    <@fdsInsetText.insetText >
      <p>Use update requests to get applicant to remove options that haven’t been approved by OGA or consultees. </p>
      <p>By approving using this screen you are giving the applicant consent to proceed with any of the options defined in the application.</p>
    </@fdsInsetText.insetText>


    <@fdsForm.htmlForm>
        <@fdsDateInput.dateInput
        labelText="Provide a deadline for the holder to submit option"
        fieldsetHeadingSize="h2"
        fieldsetHeadingClass="govuk-fieldset__legend--m"
        dayPath="form.deadlineDateDay" monthPath="form.deadlineDateMonth" yearPath="form.deadlineDateYear"
        formId="form.deadlineDate"/>

        <@fdsAction.submitButtons primaryButtonText="Approve options" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>
    </@fdsForm.htmlForm>
</@defaultPage>