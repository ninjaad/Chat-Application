package com.example.whatsappclone

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit


const val PHONE_NUMBER="phoneNumber"
class OtpActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var callbacks:PhoneAuthProvider.OnVerificationStateChangedCallbacks
    var phoneNumber:String? = null
    var mVerificationId:String?= null
    var mResendToken:PhoneAuthProvider.ForceResendingToken?=null
    private lateinit var progressDialog: ProgressDialog
    private var mCounterDown: CountDownTimer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)
        initViews()
        startVerify()
    }

    private fun startVerify() {
            startPhoneNumberVerification(phoneNumber!!)
            showTimer(60000)
            progressDialog = createProgressDialog("Sending a verification code", false)
            progressDialog.show()
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,      // Phone number to verify
            60,               // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this,            // Activity (for callback binding)
            callbacks
        ) // OnVerificationStateChangedCallbacks
    }

    private fun showTimer(milliSecInFuture: Long) {
        val counterTv = findViewById<TextView>(R.id.counterTv)
        val resendBtn = findViewById<Button>(R.id.resendBtn)
        resendBtn.isEnabled = false
        object : CountDownTimer(milliSecInFuture,1000) {


            override fun onFinish() {
             resendBtn.isEnabled = true
             counterTv.isVisible = false
            }

            override fun onTick(millisUntilFinished: Long) {
                counterTv.isVisible= true
                counterTv.text = getString(R.string.seconds_remaining,millisUntilFinished/1000)
            }


        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(mCounterDown!=null){
            mCounterDown!!.cancel()
        }
    }

    private fun initViews() {

        phoneNumber = intent.getStringExtra(PHONE_NUMBER )
        val verifyTv = findViewById<TextView>(R.id.verifyTv)
        verifyTv.text = getString(R.string.verify_number, phoneNumber)
        setSpannableString()
        val verificationBtn = findViewById<MaterialButton>(R.id.verificationBtn)
        val resendBtn = findViewById<MaterialButton>(R.id.resendBtn)
        verificationBtn.setOnClickListener(this)
        resendBtn.setOnClickListener(this)


        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.

                if (::progressDialog.isInitialized) {
                    progressDialog.dismiss()
                }

                val smsCode:String? = credential.smsCode
                if(!smsCode.isNullOrEmpty()) {
                    val sentcodeEt = findViewById<EditText>(R.id.sentcodeEt)
                    sentcodeEt.setText(smsCode)
                }
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.


                if (::progressDialog.isInitialized) {
                    progressDialog.dismiss()
                }

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                    // reCAPTCHA verification attempted with null Activity
                }



            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {

                val counterTv=findViewById<TextView>(R.id.counterTv)
                progressDialog.dismiss()
                counterTv.isVisible = false
                // Save verification ID and resending token so we can use them later
                Log.e("onCodeSent==", "onCodeSent:$verificationId")
                mVerificationId = verificationId
                mResendToken = token
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        val mAuth = FirebaseAuth.getInstance()
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    startActivity(
                        Intent(this, SignUpActivity::class.java)
                    )
                    finish()
                }
                else{
                    notifyUserAndRetry("Your Phone Number Verification is failed.Retry again!")
                }
            }
    }

    private fun notifyUserAndRetry(message: String) {
        MaterialAlertDialogBuilder(this).apply {
            setMessage(message)
            setPositiveButton("Ok") { _, _ ->
                showLoginActivity()
            }

            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

            setCancelable(false)
            create()
            show()
        }
    }

    @SuppressLint("StringFormatInvalid")
    private fun setSpannableString() {
        val span = SpannableString(getString(R.string.waiting_text , phoneNumber))
        val clickableSpan = object :ClickableSpan(){
            override fun onClick(widget: View) {
                //send back

                showLoginActivity()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = ds.linkColor
            }


        }

        span.setSpan(clickableSpan,span.length-13,span.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val waitingTv=findViewById<TextView>(R.id.waitingTv)
        waitingTv.movementMethod = LinkMovementMethod.getInstance()
        waitingTv.text = span

    }

    private fun showLoginActivity() {
        startActivity(Intent(this,LoginActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))

    }


    override fun onBackPressed() {
        super.onBackPressed()


    }


    override fun onClick(v: View?) {
        val verificationBtn = findViewById<MaterialButton>(R.id.verificationBtn)
        val sentcodeEt = findViewById<EditText>(R.id.sentcodeEt)
        val resendBtn = findViewById<MaterialButton>(R.id.resendBtn)

        when (v) {
            verificationBtn -> {
                // try to enter the code by yourself to handle the case
                // if user enter another sim card used in another phone ...
                var code = sentcodeEt.text.toString()
                if (code.isNotEmpty() && !mVerificationId.isNullOrEmpty()) {

                    progressDialog = createProgressDialog("Please wait...", false)
                    progressDialog.show()
                    val credential =
                        PhoneAuthProvider.getCredential(mVerificationId!!, code.toString())
                    signInWithPhoneAuthCredential(credential)
                }
            }

            resendBtn -> {
                if (mResendToken != null) {
                    showTimer(60000)
                    progressDialog = createProgressDialog("Sending a verification code", false)
                    progressDialog.show()
                }
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNumber!!,
                    60,
                    TimeUnit.SECONDS,
                    this,
                    callbacks,
                    mResendToken

                )
            }

        }
    }
    }





fun Context.createProgressDialog(message:String,isCancelable:Boolean):ProgressDialog{
    return ProgressDialog(this).apply {
        setCancelable(false)
        setMessage(message)
        setCanceledOnTouchOutside(false)

    }
}