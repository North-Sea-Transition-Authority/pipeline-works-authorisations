<#include '../../../layout.ftl'>
<#include 'fluidCompositionQuestion.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>"-->
<#-- @ftlvariable name="backUrl" type=" java.lang.String"-->
<#-- @ftlvariable name="chemicals" type="java.util.List<Chemicals>" -->
<#-- @ftlvariable name="resourceType" type="uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType" -->

<@defaultPage htmlTitle="Fluid composition" pageHeading="What is the fluid composition of the products to be conveyed?" breadcrumbs=true errorItems=errorList>

    <@fdsForm.htmlForm>

        <#list chemicals as chemical>
            <@fluidCompositionQuestion chemical resourceType/>
        </#list>

        <@fdsTextarea.textarea
            path="form.otherInformation"
            labelText="Other information or components"
            hintText="Indicate here if a different measurement method is used for above components"
            characterCount=true maxCharacterLength="1000"
            optionalLabel=true/>

        <@fdsAction.submitButtons primaryButtonText=submitPrimaryButtonText secondaryButtonText=submitSecondaryButtonText/>

    </@fdsForm.htmlForm>

</@defaultPage>
