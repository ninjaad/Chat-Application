package com.example.whatsappclone

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.shapes.Shape
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask

class SignUpActivity : AppCompatActivity() {

    val storage by lazy {
        FirebaseStorage.getInstance()
    }
    val auth by lazy {
        FirebaseAuth.getInstance()
    }

    val database by lazy {
        FirebaseFirestore.getInstance()
    }
    lateinit var downloadUrl:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        val userImgView = findViewById<ShapeableImageView>(R.id.userImgView)
        userImgView.setOnClickListener {
            checkPermissionForImage()
        }
        val nextBtn = findViewById<MaterialButton>(R.id.nextBtn)
        val nameEt= findViewById<EditText>(R.id.nameEt)
        nextBtn.setOnClickListener{
            nextBtn.isEnabled= false
            val name = nameEt.text.toString()
            if(name.isEmpty()){
                Toast.makeText(this,"Name cannot be Empty",Toast.LENGTH_SHORT).show()
            }
            else if(!::downloadUrl.isInitialized){
                Toast.makeText(this,"Image cannot be Empty",Toast.LENGTH_SHORT).show()
            }
            else{
                val user = User(name,downloadUrl,downloadUrl,auth.uid!!)
                database.collection("users").document(auth.uid!!).set(user).addOnSuccessListener {

                    startActivity(
                        Intent(this,MainActivity::class.java)
                    )
                    finish()
                }.addOnFailureListener{
                    nextBtn.isEnabled=true
                }
            }
        }
    }

    private fun checkPermissionForImage() {


        if ((checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            && (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
        ) {
            val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            val permissionWrite = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

            requestPermissions(
                permission,
                1001
            )//GIVE AN INTEGER VALUE FOR PERMISSION_ CODE_READ LIKE 1001

            requestPermissions(
                permissionWrite,
                1002
            )//GIVE AN INTEGER VALUE FOR PERMISSION_ CODE_WRITE LIKE 1002
        } else {
            pickImageFromGallery()
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type= "image/*"
        startActivityForResult(
            intent,
            1000
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val userImgView = findViewById<ShapeableImageView>(R.id.userImgView)
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode ==1000)
            data?.data?.let {
                userImgView.setImageURI(it)
                uploadImage(it)
            }
    }

    private fun uploadImage(it: Uri) {
        val nextBtn=findViewById<MaterialButton>(R.id.nextBtn)
           nextBtn.isEnabled = false
        val ref= storage.reference.child("uploads/"+auth.uid.toString())
        val uploadTask = ref.putFile(it)
        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation ref.downloadUrl
        }).addOnCompleteListener { task ->
            nextBtn.isEnabled = true
            if(task.isSuccessful){
                downloadUrl = task.result.toString()
                Log.i("URl","downloadUrl: $downloadUrl")
            }
            else{

            }
        }.addOnFailureListener {

        }
    }
}