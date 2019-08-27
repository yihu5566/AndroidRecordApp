package com.example.record_app

import android.app.Activity
import android.content.Intent
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.record_app.view.CircleProgressView
import kotlinx.android.synthetic.main.activity_record_view.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException


class RecordViewActivity : AppCompatActivity(), View.OnClickListener {
    private var circle_progress: CircleProgressView? = null
    private var tv_progress: TextView? = null
    private var playButton: Button? = null
    private var isStopRecord: Boolean = false
    private var currentProgress: Int = 0
    private var textView: TextView? = null
    /**
     * 是否暂停标志位
     */
    private var isPause: Boolean = false
    /**
     * 记录需要合成的几段amr语音文件
     */
    var list: ArrayList<String>? = null
    var myRecAudioFile: File? = null
    val SUFFIX = ".mp3"
    /**
     * 录音保存路径
     */
    var myRecAudioDir: File? = null
    var mMediaRecorder01: MediaRecorder? = null
    /**
     * 文件存在
     */
    var sdcardExit: Boolean = false
    /**
     * 第一次进入状态
     */
    private var isRecord: Boolean = true
    /**
     * 存儲文件路徑
     */
    var pathStr1: String? = null
    var finishFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_view)

        circle_progress = findViewById(R.id.circle_progress)
        tv_progress = findViewById(R.id.tv_progress)
        playButton = findViewById(R.id.btn_play_or_pause)
        textView = findViewById(R.id.tv_play_state)

        initData()
        initListener()
    }

    private fun initData() {
        //初始化list
        list = ArrayList()
        // 判断sd Card是否插入
        sdcardExit = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
        // 取得sd card路径作为录音文件的位置
        if (sdcardExit) {
            pathStr1 = Environment.getExternalStorageDirectory().path + "/super_start"
            myRecAudioDir = File(pathStr1!!)
            if (!myRecAudioDir!!.exists()) {
                myRecAudioDir!!.mkdirs()
                Log.v("录音", "创建录音文件！" + myRecAudioDir!!.exists())
            }
        }
    }

    private fun initListener() {
        playButton!!.setOnClickListener(this)
        btn_stop.setOnClickListener(View.OnClickListener {
            finishRecord()
        })
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_play_or_pause -> onRecord()
            else -> {
            }
        }
    }


    private fun onRecord() {
        if (isRecord) {
            isRecord = false
            list!!.clear()
            startRecord()
            return
        }
        if (isPause) {
            startRecord()
        } else {
            pauseRecord()
        }

    }


    fun startRecord() {
        try {
            textView!!.text = "暂停录音"
            isPause = false
            isStopRecord = false
            val mMinute1 = getTime()
            myRecAudioFile = File(myRecAudioDir, mMinute1 + SUFFIX)
            mMediaRecorder01 = MediaRecorder()
            // 设置录音为麦克风
            mMediaRecorder01!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            mMediaRecorder01!!.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
            mMediaRecorder01!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            //录音文件保存这里
            mMediaRecorder01!!.setOutputFile(myRecAudioFile!!.absolutePath)
            mMediaRecorder01!!.prepare()
            mMediaRecorder01!!.start()
            mMediaRecorder01!!.setOnInfoListener { mr, what, extra ->
                // TODO Auto-generated method stub
                val a = mr.maxAmplitude
                //Toast.makeText(RecordViewActivity.this, a, Toast.LENGTH_SHORT).show();
            }
            //Toast.makeText(this, R.string.toast_recording_start, Toast.LENGTH_SHORT).show();
            //start Chronometer
            playButton!!.setBackgroundResource(R.drawable.ic_pause_circle_filled_black_24dp)
            circle_progress!!.startAnimProgress()
            //playButton.setEnabled(true);
            //监听进度条进度
            circle_progress!!.setOnAnimProgressListener(
                object : CircleProgressView.OnAnimProgressListener {
                    override fun valueUpdate(progress: Int) {
                        currentProgress = progress
                        tv_progress!!.text = progress.toString()
                    }

                    override fun recordFinish(progress: Int) {
                        currentProgress = progress
                        tv_progress!!.text = progress.toString()
                        val builder = AlertDialog.Builder(this@RecordViewActivity)
                        builder.setMessage("录音完成了!").setPositiveButton("确认") { dialogInterface, i ->
                            finishRecord()

                        }.show()
                    }
                }
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun pauseRecord() {
        isPause = true
        isStopRecord = false
        //当前正在录音的文件名，全程
        list!!.add(myRecAudioFile!!.path)
        stopRecord()
        //计时停止
        circle_progress!!.pauseRecord()

    }

    protected fun stopRecord() {
        if (mMediaRecorder01 != null && !isStopRecord) {
            // 停止录音
            mMediaRecorder01!!.stop()
            mMediaRecorder01!!.release()
            mMediaRecorder01 = null
        }
        textView!!.text = "开始录音"
        circle_progress!!.stopRecord()
        //Toast.makeText(this, R.string.toast_recording_stop, Toast.LENGTH_SHORT).show();
        isStopRecord = true
        playButton!!.setBackgroundResource(R.drawable.ic_play_circle_filled_black_24dp)
    }

    fun finishRecord() {
        if (isPause) {
            //在暂停状态按下结束键,处理list就可以了
            getInputCollection(list!!)
        } else {
            textView!!.text = "开始录音"
            circle_progress!!.stopRecord()
            //            Toast.makeText(this, R.string.toast_recording_stop, Toast.LENGTH_SHORT).show();
            playButton!!.setBackgroundResource(R.drawable.ic_play_circle_filled_black_24dp)
            if (list!!.size > 0) {
                //在正在录音时，处理list里面的和正在录音的语音
                list!!.add(myRecAudioFile!!.path)
                if (mMediaRecorder01 != null && !isStopRecord) {
                    // 停止录音
                    mMediaRecorder01!!.stop()
                    mMediaRecorder01!!.release()
                    mMediaRecorder01 = null
                }
                isStopRecord = true
                //生成处理后的文件
                getInputCollection(list!!)
            } else {
                //若录音没有经过任何暂停
                if (myRecAudioFile != null) {
                    // 停止录音
                    mMediaRecorder01!!.stop()
                    mMediaRecorder01!!.release()
                    mMediaRecorder01 = null
                }
            }
        }
        val intent = Intent(this, MainActivity::class.java)

        if (list!!.size > 0) {
            intent.putExtra("record_url", finishFile!!.absolutePath)
        } else {
            intent.putExtra("record_url", myRecAudioFile!!.absolutePath)
        }

        setResult(Activity.RESULT_OK, intent)
        finish()
    }


    /**
     * activity的生命周期，stop时关闭录音资源
     */
    override fun onStop() {
        // TODO Auto-generated method stub
        if (mMediaRecorder01 != null && !isStopRecord) {
            // 停止录音
            mMediaRecorder01!!.stop()
            mMediaRecorder01!!.release()
            mMediaRecorder01 = null
        }
        super.onStop()
    }

    fun getTime(): String {
        return "start_" + System.currentTimeMillis()
    }

    /**
     * @return 将合并的流用字符保存
     */
    fun getInputCollection(list: ArrayList<String>) {
        // 创建音频文件,合并的文件放这里
        finishFile = File(myRecAudioDir, "merge_" + getTime() + SUFFIX)
        var fileOutputStream: FileOutputStream? = null
        if (!finishFile!!.exists()) {
            try {
                finishFile!!.createNewFile()
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }

        }
        try {
            fileOutputStream = FileOutputStream(finishFile)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        //list里面为暂停录音 所产生的 几段录音文件的名字，中间几段文件的减去前面的6个字节头文件
        var fileInputStream: FileInputStream? = null
        for (i in list.indices) {
            val file = File(list[i] as String)
            Log.d("list的长度", list.size.toString() + "")
            try {
                fileInputStream = FileInputStream(file)
                val myByte = ByteArray(fileInputStream.available())
                //文件长度
                val length = myByte.size
                //头文件
                if (i == 0) {
                    while (fileInputStream.read(myByte) != -1) {
                        fileOutputStream!!.write(myByte, 0, length)
                    }
                } else {
                    //之后的文件，去掉头文件就可以了
                    while (fileInputStream.read(myByte) != -1) {
                        fileOutputStream!!.write(myByte, 6, length - 6)
                    }
                }
                fileOutputStream!!.flush()
                fileInputStream.close()
                println("合成文件长度：" + finishFile!!.length())

            } catch (e: Exception) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            } finally {
                fileInputStream!!.close()
            }
        }
        //结束后关闭流
        try {
            fileOutputStream!!.close()
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
        deleteListRecord()
    }

    private fun deleteListRecord() {
        for (i in list!!.indices) {
            val file = File(list!!.get(i))
            if (file.exists()) {
                file.delete()
            }
        }

    }


}
