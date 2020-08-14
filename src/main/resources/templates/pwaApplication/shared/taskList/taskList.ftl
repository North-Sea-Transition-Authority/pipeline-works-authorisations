<#include '../../../layout.ftl'>

<#-- @ftlvariable name="applicationTasks" type="java.util.List<uk.co.ogauthority.pwa.model.tasklist.TaskListEntry>" -->
<#-- @ftlvariable name="submissionTask" type="uk.co.ogauthority.pwa.model.tasklist.TaskListEntry" -->


<@defaultPage htmlTitle="Pipeline Works Authorisation Submission" pageHeading="Submit a Pipeline Works Authorisation" breadcrumbs=true>

    <@fdsTaskList.taskList>

        <@fdsTaskList.taskListSection sectionNumber="1" sectionHeadingText="Prepare application">
            <#list applicationTasks as task>
                <@pwaTaskListItem.taskInfoItem taskName=task.taskName taskInfoList=task.taskInfoList route=task.route isCompleted=task.completed/>
            </#list>
        </@fdsTaskList.taskListSection>

        <@fdsTaskList.taskListSection sectionNumber="2" sectionHeadingText="Submit application">
            <@fdsWarning.warning>
              In order to submit this application, all sections above must have a COMPLETED label.
            </@fdsWarning.warning>
            <@fdsTaskList.taskListItem itemUrl=springUrl(submissionTask.route) itemText=submissionTask.taskName/>
        </@fdsTaskList.taskListSection>

    </@fdsTaskList.taskList>

</@defaultPage>