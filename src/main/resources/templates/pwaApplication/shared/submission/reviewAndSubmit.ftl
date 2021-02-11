<#include '../../../layoutPane.ftl'>

<#-- @ftlvariable name="appSummaryView" type="uk.co.ogauthority.pwa.model.view.appsummary.ApplicationSummaryView" -->
<#-- @ftlvariable name="combinedSummaryHtml" type="java.lang.String" -->
<#-- @ftlvariable name="taskListUrl" type="java.lang.String" -->
<#-- @ftlvariable name="submitUrl" type="java.lang.String" -->
<#-- @ftlvariable name="applicationReference" type="java.lang.String" -->
<#-- @ftlvariable name="openUpdateRequest" type="java.lang.Boolean" -->
<#-- @ftlvariable name="updateRequestView" type="uk.co.ogauthority.pwa.model.view.appprocessing.applicationupdates.ApplicationUpdateRequestView" -->
<#-- @ftlvariable name="submitterCandidates" type="java.util.Map<java.lang.Integer, java.lang.String>" -->
<#-- @ftlvariable name="userPermissions" type="java.util.Set<uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission>" -->

<#assign pageHeading="Review and Submit Application ${applicationReference}"/>

<@defaultPagePane htmlTitle=pageHeading phaseBanner=false>

    <#if updateRequestView?has_content>
        <#assign updateRequestBanner>
            <@pwaUpdateRequestView.banner view=updateRequestView canUpdate=false taskListUrl=taskListUrl />
        </#assign>
    </#if>

    <@pwaAppSummary.summary
    pageHeading=pageHeading
    appSummaryView=appSummaryView
    sidebarHeading="Check your answers for all questions in the application"
    errorList=errorList
    aboveSummaryInsert=updateRequestBanner!"">

        <@fdsForm.htmlForm actionUrl=springUrl(submitUrl)>
          <!-- Submit button macro not used to allow for hiding of button when application is not valid. -->

            <#if openUpdateRequest && userPermissions?seq_contains("EDIT")>

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

            <#if !userPermissions?seq_contains("SUBMIT")>

                <h2 class="govuk-heading-l">Application submission</h2>

                <@fdsSearchSelector.searchSelectorEnhanced
                    path="form.submitterPersonId"
                    options=submitterCandidates
                    labelText="Select person to submit the application to the OGA"
                    hintText="Only people with submission permissions in the holder organisation are allowed to submit this application to the OGA"
                    formGroupClass="govuk-!-width-two-thirds"/>

            </#if>

            <@fdsAction.button buttonText=submitterCandidates?has_content?then("Send to submitter", "Submit") buttonValue="submit" />
            <@fdsAction.link linkText="Back to task list" linkClass="govuk-link govuk-link--button" linkUrl=springUrl(taskListUrl)/>

        </@fdsForm.htmlForm>

    </@pwaAppSummary.summary>

</@defaultPagePane>