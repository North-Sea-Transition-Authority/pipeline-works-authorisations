<#include '../../../layout.ftl'>

<#-- @ftlvariable name="applicationTaskGroups" type="java.util.List<uk.co.ogauthority.pwa.model.tasklist.TaskListGroup>" -->
<#-- @ftlvariable name="submissionTask" type="uk.co.ogauthority.pwa.model.tasklist.TaskListEntry" -->
<#-- @ftlvariable name="applicationType" type="java.lang.String" -->
<#-- @ftlvariable name="masterPwaReference" type="java.lang.String" -->

<#assign pageCaption=masterPwaReference?has_content?then("${masterPwaReference} ${applicationType} application", "${applicationType} application")  />

<@defaultPage htmlTitle="Pipeline Works Authorisation Submission" pageHeading="Submit a Pipeline Works Authorisation" caption=pageCaption breadcrumbs=true>

    <@fdsTaskList.taskList>

        <#list applicationTaskGroups as taskGroup>
            <@fdsTaskList.taskListSection sectionNumber="${taskGroup?index + 1}" sectionHeadingText=taskGroup.groupName>
                <#list taskGroup.taskListEntries as task>
                    <@pwaTaskListItem.taskInfoItem taskName=task.taskName taskInfoList=task.taskInfoList route=task.route isCompleted=task.completed/>
                </#list>
            </@fdsTaskList.taskListSection>
        </#list>

        <@fdsTaskList.taskListSection sectionNumber="${applicationTaskGroups?size + 1}" sectionHeadingText="Submit application">
            <@fdsWarning.warning>
              In order to submit this application, all sections above must have a COMPLETED label.
            </@fdsWarning.warning>
            <@fdsTaskList.taskListItem itemUrl=springUrl(submissionTask.route) itemText=submissionTask.taskName/>
        </@fdsTaskList.taskListSection>

    </@fdsTaskList.taskList>

</@defaultPage>