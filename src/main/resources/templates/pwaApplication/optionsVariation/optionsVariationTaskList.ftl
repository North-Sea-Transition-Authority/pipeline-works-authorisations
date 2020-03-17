<#include '../../layout.ftl'>

<#-- @ftlvariable name="pwaInfoTasks" type="java.util.HashMap<String, String>" -->
<#-- @ftlvariable name="appInfoTasks" type="java.util.HashMap<String, String>" -->
<#-- @ftlvariable name="prepareAppTasks" type="java.util.HashMap<String, String>" -->
<#-- @ftlvariable name="masterPwaReference" type="String" -->

<@defaultPage htmlTitle="Pipeline Works Authorisation Submission" pageHeading="Submit an Options variation for ${masterPwaReference}" breadcrumbs=true>

    <@fdsTaskList.taskList>

        <@fdsTaskList.taskListSection sectionNumber="1" sectionHeadingText="PWA information">
            <#list pwaInfoTasks as task, link>
                <@fdsTaskList.taskListItem itemUrl=springUrl(link) itemText=task />
            </#list>
        </@fdsTaskList.taskListSection>
        <@fdsTaskList.taskListSection sectionNumber="2" sectionHeadingText="Application information">
            <#list appInfoTasks as task, link>
                <@fdsTaskList.taskListItem itemUrl=springUrl(link) itemText=task />
            </#list>
        </@fdsTaskList.taskListSection>
        <@fdsTaskList.taskListSection sectionNumber="3" sectionHeadingText="Prepare application">
            <#list prepareAppTasks as task, link>
                <@fdsTaskList.taskListItem itemUrl=springUrl(link) itemText=task />
            </#list>
        </@fdsTaskList.taskListSection>

        <@fdsTaskList.taskListSection sectionNumber="4" sectionHeadingText="Submit application">
            <@fdsWarning.warning>
              In order to submit this application, all sections above must have a COMPLETED label.
            </@fdsWarning.warning>
            <@fdsTaskList.taskListItem itemUrl=springUrl("/") itemText="Submit application"/>
        </@fdsTaskList.taskListSection>

    </@fdsTaskList.taskList>

</@defaultPage>