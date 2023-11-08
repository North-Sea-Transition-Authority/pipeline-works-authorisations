<#include '../../../layout.ftl'>
<#include 'minMaxInput.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>"-->
<#-- @ftlvariable name="showFieldLife" type="java.lang.Boolean"-->
<#-- @ftlvariable name="backUrl" type=" java.lang.String"-->

<@defaultPage htmlTitle="General technical details" pageHeading="General technical details" breadcrumbs=true errorItems=errorList>

    <@fdsForm.htmlForm>
        <#if resourceType == "PETROLEUM">
          <@fdsTextInput.textInput path="form.estimatedAssetLife" labelText="What is the estimated life of the field?" suffix="years" inputClass="govuk-input--width-5"/>
        <#elseif resourceType == "CCUS">
          <@fdsTextInput.textInput path="form.estimatedAssetLife" labelText="What is the estimated life of the storage site?" suffix="years" inputClass="govuk-input--width-5"/>
        </#if>
        <@fdsRadio.radioGroup path="form.pipelineDesignedToStandards" labelText="Has the pipeline or pipeline system been designed in accordance with industry recognised codes and standards?"
            hintText="For example, PD 8010 n2004 Part 2 Subsea Pipelines" hiddenContent=true>
            <@fdsRadio.radioYes path="form.pipelineDesignedToStandards">
                <@fdsTextarea.textarea path="form.pipelineStandardsDescription" nestingPath="form.pipelineDesignedToStandards" labelText="Provide the design codes/standards for the pipeline or pipeline system" characterCount=true maxCharacterLength=maxCharacterLength?c/>
            </@fdsRadio.radioYes>
            <@fdsRadio.radioNo path="form.pipelineDesignedToStandards"/>
        </@fdsRadio.radioGroup>

        <@fdsTextarea.textarea path="form.corrosionDescription" labelText="Provide a brief description of the corrosion management strategy" characterCount=true maxCharacterLength=maxCharacterLength?c/>

        <@fdsRadio.radioGroup path="form.plannedPipelineTieInPoints" labelText="Will there be any future tie-in points for the planned pipelines and umbilicals?" hiddenContent=true>
            <@fdsRadio.radioYes path="form.plannedPipelineTieInPoints">
                <@fdsTextarea.textarea path="form.tieInPointsDescription" nestingPath="form.plannedPipelineTieInPoints" labelText="Provide a description of tie-in points"
                    hintText="Include the geographical location of each tie-in point in WGS84 format" characterCount=true maxCharacterLength=maxCharacterLength?c/>
            </@fdsRadio.radioYes>
            <@fdsRadio.radioNo path="form.plannedPipelineTieInPoints"/>
        </@fdsRadio.radioGroup>

        <@fdsAction.submitButtons primaryButtonText=submitPrimaryButtonText secondaryButtonText=submitSecondaryButtonText/>

    </@fdsForm.htmlForm>

</@defaultPage>
