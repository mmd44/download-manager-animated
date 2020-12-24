package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.udacity.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private var downloadChoice: DownloadChoice = DownloadChoice.NONE


    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(toolbar)


        createChannel(
            getString(R.string.dm_notification_channel_id),
            getString(R.string.dm_notification_channel_name)
        )

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        binding.contentMain.customButton.setOnClickListener {
            download()
        }

    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            val notificationManager = ContextCompat.getSystemService(
                context,
                NotificationManager::class.java
            ) as NotificationManager

            notificationManager.sendNotification(
                context.getText(R.string.notification_description).toString(),
                id,
                downloadChoice.toString(),
                context
            )

            binding.contentMain.customButton.downloadCompleted()
        }


    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
                .apply {
                    setShowBadge(false)
                }

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Download is Ready!"

            val notificationManager = this.getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun download() {

        val checkedID = binding.contentMain.repoOptionsRadioGroup.checkedRadioButtonId

        if (-1 != checkedID) {

            when (checkedID) {
                R.id.glide_radio_button -> downloadChoice = DownloadChoice.GLIDE
                R.id.udacity_radio_button -> downloadChoice = DownloadChoice.UDACITY
                R.id.retrofit_radio_button -> downloadChoice = DownloadChoice.RETROFIT
            }

            val request =
                DownloadManager.Request(Uri.parse(downloadChoice.url))
                    .setTitle(getString(R.string.app_name))
                    .setDescription(getString(R.string.app_description))
                    .setRequiresCharging(false)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)


            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager

            downloadID =
                downloadManager.enqueue(request)// enqueue puts the download request in the queue.

            // Tried to handle the progress concurrently with the total
            // and downloaded bytes but they don't get updated properly.
            /*thread {
                var downloading = true
                while (downloading){
                    val query = DownloadManager.Query()
                    query.setFilterById(downloadID)

                    val cursor = downloadManager.query(query)
                    if(cursor.moveToFirst()){

                        val totalBytes = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

                        if (totalBytes>0) {
                            val downloadedBytes = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                            //val progress = ((bytesDownloaded * 100L)/bytesTotal).toInt()
                            val progress = downloadedBytes * 100L / totalBytes
                            runOnUiThread {
                                Log.i("DownloadProgress: ", progress.toString())
                            }
                        }

                        if(cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL){
                            downloading = false
                        }

                        cursor.close()
                    }
                }
            }*/
        } else {
            binding.contentMain.customButton.downloadCompleted()
            Toast.makeText(this, getString(R.string.select_a_repo),
                Toast.LENGTH_LONG).show()
        }
    }

}

enum class DownloadChoice (val url: String) {
    UDACITY  ("https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"),
    GLIDE ("https://github.com/bumptech/glide/archive/master.zip"),
    RETROFIT ("https://github.com/square/retrofit/archive/master.zip"),
    NONE ("")
}


