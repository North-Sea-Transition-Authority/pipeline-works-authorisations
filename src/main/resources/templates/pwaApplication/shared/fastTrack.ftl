<#include '../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->

<@defaultPage htmlTitle="Fast-track" pageHeading="Fast-track" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>

    <@fdsWarning.warning>
        Your application will be fast-tracked as the start date is outside of the minimum review period.
        All fast-tracked applications require approval prior to being processed.
        <br/><br/>
        Current start date: ${startDate}
        <br/>
        <a href="${springUrl(modifyStartDateUrl)}" class="govuk-link">Click here to change your start date</a>
    </@fdsWarning.warning>

    <@fdsForm.htmlForm>

        <@fdsFieldset.fieldset legendHeading="Provide reasons for fast-tracking your application" legendHeadingSize="h2">
            <@fdsCheckbox.checkbox path="form.avoidEnvironmentalDisaster" labelText="Avoiding environmental disaster"/>
            <@fdsTextarea.textarea path="form.environmentalDisasterReason" labelText="Avoiding environmental disaster reason"/>

            <@fdsCheckbox.checkbox path="form.savingBarrels" labelText="Save barrels"/>
            <@fdsTextarea.textarea path="form.environmentalDisasterReason" labelText="Save barrels reason"/>

            <@fdsCheckbox.checkbox path="form.projectPlanning" labelText="Project planning"/>
            <@fdsTextarea.textarea path="form.projectPlanningReason" labelText="Project planning reason"/>

            <@fdsCheckbox.checkbox path="form.hasOtherReason" labelText="Other reason"/>
            <@fdsTextarea.textarea path="form.otherReason" labelText="Other reasons and their justifications"/>
        </@fdsFieldset.fieldset>

<#--        <@fdsCheckbox.checkboxes path="form.fastTrackReasons" checkboxes=reasons/>-->

        <@fdsAction.submitButtons primaryButtonText="Complete" secondaryButtonText="Save and complete later"/>
    </@fdsForm.htmlForm>

</@defaultPage>