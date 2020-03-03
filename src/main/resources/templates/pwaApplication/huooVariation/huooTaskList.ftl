<#include '../../layout.ftl'>

<#-- @ftlvariable name="informationTasks" type="java.util.HashMap<String, String>" -->
<#-- @ftlvariable name="applicationTasks" type="java.util.HashMap<String, String>" -->

<@defaultPage htmlTitle="Pipeline Works Authorisation Submission" pageHeading="Submit a Holder, User, Operator, Owner Variation for ${masterPwaReference}" breadcrumbs=true>

    <@fdsTaskList.taskList>

        <@fdsTaskList.taskListSection sectionNumber="1" sectionHeadingText="PWA information">
            <#list informationTasks as task>
                <@fdsTaskList.taskListItem itemUrl=springUrl(task.route) itemText=task.taskName completed=task.completed/>
            </#list>
        </@fdsTaskList.taskListSection>
        <@fdsTaskList.taskListSection sectionNumber="2" sectionHeadingText="Prepare application">
            <#list applicationTasks as task>
                <@fdsTaskList.taskListItem itemUrl=springUrl(task.route) itemText=task.taskName completed=task.completed/>
            </#list>
        </@fdsTaskList.taskListSection>

        <@fdsTaskList.taskListSection sectionNumber="3" sectionHeadingText="Submit application">
            <@fdsWarning.warning>
              In order to submit this application, all sections above must have a COMPLETED label.
            </@fdsWarning.warning>
            <@fdsTaskList.taskListItem itemUrl=springUrl("/") itemText="Submit application"/>
        </@fdsTaskList.taskListSection>

    </@fdsTaskList.taskList>

</@defaultPage>