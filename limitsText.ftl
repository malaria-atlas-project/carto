<#ftl strip_text=true strip_whitespace=true>
<@compress single_line=true>
<#if (nSurveys > 0)>The ${nSurveys} <i>${parasiteAbbr}</i> parasite rate surveys available for predicting prevalance within the stable limits were collected between ${yearStart} and ${yearEnd}.</#if>
<#if (parasite == "Pf")><#if (nSurveys > 0)><br></#if><b>Citation:</b> Guerra, C.A. <i>et al.</i> (2008). The limits and intensity of <i>Plasmodium falciparum</i> transmission: implications for malaria control and elimination worldwide. <i>PLoS Medicine</i> <b>5</b>: e38.</#if>
<#if (parasite == "Pv")><#if (nSurveys > 0)><br></#if><b>Citation:</b> Guerra, C.A. <i>et al.</i> (2010). The international limits and population at risk of <i>Plasmodium vivax</i> transmission in 2009. <i>Public Library of Science Neglected Tropical Diseases</i>, <b>4</b>(8): e774.</#if>
<br><#include "copyright.ftl">
</@compress>
