<#include '../../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="deposit" type="uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PermanentDepositDrawingView" -->


<#macro depositDrawingViewSummary depositDrawingView urlFactory>
    <dl class="govuk-summary-list govuk-!-margin-bottom-9">          

        <div class="govuk-summary-list__row">
            <dt class="govuk-summary-list__key">Deposit drawing</dt>
            <dd class="govuk-summary-list__value"> 
                <#if depositDrawingView.fileId??>
                    <@fdsAction.link linkText=depositDrawingView.fileName linkUrl=springUrl(urlFactory.getPipelineDrawingDownloadUrl(depositDrawingView.fileId)) linkClass="govuk-link" linkScreenReaderText="Download ${depositDrawingView.fileName}" role=false start=false openInNewTab=true/>
                <#else>
                    <span> No file uploaded </span>
                </#if>
            </dd>                    
        </div>
        <#if depositDrawingView.fileId??>
            <div class="govuk-summary-list__row">
                <dt class="govuk-summary-list__key">File description</dt>
                <dd class="govuk-summary-list__value"> ${depositDrawingView.documentDescription}</dd>                 
            </div>
        </#if>
        <div class="govuk-summary-list__row">
            <dt class="govuk-summary-list__key">Deposits on drawing</dt>
            <dd class="govuk-summary-list__value">
                <#list depositDrawingView.depositReferences?sort as ref>
                    <ul class="govuk-list">
                        <li>${ref}</li>
                    </ul>
                </#list>
            </dd>                    
        </div>
        
    </dl>
</#macro>