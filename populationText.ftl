<#ftl strip_text=true strip_whitespace=true>
<@compress single_line=true>
<b>Population total:</b> The 2010 estimate for the total population of ${country_name} is ${pop_total?string("###,###,###,###")}.

<br><b>Derivation:</b> The GRUMP beta (http://sedac.ciesin.columbia.edu/gpw/) version provides gridded population counts at 1x1 km globally for the year 2000 
and an ancillary surface of urban extents. These were projected to the year 2010 by applying national, 
urban and rural specific growth rates (http://esa.un.org/unup/) to the relevant areas and adjusting national totals to match 
the United Nations' estimates. This resulted in the 2010 population count surface shown, which was used 
to derive the population totals. 
<#if usesAfripop>
This map is currently being re-modelled using new improved data sources (http://www.afripop.org/).
</#if>

<br><#include "copyright.ftl">
</@compress>
