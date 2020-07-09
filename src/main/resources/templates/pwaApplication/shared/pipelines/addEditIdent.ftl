<#include '../../../layout.ftl'>
<#import '../../../components/coordinates/coordinateInput.ftl' as coordinateInput/>

<#-- @ftlvariable name="longDirections" type="java.util.Map<java.lang.String,java.lang.String>" -->
<#-- @ftlvariable name="form" type="uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentForm" -->
<#-- @ftlvariable name="cancelUrl" type="String" -->
<#-- @ftlvariable name="screenActionType" type="uk.co.ogauthority.pwa.model.form.enums.ScreenActionType" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="coreType" type="uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineCoreType" -->

<@defaultPage htmlTitle="${screenActionType.actionText} ident" pageHeading="${screenActionType.actionText} ident" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList />
    </#if>

    <#assign coordinateGuidance>
        <@fdsDetails.details detailsTitle="When should I provide ident coordinates?" detailsText="Provide coordinates if this is a key point along the pipeline route." />
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

            <@fdsTextInput.textInput path="form.length" labelText="Length (m)" inputClass="govuk-input--width-5"/>

            <@fdsTextarea.textarea path="form.dataForm.componentPartsDescription" labelText="Description of component parts" hintText="Some guidance text here."/>
            <@identDataTextInput coreType=coreType textInputPath="form.dataForm.externalDiameter" textAreaPath="form.dataForm.externalDiameterMultiCore" labelText="External diameter" suffix="mm" suffixScreenReaderPrompt="mm"/>
            <@identDataTextInput coreType=coreType textInputPath="form.dataForm.internalDiameter" textAreaPath="form.dataForm.internalDiameterMultiCore" labelText="Internal diameter" suffix="mm" suffixScreenReaderPrompt="mm"/>
            <@identDataTextInput coreType=coreType textInputPath="form.dataForm.wallThickness" textAreaPath="form.dataForm.wallThicknessMultiCore" labelText="Wall thickness" suffix="mm" suffixScreenReaderPrompt="mm"/>
            <@identDataTextInput coreType=coreType textInputPath="form.dataForm.maop" textAreaPath="form.dataForm.maopMultiCore" labelText="MAOP" suffix="barg" suffixScreenReaderPrompt="barg"/>
            <@identDataTextInput coreType=coreType textInputPath="form.dataForm.insulationCoatingType" textAreaPath="form.dataForm.insulationCoatingTypeMultiCore" labelText="Insulation / coating type"/>
            <@identDataTextInput coreType=coreType textInputPath="form.dataForm.productsToBeConveyed" textAreaPath="form.dataForm.productsToBeConveyedMultiCore" labelText="Products to be conveyed"/>

        </@fdsFieldset.fieldset>

        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="${screenActionType.submitButtonText} ident" secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>

    </@fdsForm.htmlForm>
</@defaultPage>


<#macro identDataTextInput coreType labelText textInputPath textAreaPath suffix="" suffixScreenReaderPrompt="">
    <#if coreType == "SINGLE_CORE">
        <@fdsTextInput.textInput path=textInputPath labelText=labelText inputClass="govuk-input--width-5" suffix=suffix suffixScreenReaderPrompt=suffixScreenReaderPrompt/>
    <#else>
        <#assign unit = ""/>
        <#if suffix?has_content>
            <#assign unit = "(" + suffix + ")"/>
        </#if>
        <@fdsTextarea.textarea path=textAreaPath labelText="${labelText} ${unit}" maxCharacterLength="4000" characterCount=true/>
    </#if>
</#macro>

