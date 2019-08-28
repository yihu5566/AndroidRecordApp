package com.recordapp.record

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.ArrayList

class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    var permissionList: ArrayList<String> = ArrayList()
    /**存放音频文件列表 */
    private var recordFiles: ArrayList<String>? = null
    /**文件存在 */
    private var sdcardExit: Boolean = false
    var myRecAudioDir: String? = Environment.getExternalStorageDirectory().path + "/super_start"
    var myRecyclerAdapter: MyRecyclerAdapter? = null
    var isMp3: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sfl_main_root.setOnRefreshListener(this)
        // 判断sd Card是否插入
        sdcardExit = Environment.getExternalStorageState() == android.os.Environment.MEDIA_MOUNTED
        btn_merge.setOnClickListener(View.OnClickListener {
            if (isMp3) {
                startActivityForResult(Intent(this, RecordAudioRecordViewActivity::class.java), 1001)
            } else {
                startActivityForResult(Intent(this, RecordViewActivity::class.java), 1001)
            }

        })
        getPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.RECORD_AUDIO

        )
        recordFiles = ArrayList<String>()
        getRecordFiles()
        if (recordFiles!!.size == 0) {
            rlv_main.visibility = View.GONE
            tv_not_data.visibility=View.VISIBLE
        } else {
            rlv_main.visibility = View.VISIBLE
            tv_not_data.visibility=View.GONE
        }
        myRecyclerAdapter = MyRecyclerAdapter(this, recordFiles)
        myRecyclerAdapter!!.setListener { path -> openFile(File(path)) }
        rlv_main.layoutManager = LinearLayoutManager(this)
        rlv_main.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        rlv_main.adapter = myRecyclerAdapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1001) {
                var myRecAudioDir = data!!.getStringExtra("record_url")
                recordFiles!!.add(File(myRecAudioDir).name)
                myRecyclerAdapter!!.notifyDataSetChanged()
            }
        }
    }

    override fun onRefresh() {
        getRecordFiles()
        if (recordFiles!!.size == 0) {
            rlv_main.visibility = View.GONE
            tv_not_data.visibility=View.VISIBLE
        } else {
            rlv_main.visibility = View.VISIBLE
            tv_not_data.visibility=View.GONE
        }
        myRecyclerAdapter!!.notifyDataSetChanged()
    }

    private fun getRecordFiles() {
        recordFiles!!.clear()
        if (sdcardExit) {
            val files = File(myRecAudioDir).listFiles()
            if (files != null) {
                for (i in files!!.indices) {
                    if (files!![i].getName().indexOf(".") >= 0) { // 只取.amr 文件
                        val fileS = files!![i].getName().substring(
                            files!![i].getName().indexOf(".")
                        )
                        if (fileS.toLowerCase() == ".mp3"
                            || fileS.toLowerCase() == ".amr"
                            || fileS.toLowerCase() == ".mp4"
                        )
                            recordFiles!!.add(files!![i].getName())

                    }
                }
                sfl_main_root.isRefreshing = false
            }
        }

    }

    private fun getPermission(context: Activity, vararg permission: String) {

        for (i in permission.indices) {
            if (ContextCompat.checkSelfPermission(context, permission[i]) !== PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission[i])
            }
        }
        if (permissionList.size > 0) {
            val permissions = arrayOfNulls<String>(permissionList.size)
            for (i in permissionList.indices) {
                permissions[i] = permissionList.get(i)
            }
            ActivityCompat.requestPermissions(context, permissions, 1)
        }

    }

    private fun getTime(): String {
        //        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日HH：mm：ss");
        //        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        //        String time = formatter.format(curDate);
        return "start_" + System.currentTimeMillis()
    }

    private fun openFile(f: File) {
        var intent = Intent(Intent.ACTION_VIEW)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            var uriForFile = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileProvider", f)
            intent.setDataAndType(uriForFile, getMIMEType(f))
        } else {
            intent.setDataAndType(Uri.fromFile(f), getMIMEType(f))
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
//        intent = Intent()
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        intent.action = Intent.ACTION_VIEW
//        val type = getMIMEType(f)
//        intent.setDataAndType(Uri.fromFile(f), type)
//        startActivity(intent)
//        		Uri uri=Uri.fromFile(f)
//        MediaPlayer mediaPlayer = MediaPlayer.create (this, uri);
//        try {
//            mediaPlayer.prepare();
//        } catch (IllegalStateException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        mediaPlayer.start();
    }

    private fun getMIMEType(f: File): String {

        val end = f.name.substring(
            f.name.lastIndexOf(".") + 1,
            f.name.length
        ).toLowerCase()
        var type = ""
        if (end == "mp3" || end == "aac" || end == "amr"
            || end == "mpeg" || end == "mp4"
        ) {
            type = "audio"
        } else if (end == "jpg" || end == "gif" || end == "png"
            || end == "jpeg"
        ) {
            type = "image"
        } else {
            type = "*"
        }
        type += "/"
        return type
    }
}
