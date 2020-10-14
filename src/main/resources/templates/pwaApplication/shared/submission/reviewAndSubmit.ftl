<#include '../../../layoutPane.ftl'>

<#-- @ftlvariable name="appSummaryView" type="uk.co.ogauthority.pwa.model.view.appsummary.ApplicationSummaryView" -->
<#-- @ftlvariable name="combinedSummaryHtml" type="java.lang.String" -->
<#-- @ftlvariable name="taskListUrl" type="java.lang.String" -->
<#-- @ftlvariable name="applicationReference" type="java.lang.String" -->

<#assign pageHeading="Review and Submit Application ${applicationReference}"/>

<@defaultPagePane htmlTitle=pageHeading phaseBanner=false>

    <@pwaAppSummary.summary
        pageHeading=pageHeading
        appSummaryView=appSummaryView
        sidebarHeading="Check your answers for all questions in the application">

        <@fdsForm.htmlForm>
            <!-- Submit button macro not used to allow for hiding of button when application is not valid. -->
            <@fdsAction.button buttonText="Submit" buttonValue="submit" />
            <@fdsAction.link linkText="Back to task list" linkClass="govuk-link govuk-link--button" linkUrl=springUrl(taskListUrl)/>
        </@fdsForm.htmlForm>

    </@pwaAppSummary.summary>

</@defaultPagePane>