<#include '../../layout.ftl'>

<#-- @ftlvariable name="pwaInfoTasks" type="java.util.HashMap<String, String>" -->
<#-- @ftlvariable name="appInfoTasks" type="java.util.HashMap<String, uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskInfo>" -->
<#-- @ftlvariable name="prepareAppTasks" type="java.util.List<uk.co.ogauthority.pwa.model.tasklist.TaskListEntry>" -->
<#-- @ftlvariable name="submissionTask" type="uk.co.ogauthority.pwa.model.tasklist.TaskListEntry" -->
<#-- @ftlvariable name="masterPwaReference" type="String" -->

<@defaultPage htmlTitle="Pipeline Works Authorisation Submission" pageHeading="Submit a Decommissioning variation for ${masterPwaReference}" breadcrumbs=true>

    <@fdsTaskList.taskList>

        <@fdsTaskList.taskListSection sectionNumber="1" sectionHeadingText="PWA information">
            <#list pwaInfoTasks as task, link>
                <@fdsTaskList.taskListItem itemUrl=springUrl(link) itemText=task />
            </#list>
        </@fdsTaskList.taskListSection>
        <@fdsTaskList.taskListSection sectionNumber="2" sectionHeadingText="Application information">
            <#list appInfoTasks as task>
                <@pwaTaskListItem.taskInfoItem taskName=task.taskName taskInfoList=task.taskInfoList route=task.route/>
            </#list>
        </@fdsTaskList.taskListSection>
        <@fdsTaskList.taskListSection sectionNumber="3" sectionHeadingText="Prepare application">
            <#list prepareAppTasks as task>
                <@fdsTaskList.taskListItem itemUrl=springUrl(task.route) itemText=task.taskName completed=task.completed />
            </#list>
        </@fdsTaskList.taskListSection>

        <@fdsTaskList.taskListSection sectionNumber="4" sectionHeadingText="Submit application">
            <@fdsWarning.warning>
              In order to submit this application, all sections above must have a COMPLETED label.
            </@fdsWarning.warning>
            <@fdsTaskList.taskListItem itemUrl=springUrl(submissionTask.route) itemText=submissionTask.taskName/>
        </@fdsTaskList.taskListSection>

    </@fdsTaskList.taskList>

</@defaultPage>