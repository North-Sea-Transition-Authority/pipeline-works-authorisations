<#include '../../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>"-->
<#-- @ftlvariable name="backUrl" type=" java.lang.String"-->

<@defaultPage htmlTitle="General technical details" pageHeading="General technical details" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>

    <@fdsForm.htmlForm>
        <@fdsTextInput.textInput path="form.estimatedFieldLife" labelText="What is the estimated life of the field?" inputClass="govuk-input--width-5"/>

        <@fdsRadio.radioGroup path="form.pipelineDesignedToStandards" labelText="Have the pipeline systems been designed in accordance with industry recognised codes and standards?" 
            hintText="For example, PD 8010 n2004 Part 2 Subsea Pipelines" hiddenContent=true>  
            <@fdsRadio.radioYes path="form.pipelineDesignedToStandards">      
                <@fdsTextarea.textarea path="form.pipelineStandardsDescription" nestingPath="form.pipelineDesignedToStandards" labelText="Provide the design codes/standards for the pipelines system" maxCharacterLength="4000"/>                     
            </@fdsRadio.radioYes>                      
            <@fdsRadio.radioNo path="form.pipelineDesignedToStandards"/>                      
        </@fdsRadio.radioGroup>
        
        <@fdsTextarea.textarea path="form.corrosionDescription" labelText="Provide a brief description of the corrosion management strategy" maxCharacterLength="4000"/>     

        <@fdsRadio.radioGroup path="form.plannedPipelineTieInPoints" labelText="Will there be any future tie-in points for the planned pipelines and umbilicals?" hiddenContent=true>  
            <@fdsRadio.radioYes path="form.plannedPipelineTieInPoints">      
                <@fdsTextarea.textarea path="form.tieInPointsDescription" nestingPath="form.plannedPipelineTieInPoints" labelText="Provide a description of tie-in points" 
                    hintText="Include the geographical location of each tie-in point in WGS84 format" maxCharacterLength="4000"/>                     
            </@fdsRadio.radioYes>                      
            <@fdsRadio.radioNo path="form.plannedPipelineTieInPoints"/>                      
        </@fdsRadio.radioGroup>  
        


        <@fdsAction.submitButtons primaryButtonText="Complete" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>
