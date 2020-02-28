<#include '../../layout.ftl'>

<#-- @ftlvariable name="informationTasks" type="java.util.HashMap<String, String>" -->
<#-- @ftlvariable name="applicationTasks" type="java.util.HashMap<String, String>" -->

<@defaultPage htmlTitle="Pipeline Works Authorisation Submission" pageHeading="Submit a Category 2 variation for ${masterPwaReference}" breadcrumbs=true>

    <@fdsTaskList.taskList>
        <#assign sectionNumber = 1/>
        <#if informationTasks?has_content>
            <@fdsTaskList.taskListSection sectionNumber="${sectionNumber}" sectionHeadingText="PWA information">
                <#list informationTasks as task>
                    <@fdsTaskList.taskListItem itemUrl=springUrl(task.route) itemText=task.taskName completed=task.completed/>
                </#list>
            </@fdsTaskList.taskListSection>
            <#assign sectionNumber = sectionNumber + 1/>
        </#if>
        <@fdsTaskList.taskListSection sectionNumber="${sectionNumber}" sectionHeadingText="Prepare application">
            <#list applicationTasks as task>
                <@fdsTaskList.taskListItem itemUrl=springUrl(task.route) itemText=task.taskName completed=task.completed/>
            </#list>
            <#assign sectionNumber = sectionNumber + 1/>
        </@fdsTaskList.taskListSection>

        <@fdsTaskList.taskListSection sectionNumber="${sectionNumber}" sectionHeadingText="Submit application">
            <@fdsWarning.warning>
              In order to submit this application, all sections above must have a COMPLETED label.
            </@fdsWarning.warning>
            <@fdsTaskList.taskListItem itemUrl=springUrl("/") itemText="Submit application"/>
        </@fdsTaskList.taskListSection>

    </@fdsTaskList.taskList>

</@defaultPage>