<#include '../../layout.ftl'>

<#-- @ftlvariable name="pwaInformationTasks" type="java.util.HashMap<String, String>" -->
<#-- @ftlvariable name="applicationTasks" type="java.util.HashMap<String, String>" -->

<@defaultPage htmlTitle="Pipeline Works Authorisation Submission" pageHeading="Submit a Pipeline Works Authorisation" breadcrumbs=true>

    <@fdsTaskList.taskList>

        <@fdsTaskList.taskListSection sectionNumber="1" sectionHeadingText="PWA information">
            <#list pwaInformationTasks as task, link>
                <@fdsTaskList.taskListItem itemUrl=springUrl(link) itemText=task/>
            </#list>
        </@fdsTaskList.taskListSection>
        <@fdsTaskList.taskListSection sectionNumber="2" sectionHeadingText="Prepare application">
            <#list applicationTasks as task, link>
                <@fdsTaskList.taskListItem itemUrl=springUrl(link) itemText=task/>
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