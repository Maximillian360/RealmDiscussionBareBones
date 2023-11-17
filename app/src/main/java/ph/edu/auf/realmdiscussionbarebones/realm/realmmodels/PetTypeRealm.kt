package ph.edu.auf.realmdiscussionbarebones.realm.realmmodels

import io.realm.kotlin.types.RealmObject
import org.mongodb.kbson.ObjectId

class PetTypeRealm : RealmObject {
    var id: ObjectId = ObjectId()
    var petType: String = ""
    var type: Int = 0
}