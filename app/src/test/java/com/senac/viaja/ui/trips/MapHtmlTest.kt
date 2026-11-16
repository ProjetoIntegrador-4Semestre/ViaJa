package com.senac.viaja.ui.trips
import com.senac.viaja.data.model.MapStop
import org.junit.Assert.*
import org.junit.Test
class MapHtmlTest{
    private fun point(name:String,latitude:Double?,longitude:Double?)=MapStop(1,name,null,latitude,longitude,1,1,null,false,null)
    @Test fun treatsPlaceNamesAsTextAndEscapesScriptTerminators(){val html=MapHtml.render(listOf(point("</script><script>alert('x')</script>",0.0,0.0)));assertFalse(html.contains("</script><script>alert"));assertTrue(html.contains("\\u003c"));assertTrue(html.contains("label.textContent=p.name"))}
    @Test fun excludesInvalidCoordinatesButKeepsEquator(){val points=MapHtml.valid(listOf(point("zero",0.0,0.0),point("invalid",100.0,0.0),point("missing",null,0.0),point("nan",Double.NaN,0.0)));assertEquals(1,points.size);assertEquals("zero",points.first().name)}
}
