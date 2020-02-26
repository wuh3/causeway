package org.ro.core.model

import org.ro.layout.Layout
import org.ro.to.DomainType
import org.ro.to.Property
import org.ro.to.TransferObject

class DiagramDisplay(override val title: String) : BaseDisplayable() {

    override var layout: Layout? = null

    val classes = mutableSetOf<DomainType>()
    val properties = mutableSetOf<Property>()
    var numberOfClasses = -1
    private var numberOfProperties = 0

    fun incNumberOfProperties(inc: Int) {
        numberOfProperties += inc
    }

    fun decNumberOfClasses() {
        numberOfClasses--
    }

    override fun canBeDisplayed(): Boolean {
        console.log("[DiagramDisplay.canBeDisplayed]")
        console.log(this)
        return (numberOfClasses == classes.size
                //TODO && numberOfProperties == properties.size
        )
    }

    override fun addData(obj: TransferObject) {
        console.log("[DiagramDisplay.addData] ${obj::class}")
        console.log(obj)
        when (obj) {
            is DomainType -> classes.add(obj)
            is Property -> properties.add(obj)
            else -> {
            }
        }
    }

}
