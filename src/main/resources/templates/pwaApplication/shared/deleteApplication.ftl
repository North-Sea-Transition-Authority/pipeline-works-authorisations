<#include '../../layout.ftl'>

<#-- @ftlvariable name="appRef" type="java.lang.String" -->
<#-- @ftlvariable name="taskListUrl" type="java.lang.String" -->

<@defaultPage htmlTitle="Delete application" pageHeading="Are you sure you want to delete application ${appRef}" breadcrumbs=true>



    <@fdsForm.htmlForm>

        <@fdsInsetText.insetText>
            Once an application is deleted there is no way to restore it.
        </@fdsInsetText.insetText>



        <@fdsAction.submitButtons primaryButtonText="Delete" linkSecondaryAction=true secondaryLinkText="Back to task list" linkSecondaryActionUrl=springUrl(taskListUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>