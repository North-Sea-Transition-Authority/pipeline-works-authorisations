<#include '../../../layout.ftl'>

<#-- @ftlvariable name="pageHeading" type="java.lang.String" -->
<#-- @ftlvariable name="backUrl" type="java.lang.String" -->
<#-- @ftlvariable name="markCompleteErrorMessage" type="java.lang.String" -->

<@defaultPage htmlTitle=pageHeading pageHeading=pageHeading breadcrumbs=true>


    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons
        errorMessage=markCompleteErrorMessage!""
        primaryButtonText="Complete"
        linkSecondaryAction=true
        secondaryLinkText="Back to task list"
        linkSecondaryActionUrl=springUrl(backUrl)
        />
    </@fdsForm.htmlForm>

</@defaultPage>