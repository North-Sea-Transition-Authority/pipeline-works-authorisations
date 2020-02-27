<#include '../../layout.ftl'>

<#-- @ftlvariable name="availableTasks" type="java.util.HashMap<String, String>" -->

<@defaultPage htmlTitle="Pipeline Works Authorisation Submission" pageHeading="Submit a Pipeline Works Authorisation" breadcrumbs=true>

    <@fdsTaskList.taskList>

        <@fdsTaskList.taskListSection sectionNumber="1" sectionHeadingText="Prepare application">
            <#list availableTasks as task>
                <@fdsTaskList.taskListItem itemUrl=springUrl(task.taskRoute) itemText=task.taskName completed=task.completed/>
            </#list>
        </@fdsTaskList.taskListSection>

        <@fdsTaskList.taskListSection sectionNumber="2" sectionHeadingText="Submit application">
            <@fdsWarning.warning>
              In order to submit this application, all sections above must have a COMPLETED label.
            </@fdsWarning.warning>
            <@fdsTaskList.taskListItem itemUrl=springUrl("/") itemText="Submit application"/>
        </@fdsTaskList.taskListSection>

    </@fdsTaskList.taskList>

</@defaultPage>