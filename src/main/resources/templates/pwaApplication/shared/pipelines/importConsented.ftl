<#include '../../../layout.ftl'>
<#import '../../../components/coordinates/coordinateInput.ftl' as coordinateInput/>

<#-- @ftlvariable name="longDirections" type="java.util.Map<java.lang.String,java.lang.String>" -->
<#-- @ftlvariable name="form" type="uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PipelineIdentForm" -->
<#-- @ftlvariable name="cancelUrl" type="String" -->
<#-- @ftlvariable name="screenActionType" type="uk.co.ogauthority.pwa.model.form.enums.ScreenActionType" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="coreType" type="uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineCoreType" -->
<@defaultPage htmlTitle="Modify consented pipeline" pageHeading="Modify consented pipeline" breadcrumbs=true errorItems=errorList>

    <@fdsForm.htmlForm>

        <@fdsSearchSelector.searchSelectorEnhanced path="form.pipelineId" options=consentedPipelines labelText="Which pipeline is being modified?" hintText="You can only modify pipelines on the PWA this variation is for"/>

        <@fdsRadio.radioGroup path="form.pipelineStatus" labelText="What will the status of the pipeline be after your changes?" hiddenContent=true>
          <#list serviceStatuses as option>
            <@fdsRadio.radioItem path="form.pipelineStatus" itemMap={option: option.displayText}>

              <#if option == "OUT_OF_USE_ON_SEABED">
                <@fdsTextarea.textarea path="form.outOfUseStatusReason" labelText="Why is the pipeline not being returned to shore?" nestingPath="form.pipelineStatus" characterCount=true maxCharacterLength=maxCharacterLength?c/>

              <#elseif option == "TRANSFERRED">
                <@fdsCheckbox.checkboxGroup path="form.transferAgreed" nestingPath="form.pipelineStatus">
                    <@fdsCheckbox.checkboxItem path="form.transferAgreed" labelText="The NSTA consents and authorisations manager has agreed that this pipeline can be transferred to another PWA" />
                    <@fdsTextarea.textarea path="form.transferStatusReason" labelText="Why is the pipeline being transferred to another PWA?" nestingPath="form.pipelineStatus" characterCount=true maxCharacterLength=maxCharacterLength?c/>
                </@fdsCheckbox.checkboxGroup>
              </#if>

            </@fdsRadio.radioItem>
          </#list>
        </@fdsRadio.radioGroup>

        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Modify consented pipeline" secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>

    </@fdsForm.htmlForm>
</@defaultPage>
