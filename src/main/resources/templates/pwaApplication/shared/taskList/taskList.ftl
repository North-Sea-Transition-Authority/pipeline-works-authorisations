<#include '../../../layout.ftl'>

<#-- @ftlvariable name="applicationTaskGroups" type="java.util.List<uk.co.ogauthority.pwa.model.tasklist.TaskListGroup>" -->
<#-- @ftlvariable name="submissionTask" type="uk.co.ogauthority.pwa.model.tasklist.TaskListEntry" -->
<#-- @ftlvariable name="applicationType" type="java.lang.String" -->
<#-- @ftlvariable name="masterPwaReference" type="java.lang.String" -->
<#-- @ftlvariable name="updateRequestView" type="uk.co.ogauthority.pwa.model.view.appprocessing.applicationupdates.ApplicationUpdateRequestView" -->
<#-- @ftlvariable name="optionsApprovalPageBanner" type="uk.co.ogauthority.pwa.model.view.banner.PageBannerView" -->
<#-- @ftlvariable name="canShowDeleteAppButton" type="java.lang.Boolean" -->
<#-- @ftlvariable name="deleteAppUrl" type="java.lang.String" -->


<#assign pageCaption=masterPwaReference?has_content?then("${masterPwaReference} ${applicationType} application", "${applicationType} application")  />

<@defaultPage htmlTitle="Pipeline Works Authorisation Submission" pageHeading="Submit a Pipeline Works Authorisation" caption=pageCaption breadcrumbs=true>

    <#if updateRequestView?has_content>
      <@pwaUpdateRequestView.banner view=updateRequestView />
    </#if>

    <#if optionsApprovalPageBanner?has_content>
        <@pageBanner.banner view=optionsApprovalPageBanner />
    </#if>

    <@fdsTaskList.taskList>

        <#list applicationTaskGroups as taskGroup>
            <@fdsTaskList.taskListSection sectionNumber="${taskGroup?index + 1}" sectionHeadingText=taskGroup.groupName>
                <#list taskGroup.taskListEntries as task>
                    <@pwaTaskListItem.taskInfoItem taskName=task.taskName taskInfoList=task.taskInfoList route=task.route isCompleted=task.completed/>
                </#list>
            </@fdsTaskList.taskListSection>
        </#list>

        <@fdsTaskList.taskListSection sectionNumber="${applicationTaskGroups?size + 1}" sectionHeadingText="Submit application" warningText="In order to submit this application, all sections above must have a COMPLETED label.">
            <@fdsTaskList.taskListItem itemUrl=springUrl(submissionTask.route) itemText=submissionTask.taskName/>
        </@fdsTaskList.taskListSection>

        <#if canShowDeleteAppButton>
            <@fdsAction.link linkText="Delete application" linkUrl=springUrl(deleteAppUrl) linkClass="govuk-button govuk-button--warning" role=true/>
        </#if>

    </@fdsTaskList.taskList>

</@defaultPage>