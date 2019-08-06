package org.ro.to

import kotlinx.serialization.UnstableDefault
import org.ro.core.model.Revealator
import org.ro.core.model.ObjectList
import org.ro.handler.TObjectHandler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@UnstableDefault
class TObjectTest {

    @Test
    fun testParse() {
        val jsonStr = SO_0.str
        val to = TObjectHandler().parse(jsonStr) as TObject
        assertNotNull(to)
        assertNotNull(to.links)
    }

    @Test
    fun testMembers() {
        val jsonStr = SO_0.str
        val to = TObjectHandler().parse(jsonStr) as TObject
        assertEquals("Object: Foo", to.links[0].title)

        val members = to.members
        assertEquals(10, members.size)
        val properties = to.getProperties()
        assertEquals(4, properties.size)

        val objectList = ObjectList()
        to.addMembersAsProperties()  //FIXME move fun from TObject to OA?
        val oa1 = Revealator(to)
        objectList.list.add(oa1)

        val oa: Revealator = objectList.last()!!
        val actualDnId =  oa.get("datanucleusIdLong") as Value
        assertEquals(0, actualDnId.content)

        val actualDnvers =  oa.get("datanucleusVersionTimestamp") as Value
        assertEquals("1514897074953", actualDnvers.content)

        val actualNotes =  oa.get("notes")
        assertEquals(null, actualNotes)
    }

}
