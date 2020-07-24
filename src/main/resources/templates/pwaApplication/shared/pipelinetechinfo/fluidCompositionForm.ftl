<#include '../../../layout.ftl'>
<#include 'fluidCompositionQuestion.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>"-->
<#-- @ftlvariable name="backUrl" type=" java.lang.String"-->
<#-- @ftlvariable name="chemicals" type="java.util.List<Chemicals>" -->
<#-- @ftlvariable name="fluidCompositionOptions" type="java.util.List<FluidCompositionOptions>" -->

<@defaultPage htmlTitle="Fluid composition" pageHeading="What is the fluid composition of the products to be conveyed?" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>

    <@fdsForm.htmlForm>

        <#list chemicals as chemical>
            <@fluidCompositionQuestion chemical fluidCompositionOptions/>
        </#list>

        <@fdsAction.submitButtons primaryButtonText=submitPrimaryButtonText secondaryButtonText=submitSecondaryButtonText/>

    </@fdsForm.htmlForm>

</@defaultPage>
