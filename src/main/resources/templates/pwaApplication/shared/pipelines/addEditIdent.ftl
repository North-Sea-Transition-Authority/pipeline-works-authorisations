<#include '../../../layout.ftl'>
<#import '../../../components/coordinates/coordinateInput.ftl' as coordinateInput/>

<#-- @ftlvariable name="longDirections" type="java.util.Map<java.lang.String,java.lang.String>" -->
<#-- @ftlvariable name="form" type="uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PipelineIdentForm" -->
<#-- @ftlvariable name="cancelUrl" type="String" -->
<#-- @ftlvariable name="screenActionType" type="uk.co.ogauthority.pwa.model.form.enums.ScreenActionType" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="coreType" type="uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineCoreType" -->

<@defaultPage htmlTitle="${screenActionType.actionText} ident" pageHeading="${screenActionType.actionText} ident" breadcrumbs=true errorItems=errorList>

    <#assign coordinateGuidance>
        <@fdsDetails.details detailsTitle="When should I provide ident coordinates?" detailsText="Provide coordinates if this is a key point along the pipeline route or a cut point." />
    </#assign>

    <@fdsForm.htmlForm>

        <@fdsFieldset.fieldset legendHeading="Where does the ident start?" legendHeadingSize="h2" legendHeadingClass="govuk-fieldset__legend--l">

            <@fdsTextInput.textInput path="form.fromLocation" labelText="Structure" />

            ${coordinateGuidance}

            <@coordinateInput.latitudeInput degreesLocationPath="form.fromCoordinateForm.latitudeDegrees"
                                    minutesLocationPath="form.fromCoordinateForm.latitudeMinutes"
                                    secondsLocationPath="form.fromCoordinateForm.latitudeSeconds"
                                    optionalLabel="true"
                                    formId="fromLatitude"
                                    labelText="Start point latitude"/>

            <@coordinateInput.longitudeInput degreesLocationPath="form.fromCoordinateForm.longitudeDegrees"
                                    minutesLocationPath="form.fromCoordinateForm.longitudeMinutes"
                                    secondsLocationPath="form.fromCoordinateForm.longitudeSeconds"
                                    optionalLabel="true"
                                    direction="EW"
                                    directionPath="form.fromCoordinateForm.longitudeDirection"
                                    directionList=longDirections
                                    formId="fromLongitude"
                                    labelText="Start point longitude"/>

        </@fdsFieldset.fieldset>

        <@fdsFieldset.fieldset legendHeading="Where does the ident finish?" legendHeadingSize="h2" legendHeadingClass="govuk-fieldset__legend--l">

            <@fdsTextInput.textInput path="form.toLocation" labelText="Structure" />

            ${coordinateGuidance}

            <@coordinateInput.latitudeInput degreesLocationPath="form.toCoordinateForm.latitudeDegrees"
                                    minutesLocationPath="form.toCoordinateForm.latitudeMinutes"
                                    secondsLocationPath="form.toCoordinateForm.latitudeSeconds"
                                    optionalLabel="true"
                                    formId="toLatitude"
                                    labelText="Finish point latitude"/>

            <@coordinateInput.longitudeInput degreesLocationPath="form.toCoordinateForm.longitudeDegrees"
                                    minutesLocationPath="form.toCoordinateForm.longitudeMinutes"
                                    secondsLocationPath="form.toCoordinateForm.longitudeSeconds"
                                    optionalLabel="true"
                                    direction="EW"
                                    directionPath="form.toCoordinateForm.longitudeDirection"
                                    directionList=longDirections
                                    formId="toLongitude"
                                    labelText="Finish point longitude"/>

        </@fdsFieldset.fieldset>

        <@fdsFieldset.fieldset legendHeading="Ident information" legendHeadingSize="h2" legendHeadingClass="govuk-fieldset__legend--l">

            <@fdsTextarea.textarea path="form.dataForm.componentPartsDescription" labelText="Description of component part" nestingPath="form.definingStructure"
             hintText="e.g. 10\" production flowline, electrical lead d B, 2 x 6\" Production Jumper within a Wellhead Bundle, 6\" flexible gas lift flowline, control umbilical etc. Note: If this ident is in a bundle include the text “(within bundle)\"" characterCount=true maxCharacterLength=maxCharacterLength?c/>
            <@identDataTextInput coreType=coreType textInputPath="form.dataForm.productsToBeConveyed" textAreaPath="form.dataForm.productsToBeConveyedMultiCore" useTextArea=true labelText="Products to be conveyed" nestingPath="form.definingStructure"/>

            <@fdsRadio.radioGroup path="form.definingStructure" labelText="Is this ident defining a structure?" hiddenContent=true>
                <@fdsRadio.radioYes path="form.definingStructure">
                    <@fdsTextInput.textInput path="form.lengthOptional.value" labelText="Length" suffix="m" inputClass="govuk-input--width-5" optionalLabel=true nestingPath="form.definingStructure"/>
                </@fdsRadio.radioYes>

                <@fdsRadio.radioNo path="form.definingStructure">
                    <@fdsTextInput.textInput path="form.length.value" labelText="Length" suffix="m" inputClass="govuk-input--width-5" nestingPath="form.definingStructure"/>
                    <@identDataTextInput coreType=coreType textInputPath="form.dataForm.externalDiameter.value" textAreaPath="form.dataForm.externalDiameterMultiCore" labelText="External diameter" suffix="mm" suffixScreenReaderPrompt="mm" nestingPath="form.definingStructure"/>
                    <@identDataTextInput coreType=coreType textInputPath="form.dataForm.internalDiameter.value" textAreaPath="form.dataForm.internalDiameterMultiCore" nestingPath="form.definingStructure"
                     labelText="Internal diameter" suffix="mm" suffixScreenReaderPrompt="mm" multiCoreHintText="Provide for each of the cores" nestingPath="form.definingStructure"/>
                    <@identDataTextInput coreType=coreType textInputPath="form.dataForm.wallThickness.value" textAreaPath="form.dataForm.wallThicknessMultiCore" nestingPath="form.definingStructure"
                     labelText="Wall thickness" suffix="mm" suffixScreenReaderPrompt="mm" multiCoreHintText="Provide for the outer casing not the internal casings"/>
                    <@identDataTextInput coreType=coreType textInputPath="form.dataForm.maop.value" textAreaPath="form.dataForm.maopMultiCore" labelText="MAOP" suffix="barg" nestingPath="form.definingStructure" suffixScreenReaderPrompt="barg" multiCoreHintText="Provide for each of the internal cores"/>
                    <@identDataTextInput coreType=coreType textInputPath="form.dataForm.insulationCoatingType" textAreaPath="form.dataForm.insulationCoatingTypeMultiCore" nestingPath="form.definingStructure" useTextArea=true labelText="Insulation / coating type" multiCoreHintText="Provide for the outer casing"/>
                </@fdsRadio.radioNo>
            </@fdsRadio.radioGroup>

        </@fdsFieldset.fieldset>

        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="${screenActionType.submitButtonText} ident" secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>

    </@fdsForm.htmlForm>
</@defaultPage>


<#macro
  identDataTextInput
  coreType
  labelText
  textInputPath
  textAreaPath
  suffix=""
  suffixScreenReaderPrompt=""
  useTextArea=false
  multiCoreHintText=""
  nestingPath="">
    <#if coreType == "SINGLE_CORE">
        <#if useTextArea>
            <@fdsTextarea.textarea path=textInputPath labelText="${labelText} ${suffix}" maxCharacterLength=maxCharacterLength?c characterCount=true nestingPath=nestingPath/>
        <#else>
            <@fdsTextInput.textInput path=textInputPath labelText=labelText inputClass="govuk-input--width-5" suffix=suffix suffixScreenReaderPrompt=suffixScreenReaderPrompt nestingPath=nestingPath/>
        </#if>
    <#else>
        <#assign unit = ""/>
        <#if suffix?has_content>
            <#assign unit = "(" + suffix + ")"/>
        </#if>
        <@fdsTextarea.textarea path=textAreaPath labelText="${labelText} ${unit}" maxCharacterLength=maxCharacterLength?c characterCount=true hintText=multiCoreHintText nestingPath=nestingPath/>
    </#if>
</#macro>

