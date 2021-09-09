<#include '../../layout.ftl'>

<#macro text widgetDisplayName>
    <@fdsInsetText.insetText>The ${widgetDisplayName} below supports <@fdsAction.link linkText="Markdown" linkUrl=springUrl(markdownGuidanceUrl) openInNewTab=true/> for text formatting.</@fdsInsetText.insetText>
</#macro>