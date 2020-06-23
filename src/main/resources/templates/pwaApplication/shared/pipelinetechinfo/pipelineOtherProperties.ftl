<#include '../../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>"-->



<@defaultPage htmlTitle="Other properties" pageHeading="What other properties affecting pipeline design are available/present?" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>


    <@fdsForm.htmlForm>
       
        


        <@fdsAction.submitButtons primaryButtonText="Complete" secondaryButtonText="Save and complete later"/>
    </@fdsForm.htmlForm>

</@defaultPage>
