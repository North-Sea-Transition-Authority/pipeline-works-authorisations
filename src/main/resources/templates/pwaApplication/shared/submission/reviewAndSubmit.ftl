<#include '../../../layoutPane.ftl'>

<#-- @ftlvariable name="appSummaryView" type="uk.co.ogauthority.pwa.model.view.appsummary.ApplicationSummaryView" -->
<#-- @ftlvariable name="combinedSummaryHtml" type="java.lang.String" -->
<#-- @ftlvariable name="taskListUrl" type="java.lang.String" -->
<#-- @ftlvariable name="applicationReference" type="java.lang.String" -->
<#-- @ftlvariable name="openUpdateRequest" type="java.lang.Boolean" -->

<#assign pageHeading="Review and Submit Application ${applicationReference}"/>

<@defaultPagePane htmlTitle=pageHeading phaseBanner=false>

    <@pwaAppSummary.summary
    pageHeading=pageHeading
    appSummaryView=appSummaryView
    sidebarHeading="Check your answers for all questions in the application"
    errorList=errorList>

        <@fdsForm.htmlForm>
          <!-- Submit button macro not used to allow for hiding of button when application is not valid. -->

            <#if openUpdateRequest>
                <@fdsRadio.radioGroup
                path="form.madeOnlyRequestedChanges"
                labelText="Describe the update"
                hiddenContent=true
                fieldsetHeadingSize="h2" fieldsetHeadingClass="govuk-fieldset__legend--l">
                    <@fdsRadio.radioItem path="form.madeOnlyRequestedChanges" itemMap={"true":"Requested changes only"} isFirstItem=true/>
                    <@fdsRadio.radioItem path="form.madeOnlyRequestedChanges" itemMap={"false":"Other changes"} >
                        <@fdsTextarea.textarea path="form.otherChangesDescription" labelText="Describe the changes that have been made" nestingPath="form.madeOnlyRequestedChanges" characterCount=true maxCharacterLength="4000"/>
                    </@fdsRadio.radioItem>
                </@fdsRadio.radioGroup>

            </#if>

            <@fdsAction.button buttonText="Submit" buttonValue="submit" />
            <@fdsAction.link linkText="Back to task list" linkClass="govuk-link govuk-link--button" linkUrl=springUrl(taskListUrl)/>
        </@fdsForm.htmlForm>

    </@pwaAppSummary.summary>

</@defaultPagePane>