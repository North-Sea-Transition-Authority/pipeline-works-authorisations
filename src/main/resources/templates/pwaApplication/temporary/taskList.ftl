<#include '../../layout.ftl'>

<#-- @ftlvariable name="availableTasks" type="java.util.HashMap<String, String>" -->

<@defaultPage htmlTitle="Pipeline Works Authorisation Submission" pageHeading="Submit a Pipeline Works Authorisation" twoThirdsColumn=true>

    <@fdsForm.htmlForm>
        <@fdsTaskList.taskList>

            <@fdsTaskList.taskListSection sectionNumber="1" sectionHeadingText="Prepare application">
                <#list availableTasks as task, link>
                    <@fdsTaskList.taskListItem itemUrl=springUrl(link) itemText=task/>
                </#list>
            </@fdsTaskList.taskListSection>

            <@fdsTaskList.taskListSection sectionNumber="2" sectionHeadingText="Submit application">
                <@fdsWarning.warning >
                  <span>In order to submit this application, all sections above must have a COMPLETED label.</span>
                </@fdsWarning.warning>
                <@fdsTaskList.taskListItem itemUrl=springUrl("/") itemText="Submit application"/>
            </@fdsTaskList.taskListSection>

        </@fdsTaskList.taskList>
    </@fdsForm.htmlForm>

</@defaultPage>