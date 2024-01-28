package com.example.whatsappclone

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hbb20.CountryCodePicker
import androidx.core.widget.addTextChangedListener as addTextChangedListener1

class LoginActivity : AppCompatActivity() {

    private lateinit var phoneNumber:String
    private lateinit var countryCode:String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val phoneNumberEt= findViewById<EditText>(R.id.phoneNumberEt)
        val nextBtn= findViewById<MaterialButton>(R.id.nextBtn)
        phoneNumberEt.addTextChangedListener1 {
            nextBtn.isEnabled = !(it.isNullOrEmpty() || it.length < 10)
        }

        nextBtn.setOnClickListener {
            checkNumber()
        }
    }

    private fun checkNumber() {
        val ccp=findViewById<CountryCodePicker>(R.id.ccp)
        val phoneNumberEt= findViewById<EditText>(R.id.phoneNumberEt)

        countryCode = ccp.selectedCountryCodeWithPlus
        phoneNumber = countryCode+ phoneNumberEt.text.toString()

        notifyUser()
    }

    private fun notifyUser() {
        MaterialAlertDialogBuilder(this).apply {
            setMessage("We will be verifying the phone number:$phoneNumber\n"+
            "Is this OK, or would you like to edit the number?")

            setPositiveButton("OK"){ _,_ ->
                showOtpActivity()

            }
            setNegativeButton("Edit"){dialog, which ->
                dialog.dismiss()

            }
            setCancelable(false)
            create()
            show()
        }
    }

    private fun showOtpActivity() {

        startActivity(Intent(this,OtpActivity::class.java).putExtra(PHONE_NUMBER,phoneNumber))
        finish()
    }
}