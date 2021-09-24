<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->

<#include '../../layout.ftl'>

<@defaultPage htmlTitle="Change options approval deadline" topNavigation=true breadcrumbs=true fullWidthColumn=true>

    <@grid.gridRow>
        <@grid.twoThirdsColumn>
            <#if errorList?has_content>
                <@fdsError.errorSummary errorItems=errorList />
            </#if>
        </@grid.twoThirdsColumn>
    </@grid.gridRow>

    <@grid.gridRow>
        <@grid.fullColumn>
            <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />
        </@grid.fullColumn>
    </@grid.gridRow>

    <@grid.gridRow>
        <@grid.twoThirdsColumn>

            <@fdsForm.htmlForm>
                <@fdsDateInput.dateInput
                labelText="Provide a deadline for the chosen option to be confirmed"
                fieldsetHeadingSize="h2"
                fieldsetHeadingClass="govuk-fieldset__legend--m"
                dayPath="form.deadlineDateDay" monthPath="form.deadlineDateMonth" yearPath="form.deadlineDateYear"
                formId="form.deadlineDate"/>

                <@fdsTextarea.textarea path="form.note" labelText="Why has the deadline changed?" maxCharacterLength=maxCharacterLength?c characterCount=true/>

                <@fdsAction.submitButtons primaryButtonText="Change confirmation of option deadline" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>
            </@fdsForm.htmlForm>

        </@grid.twoThirdsColumn>
    </@grid.gridRow>

</@defaultPage>