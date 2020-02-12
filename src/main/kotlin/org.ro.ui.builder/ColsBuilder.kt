package org.ro.ui.builder

import org.ro.layout.ColsLayout
import org.ro.to.TObject
import org.ro.ui.kv.RoDisplay
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.VPanel

class ColsBuilder {

    fun create(colsLayout: ColsLayout, tObject: TObject, dsp: RoDisplay): SimplePanel {
        val result = VPanel()
        val b = ColBuilder().create(colsLayout.col!!.first(), tObject, dsp)
        result.add(b)
        return result
    }

}
