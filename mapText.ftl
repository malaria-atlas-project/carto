<#ftl strip_text=true strip_whitespace=true>
 <@compress single_line=true>
 
<b>Population total:</b> The population in ${country_name} in 2010 totals ${pop_total?string("###,###,###,###")}
of which ${rural?string("###,###,###,###")} live in rural areas.

~<b>Derivation:</b> The GRUMP <i>beta</i> (http://sedac.ciesin.columbia.edu/gpw) version provides gridded population counts at 1 x 1 km 
globally for the year 2000 adjusted to the United Nations’ national population estimates and an ancillary surface of 
urban extents. These were projected to the year 2010 by applying national, medium variant, urban and rural-specific 
growth rates (http://esa.un.org/unpp) to the relevant areas. This resulted in the 2010 population count surface shown, 
which was used to derive the population totals. 
~<b>Copyright:</b> Licensed to the Malaria Atlas Project (MAP; www.map.ox.ac.uk) under a Creative Commons Attribution 3.0 License (http://creativecommons.org).
</@compress>
