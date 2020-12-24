package com.udacity

import android.app.DownloadManager
import android.app.DownloadManager.STATUS_SUCCESSFUL
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.udacity.databinding.ActivityDetailBinding
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.view.*

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail)
        setSupportActionBar(toolbar)

        setupDownloadDetails()
    }

    private fun setupDownloadDetails() {
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val downloadID :Long = intent.getLongExtra("download_id", -1)
        val query = DownloadManager.Query()
        query.setFilterById(downloadID)

        val statusString: Int

        val cursor = downloadManager.query(query)
        if(cursor.moveToFirst()){
            val status = cursor.getInt(
                cursor.getColumnIndex(
                    DownloadManager.COLUMN_STATUS
                )
            )

            when (status) {
                STATUS_SUCCESSFUL -> {
                    statusString =  R.string.success
                } else -> {
                    statusString = R.string.fail
                    binding.contentDetail.status_text.setTextColor(getColor(R.color.colorAccent))
                }
            }

        } else {
            statusString = R.string.fail
            binding.contentDetail.status_text.setTextColor(getColor(R.color.colorAccent))
        }

        val fileNameString = when (DownloadChoice.valueOf (intent.getStringExtra("download_choice") ?: "")) {
            DownloadChoice.UDACITY -> R.string.udacity_repo_radio_button_title
            DownloadChoice.GLIDE -> R.string.glide_repo_radio_button_title
            DownloadChoice.RETROFIT -> R.string.retrofit_repo_radio_button_title
            DownloadChoice.NONE -> R.string.na
        }

        binding.contentDetail.file_name_text.text = getString(fileNameString)

        binding.contentDetail.status_text.text = getString(statusString)

        binding.contentDetail.ok_button.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}
