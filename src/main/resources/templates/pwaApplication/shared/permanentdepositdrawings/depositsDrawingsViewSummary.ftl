<#include '../../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" --> 
<#-- @ftlvariable name="deposit" type="uk.co.ogauthority.pwa.model.form.pwaapplications.views.PermanentDepositDrawingView" --> 


<#macro depositDrawingViewSummary depositDrawingView urlFactory>
    <dl class="govuk-summary-list govuk-!-margin-bottom-9">          

        <div class="govuk-summary-list__row">
            <dt class="govuk-summary-list__key">Deposit drawing</dt>
            <dd class="govuk-summary-list__value"> 
                <@fdsAction.link linkText=depositDrawingView.fileName linkUrl=springUrl(urlFactory.getPipelineDrawingDownloadUrl(depositDrawingView.fileId)) linkClass="govuk-link" linkScreenReaderText="Download ${depositDrawingView.fileName}" role=false start=false openInNewTab=true/>
            </dd>                    
        </div>
        <div class="govuk-summary-list__row">
            <dt class="govuk-summary-list__key">File description</dt>
            <dd class="govuk-summary-list__value"> ${depositDrawingView.documentDescription}</dd>                 
        </div>
        <div class="govuk-summary-list__row">
            <dt class="govuk-summary-list__key">Deposits on drawing</dt>
            <dd class="govuk-summary-list__value">
                <#list depositDrawingView.depositReferences as ref>
                    <li>${ref}</li>
                </#list>
            </dd>                    
        </div>
        
    </dl>
</#macro>