package ph.edu.auf.realmdiscussionbarebones.realm

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import org.mongodb.kbson.ObjectId
import ph.edu.auf.realmdiscussionbarebones.models.Pet
import ph.edu.auf.realmdiscussionbarebones.realm.realmmodels.OwnerRealm
import ph.edu.auf.realmdiscussionbarebones.realm.realmmodels.PetRealm
import ph.edu.auf.realmdiscussionbarebones.realm.realmmodels.PetTypeRealm
import java.lang.IllegalStateException

class RealmDatabase {
    private val realm: Realm by lazy {
        val config = RealmConfiguration.Builder(
            schema = setOf(PetRealm::class, OwnerRealm::class, PetTypeRealm::class)
        ).schemaVersion(1)
            .initialData{
                copyToRealm(PetTypeRealm().apply { petType = "Cat"; type = 1})
                copyToRealm(PetTypeRealm().apply { petType = "Dog"; type = 2})
            }
            .build()
        Realm.open(config)
    }

    fun getAllPets(): List<PetRealm>{
        return realm.query<PetRealm>().find()
    }

    fun getPetsByName(name: String): List<PetRealm>{
        return realm.query<PetRealm>("name CONTAINS $0", name).find()
    }

    suspend fun addPet(name: String, age: Int, type: String, ownerName: String = ""){
        realm.write {
            val pet = PetRealm().apply {
                this.name = name
                this.age = age
                this.petType = type
            }
            val managePet = copyToRealm(pet)
            if(ownerName.isNotEmpty()){
                val ownerResult: OwnerRealm? = realm.query<OwnerRealm>("name == $0", ownerName).first().find()
                if(ownerResult == null){
                    val owner = OwnerRealm().apply {
                        this.name = ownerName
                        this.pets.add(managePet)
                    }
                    val manageOwner = copyToRealm(owner)
                    managePet.owner = manageOwner
                }
                else{
                    findLatest(ownerResult)?.pets?.add(managePet)
                    findLatest(managePet)?.owner = findLatest(ownerResult)
                }
            }
        }
    }

    suspend fun deletePet(id: ObjectId){
        realm.write {
            query<PetRealm>("id == $0", id)
                .first()
                .find()
                ?.let { delete(it) }
                ?: throw IllegalStateException("Pet not found!")
        }
    }

    fun getAllOwners(): List<OwnerRealm>{
        return realm.query<OwnerRealm>().find()
    }



}