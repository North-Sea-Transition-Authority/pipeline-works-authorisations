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
        <br/><br/>
        You will lose any progress on this page by clicking this link.
    </@fdsInsetText.insetText>

     <@fdsDetails.summaryDetails summaryTitle="What justification do I need to fast track my application for the above reasons?">
        <ul>
            <li> Avoiding environmental disaster: work required to make the pipeline safe to avoid immediate risks of pipeline rupturing or leaking and to make safe for other users of the sea and why </li>
            <li> Save barrels: quantification of the number of barrels saved and why </li>
            <li> Project planning: quantification of money saved for example due to staff costs or boat hire </li>
            <li> Other reason: fast tracking for reasons not outlined above, ensure full details are provided to support your case </li>
        </ul>
    </@fdsDetails.summaryDetails>

    <@fdsForm.htmlForm>

        <@fdsFieldset.fieldset legendHeading="Provide reasons for fast-tracking your application" legendHeadingSize="h2">

            <@fdsCheckbox.checkboxGroup path="form.avoidEnvironmentalDisaster" hiddenContent=true>
              <@fdsCheckbox.checkboxItem path="form.avoidEnvironmentalDisaster" labelText="Avoiding environmental disaster">
                <@fdsTextarea.textarea path="form.environmentalDisasterReason" labelText="Why have you selected this reason?" nestingPath="form.avoidEnvironmentalDisaster" characterCount=true maxCharacterLength="4000"/>
              </@fdsCheckbox.checkboxItem>
                <@fdsCheckbox.checkboxItem path="form.savingBarrels" labelText="Save barrels">
                    <@fdsTextarea.textarea path="form.savingBarrelsReason" labelText="Why have you selected this reason?" nestingPath="form.savingBarrels" characterCount=true maxCharacterLength="4000"/>
                </@fdsCheckbox.checkboxItem>
                <@fdsCheckbox.checkboxItem path="form.projectPlanning" labelText="Project planning">
                    <@fdsTextarea.textarea path="form.projectPlanningReason" labelText="Why have you selected this reason?" nestingPath="form.projectPlanning" characterCount=true maxCharacterLength="4000"/>
                </@fdsCheckbox.checkboxItem>
                <@fdsCheckbox.checkboxItem path="form.hasOtherReason" labelText="Other reason">
                    <@fdsTextarea.textarea path="form.otherReason" labelText="Why have you selected this reason?" nestingPath="form.hasOtherReason" characterCount=true maxCharacterLength="4000"/>
                </@fdsCheckbox.checkboxItem>
            </@fdsCheckbox.checkboxGroup>

        </@fdsFieldset.fieldset>

        <@fdsAction.submitButtons primaryButtonText=submitPrimaryButtonText secondaryButtonText=submitSecondaryButtonText/>
    </@fdsForm.htmlForm>

</@defaultPage>