package com.davebudah.leaderboard

import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.davebudah.leaderboard.api.RetrofitClient
import com.davebudah.leaderboard.databinding.ActivitySubmitBinding
import com.davebudah.leaderboard.ui.main.fragments.AlertFragment
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SubmitActivity : AppCompatActivity(), AlertFragment.OnRequestConfirmationListener {

    private lateinit var lName: String
    private lateinit var fName: String
    private lateinit var eAddress: String
    private lateinit var repoLink: String
    private lateinit var mBinding: ActivitySubmitBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_submit)
        mBinding.loading = false
        mBinding.clearForm = false

        events()

    }
    override fun onConfirmed() {
        processForm()
    }
    private fun events () {
        mBinding.backBtn.setOnClickListener{ onBackPressed()}
        mBinding.buttonSubmit.setOnClickListener{ confirm()}
    }

    private fun confirm() {
        fName = mBinding.firstName.text.toString().trim()
        lName = mBinding.lastName.text.toString().trim()
        eAddress = mBinding.emailAddress.text.toString().trim()
        repoLink = mBinding.projectLink.text.toString().trim()

        if (fName.isEmpty() || lName.isEmpty() || eAddress.isEmpty() || repoLink.isEmpty()) {
            show(message = "Please flood all input fields")
            return
        }
        if (!eAddress.isValidEmail()) {
            show(message = "Enter a valid email address.")
            return
        }
        if (!repoLink.isValidUrl()) {
            show(message = "Enter a valid URL.")
            return
        }

        AlertFragment.newInstance("confirmation").show(supportFragmentManager, "")

    }
    private fun processForm() {
        mBinding.loading = true
        RetrofitClient.getFormInstance().submit(fName, lName, eAddress, repoLink)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    mBinding.loading = false
                    AlertFragment.newInstance(if (response.isSuccessful) "success" else "failure")
                        .show(supportFragmentManager, "")
                    mBinding.clearForm = response.isSuccessful
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    mBinding.loading = false
                    AlertFragment.newInstance("failure").show(supportFragmentManager, "")
                }
            })

    }
    private fun String.isValidUrl(): Boolean = Patterns.WEB_URL.matcher(this).matches()

    private fun String.isValidEmail(): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(this).matches()

    private fun show(message: String) {
        Snackbar.make(mBinding.root, message, Snackbar.LENGTH_LONG).show()
    }

}