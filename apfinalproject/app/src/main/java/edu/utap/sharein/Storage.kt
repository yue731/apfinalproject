package edu.utap.sharein

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import java.io.File


// store images in firebase storage

class Storage {
    // create a storage reference from our app
    private val photoStorage: StorageReference = FirebaseStorage.getInstance().reference.child("images")

    fun uploadImage(localFile: File, uuid: String, uploadSuccess: () -> Unit) {
        val file = Uri.fromFile(localFile)
        val uuidRef = photoStorage.child(uuid)
        val metadata = StorageMetadata.Builder()
            .setContentType("image/jpg")
            .build()
        val uploadTask = uuidRef.putFile(file, metadata) // upload in background

        // Register observers to listen for when the download is done or if it fails
        uploadTask
            .addOnFailureListener {
                if (localFile.delete()) {
                    Log.d(javaClass.simpleName, "Upload FAILED $uuid, file deleted")
                }
                else {
                    Log.d(javaClass.simpleName, "Upload FAILED $uuid, file delete FAILED")
                }
            }
            .addOnSuccessListener {
                uploadSuccess()
                if(localFile.delete()) {
                    Log.d(javaClass.simpleName, "Upload succeeded $uuid, file deleted")
                }
                else {
                    Log.d(javaClass.simpleName, "Upload succeeded $uuid, file delete FAILED")
                }
            }
    }

    fun deleteImage(pictureUUID: String) {
        photoStorage.child(pictureUUID).delete()
            .addOnSuccessListener {
                Log.d(javaClass.simpleName, "Deleted $pictureUUID")
            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "Delete FAILED of $pictureUUID")
            }
    }

    fun listAllImages(listSuccess: (List<String>) -> Unit) {
        photoStorage.listAll()
            .addOnSuccessListener { listResult ->
                Log.d(javaClass.simpleName, "listAllImages len: ${listResult.items.size}")
                val pictureUUIDs = listResult.items.map {
                    it.name
                }
                listSuccess(pictureUUIDs)
            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "listAllImages FAILED")
            }
    }

    fun uuid2StorageReference(pictureUUID: String): StorageReference {
        return photoStorage.child(pictureUUID)
    }
}