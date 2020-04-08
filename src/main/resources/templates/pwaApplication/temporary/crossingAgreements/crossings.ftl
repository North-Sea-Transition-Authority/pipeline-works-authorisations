<#include '../../../layout.ftl'>
<#import '../../../dummyFileUpload.ftl' as dummyFileUpload/>

<#-- @ftlvariable name="blockCrossings" type="java.util.List<uk.co.ogauthority.pwa.temp.model.entity.BlockCrossing>" -->
<#-- @ftlvariable name="telecommunicationCrossings" type="java.util.List<uk.co.ogauthority.pwa.temp.model.entity.TelecommunicationCableCrossing>" -->
<#-- @ftlvariable name="pipelineCrossings" type="java.util.List<uk.co.ogauthority.pwa.temp.model.entity.PipelineCrossing>" -->
<#-- @ftlvariable name="addBlockCrossingUrl" type="java.lang.String" -->
<#-- @ftlvariable name="addTelecommuncationCableCrossingUrl" type="java.lang.String" -->
<#-- @ftlvariable name="addPipelineCrossingUrl" type="java.lang.String" -->

<@defaultPage htmlTitle="Crossings" pageHeading="Crossing agreements" breadcrumbs=true>

    <@fdsForm.htmlForm>
      <h2 class="govuk-heading-l">Block Crossings</h2>
        <@fdsAction.link linkText="Add block crossing" linkUrl=springUrl(addBlockCrossingUrl) linkClass="govuk-button govuk-button--blue"/>
        <#if blockCrossings?has_content>
          <table class="govuk-table">
            <thead class="govuk-table__head">
            <tr class="govuk-table__row">
              <th class="govuk-table__header" scope="col">UK block number</th>
              <th class="govuk-table__header" scope="col">License number</th>
              <th class="govuk-table__header" scope="col">Operator agreement confirmation</th>
            </tr>
            </thead>
            <tbody class="govuk-table__body">
            <#list blockCrossings as crossing>
              <tr class="govuk-table__row">
                <td class="govuk-table__cell">${crossing.blockNumber}</td>
                <td class="govuk-table__cell">${crossing.licenseNumber}</td>
                <td class="govuk-table__cell">${crossing.operatorAgreement}</td>
              </tr>
            </#list>
            </tbody>
          </table>
          <h3 class="govuk-heading-m">Upload documents</h3>
            <@dummyFileUpload.fileUpload id="1" uploadUrl="/" deleteUrl="" downloadUrl="" maxAllowedSize=500 allowedExtensions="txt"/>
          <div class="uploaded-file">
            <div class="uploaded-file__info">
              <div class="uploaded-file__file-info-wrapper">
                  <@fdsAction.link linkText="BLOCK_CROSSING_AGREEMENT_HL.pdf" linkUrl="/" linkClass="uploaded-file__filename"/>
                <span class="uploaded-file__extra-info"> - 1 kB</span>
                  <@fdsAction.link linkText="Delete" linkUrl="/" linkClass="govuk-link uploaded-file__delete-link"/>
              </div>
              <div class="govuk-form-group govuk-form-group--file-upload">
                <label class="govuk-label" for="file-upload-description">Operators in agreement document</label>
                <textarea class="govuk-textarea govuk-textarea--file-upload" id="file-upload-description" name="area"
                          rows="2">HESS LIMITED</textarea>
              </div>
            </div>
          </div>
          <hr class="govuk-section-break govuk-section-break--m">
        <#else>
          <p class="govuk-body">No block crossing agreements</p>
        </#if>

      <h2 class="govuk-heading-l">Telecommunication Cable Crossings</h2>
        <@fdsAction.link linkText="Add telecommunication cable crossing" linkUrl=springUrl(addTelecommuncationCableCrossingUrl) linkClass="govuk-button govuk-button--blue"/>
        <#if telecommunicationCableCrossings?has_content>
          <table class="govuk-table">
            <thead class="govuk-table__head">
            <tr class="govuk-table__row">
              <th class="govuk-table__header" scope="col">Cable name/location</th>
              <th class="govuk-table__header" scope="col">Holder of cable</th>
            </tr>
            </thead>
            <tbody class="govuk-table__body">
            <#list telecommunicationCableCrossings as crossing>
              <tr class="govuk-table__row">
                <td class="govuk-table__cell">${crossing.cableNameOrLocation}</td>
                <td class="govuk-table__cell">${crossing.holderOfCable}</td>
              </tr>
            </#list>
            </tbody>
          </table>
          <h3 class="govuk-heading-m">Upload documents</h3>
            <@dummyFileUpload.fileUpload id="2" uploadUrl="/" deleteUrl="" downloadUrl="" maxAllowedSize=500 allowedExtensions="txt"/>
          <hr class="govuk-section-break govuk-section-break--m">
        <#else>
          <p class="govuk-body">No telecommunication crossing agreements</p>
        </#if>

      <h2 class="govuk-heading-l">Pipeline Crossings</h2>
        <@fdsAction.link linkText="Add pipeline crossing" linkUrl=springUrl(addPipelineCrossingUrl) linkClass="govuk-button govuk-button--blue"/>
        <#if pipelineCrossings?has_content>
          <table class="govuk-table">
            <thead class="govuk-table__head">
            <tr class="govuk-table__row">
              <th class="govuk-table__header" scope="col">Pipeline number</th>
              <th class="govuk-table__header" scope="col">Owner of pipeline (PWA holder)</th>
            </tr>
            </thead>
            <tbody class="govuk-table__body">
            <#list pipelineCrossings as crossing>
              <tr class="govuk-table__row">
                <td class="govuk-table__cell">${crossing.pipelineNumber}</td>
                <td class="govuk-table__cell">${crossing.ownerOfPipeline}</td>
              </tr>
            </#list>
            </tbody>
          </table>
          <h3 class="govuk-heading-m">Upload documents</h3>
            <@dummyFileUpload.fileUpload id="3" uploadUrl="/" deleteUrl="" downloadUrl="" maxAllowedSize=500 allowedExtensions="txt"/>
          <hr class="govuk-section-break govuk-section-break--m">
        <#else>
          <p class="govuk-body">No pipeline crossing agreements</p>
        </#if>

        <@fdsAction.submitButtons errorMessage="" linkSecondaryAction=true primaryButtonText="Complete" secondaryLinkText="Save and continue later"/>
    </@fdsForm.htmlForm>

</@defaultPage>