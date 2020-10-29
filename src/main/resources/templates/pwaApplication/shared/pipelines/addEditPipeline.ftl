<#include '../../../layout.ftl'>
<#import '../../../components/coordinates/coordinateInput.ftl' as coordinateInput/>

<#-- @ftlvariable name="pipelineTypes" type="java.util.Map<java.lang.String,java.lang.String>" -->
<#-- @ftlvariable name="longDirections" type="java.util.Map<java.lang.String,java.lang.String>" -->
<#-- @ftlvariable name="form" type="uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineHeaderForm" -->
<#-- @ftlvariable name="cancelUrl" type="String" -->
<#-- @ftlvariable name="screenActionType" type="uk.co.ogauthority.pwa.model.form.enums.ScreenActionType" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="pipelineNumber" type="String" -->
<#-- @ftlvariable name="questionsForPipelineStatus" type="java.util.Set<uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineHeaderConditionalQuestion>" -->

<@defaultPage htmlTitle="${screenActionType.actionText} ${pipelineNumber!} pipeline" pageHeading="${screenActionType.actionText} ${pipelineNumber!} pipeline" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList />
    </#if>

    <@fdsForm.htmlForm>

        <@fdsFieldset.fieldset legendHeading="Where does the pipeline start?" legendHeadingSize="h2" legendHeadingClass="govuk-fieldset__legend--l">

            <@fdsTextInput.textInput path="form.fromLocation" labelText="Structure" />

            <@coordinateInput.latitudeInput degreesLocationPath="form.fromCoordinateForm.latitudeDegrees"
            minutesLocationPath="form.fromCoordinateForm.latitudeMinutes"
            secondsLocationPath="form.fromCoordinateForm.latitudeSeconds"
            formId="fromLatitude"
            labelText="Start point latitude"/>

            <@coordinateInput.longitudeInput degreesLocationPath="form.fromCoordinateForm.longitudeDegrees"
            minutesLocationPath="form.fromCoordinateForm.longitudeMinutes"
            secondsLocationPath="form.fromCoordinateForm.longitudeSeconds"
            direction="EW"
            directionPath="form.fromCoordinateForm.longitudeDirection"
            directionList=longDirections
            formId="fromLongitude"
            labelText="Start point longitude"/>

        </@fdsFieldset.fieldset>

        <@fdsFieldset.fieldset legendHeading="Where does the pipeline finish?" legendHeadingSize="h2" legendHeadingClass="govuk-fieldset__legend--l">

            <@fdsTextInput.textInput path="form.toLocation" labelText="Structure" />

            <@coordinateInput.latitudeInput degreesLocationPath="form.toCoordinateForm.latitudeDegrees"
            minutesLocationPath="form.toCoordinateForm.latitudeMinutes"
            secondsLocationPath="form.toCoordinateForm.latitudeSeconds"
            formId="toLatitude"
            labelText="Finish point latitude"/>

            <@coordinateInput.longitudeInput degreesLocationPath="form.toCoordinateForm.longitudeDegrees"
            minutesLocationPath="form.toCoordinateForm.longitudeMinutes"
            secondsLocationPath="form.toCoordinateForm.longitudeSeconds"
            direction="EW"
            directionPath="form.toCoordinateForm.longitudeDirection"
            directionList=longDirections
            formId="toLongitude"
            labelText="Finish point longitude"/>

        </@fdsFieldset.fieldset>

        <@fdsFieldset.fieldset legendHeading="Pipeline information" legendHeadingSize="h2" legendHeadingClass="govuk-fieldset__legend--l">

            <@fdsSelect.select path="form.pipelineType" labelText="Pipeline type" options=pipelineTypes />

            <@fdsTextInput.textInput path="form.length" labelText="Length (m)" inputClass="govuk-input--width-5"/>

            <@fdsTextarea.textarea path="form.componentPartsDescription" labelText="Description of component parts of the pipeline" hintText="e.g. 10\" production flowline, electrical lead d B, 2 x 6\" Production Jumper within a Wellhead Bundle, 6\" flexible gas lift flowline, control umbilical etc" characterCount=true maxCharacterLength="4000"/>

            <@fdsTextarea.textarea path="form.productsToBeConveyed" labelText="Products to be conveyed" characterCount=true maxCharacterLength="4000"/>

            <@fdsRadio.radioGroup path="form.trenchedBuriedBackfilled" labelText="Will the pipeline be trenched and/or buried and/or backfilled?" hiddenContent=true>
                <@fdsRadio.radioYes path="form.trenchedBuriedBackfilled">
                    <@fdsTextarea.textarea path="form.trenchingMethods" labelText="Describe the methods to be deployed to execute the trenching and the target depth of trench" nestingPath="form.trenchedBuriedBackfilled" characterCount=true maxCharacterLength="4000"/>
                </@fdsRadio.radioYes>
                <@fdsRadio.radioNo path="form.trenchedBuriedBackfilled"/>
            </@fdsRadio.radioGroup>


            <@fdsRadio.radioGroup path="form.pipelineFlexibility" labelText="Is this a flexible or rigid pipeline?" hiddenContent=true>
                <#assign firstItem=true/>
                <#list pipelineFlexibilityTypes as  pipelineFlexibilityTypeOption>
                    <@fdsRadio.radioItem path="form.pipelineFlexibility" itemMap={pipelineFlexibilityTypeOption : pipelineFlexibilityTypeOption.getDisplayText()} isFirstItem=firstItem/>
                    <#assign firstItem=false/>
                </#list>
            </@fdsRadio.radioGroup>


            <@fdsRadio.radioGroup path="form.pipelineMaterial" labelText="What materials were used to construct the pipeline?" hiddenContent=true>
                <#assign firstItem=true/>
                <#list pipelineMaterialTypes as  pipelineMaterialTypeOption>
                    <@fdsRadio.radioItem path="form.pipelineMaterial" itemMap={pipelineMaterialTypeOption : pipelineMaterialTypeOption.getDisplayText()} isFirstItem=firstItem>
                        <#if pipelineMaterialTypeOption == "OTHER">
                            <@fdsTextarea.textarea path="form.otherPipelineMaterialUsed" nestingPath="form.pipelineMaterial" labelText="Provide details of other materials used" characterCount=true maxCharacterLength="4000"/>
                        </#if>
                    </@fdsRadio.radioItem>
                    <#assign firstItem=false/>
                </#list>
            </@fdsRadio.radioGroup>

            <@fdsTextInput.textInput path="form.pipelineDesignLife" labelText="What is the design life of the pipeline?" suffix="years" inputClass="govuk-input--width-5"/>

            <@fdsRadio.radioGroup path="form.pipelineInBundle" labelText="Is the full length of this pipeline in a bundle?" hiddenContent=true>
                <@fdsRadio.radioYes path="form.pipelineInBundle">
                    <@fdsSearchSelector.searchSelectorRest path="form.bundleName" restUrl=springUrl(bundleNameRestUrl) labelText="What is the name of the bundle?" hintText="Use the same bundle name on all pipelines in the same bundle. Each different bundle must be given a unique name." preselectedItems={"${form.bundleName!''}": "${form.bundleName!''}"}/>
                </@fdsRadio.radioYes>
                <@fdsRadio.radioNo path="form.pipelineInBundle"/>
            </@fdsRadio.radioGroup>

        </@fdsFieldset.fieldset>

        <#if questionsForPipelineStatus?has_content>
            <#list questionsForPipelineStatus as question>
                <#if question == "OUT_OF_USE_ON_SEABED_REASON">
                    <@fdsTextarea.textarea path="form.whyNotReturnedToShore" labelText=question.getDisplayText() characterCount=true maxCharacterLength="4000"/>
                </#if>
            </#list>
        </#if>

        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="${screenActionType.submitButtonText} pipeline" secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>

    </@fdsForm.htmlForm>
</@defaultPage>