<#include '../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="modifyStartDateUrl" type="java.lang.String" -->
<#-- @ftlvariable name="startDate" type="java.lang.String" -->

<@defaultPage htmlTitle="Fast-track" pageHeading="Fast-track" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>

    <@fdsInsetText.insetText>
        Your application will be fast-tracked as the start date is before the minimum review period.
        All fast-tracked applications require approval prior to being processed.
        <br/><br/>
        Current start date: ${startDate}
        <br/>
        <a href="${springUrl(modifyStartDateUrl)}" class="govuk-link">Click here to change your start date</a>
    </@fdsInsetText.insetText>

    <@fdsForm.htmlForm>

        <@fdsFieldset.fieldset legendHeading="Provide reasons for fast-tracking your application" legendHeadingSize="h2">
            <@fdsCheckbox.checkbox path="form.avoidEnvironmentalDisaster" labelText="Avoiding environmental disaster"/>
            <@fdsTextarea.textarea path="form.environmentalDisasterReason" labelText="Why have you selected this reason?"/>

            <@fdsCheckbox.checkbox path="form.savingBarrels" labelText="Save barrels"/>
            <@fdsTextarea.textarea path="form.savingBarrelsReason" labelText="Why have you selected this reason?"/>

            <@fdsCheckbox.checkbox path="form.projectPlanning" labelText="Project planning"/>
            <@fdsTextarea.textarea path="form.projectPlanningReason" labelText="Why have you selected this reason?"/>

            <@fdsCheckbox.checkbox path="form.hasOtherReason" labelText="Other reason"/>
            <@fdsTextarea.textarea path="form.otherReason" labelText="Why have you selected this reason?"/>
        </@fdsFieldset.fieldset>

        <@fdsAction.submitButtons primaryButtonText="Complete" secondaryButtonText="Save and complete later"/>
    </@fdsForm.htmlForm>

</@defaultPage>