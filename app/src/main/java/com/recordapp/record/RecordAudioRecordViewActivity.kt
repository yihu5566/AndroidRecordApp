package com.recordapp.record

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mp3library.MP3Recorder
import com.recordapp.record.view.CircleProgressView
import kotlinx.android.synthetic.main.activity_record_view.*
import java.io.File
import java.io.IOException


class RecordAudioRecordViewActivity : AppCompatActivity(), View.OnClickListener {
    private var circle_progress: CircleProgressView? = null
    private var tv_progress: TextView? = null
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
    var mRecorder: MP3Recorder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_view)
        circle_progress = findViewById(R.id.circle_progress)
        tv_progress = findViewById(R.id.tv_progress)
        textView = findViewById(R.id.tv_play_state)
        initData()
        initListener()
    }

    private fun initData() {
        //初始化list
        list = ArrayList()
        // 判断sd Card是否插入
        sdcardExit = Environment.getExternalStorageState() == android.os.Environment.MEDIA_MOUNTED
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
        btn_play_or_pause.setOnClickListener(this)
        btn_stop.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_play_or_pause -> onRecord()
            R.id.btn_stop -> finishRecord()
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
            try {
                // 初始化录音工具类（mp3library库中），传入录音保存的路径
                mRecorder = MP3Recorder(myRecAudioFile)
                // 开始录音
                mRecorder!!.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            btn_play_or_pause.setBackgroundResource(R.drawable.ic_pause_circle_filled_black_24dp)
            circle_progress!!.startAnimProgress()
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
                        val builder = AlertDialog.Builder(this@RecordAudioRecordViewActivity)
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
        if (mRecorder != null && !isStopRecord) {
            try {
                if (mRecorder != null) {
                    mRecorder!!.stop()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        textView!!.text = "开始录音"
        circle_progress!!.stopRecord()
        //        Toast.makeText(this, R.string.toast_recording_stop, Toast.LENGTH_SHORT).show();
        isStopRecord = true
        btn_play_or_pause.setBackgroundResource(R.drawable.ic_play_circle_filled_black_24dp)
    }

    fun finishRecord() {
        if (isPause) {
            //在暂停状态按下结束键,处理list就可以了
            getInputCollection(list!!)
        } else {
            textView!!.text = "开始录音"
            circle_progress!!.stopRecord()
            btn_play_or_pause.setBackgroundResource(R.drawable.ic_play_circle_filled_black_24dp)
            if (list!!.size > 0) {
                //在正在录音时，处理list里面的和正在录音的语音
                list!!.add(myRecAudioFile!!.path)
                try {
                    if (mRecorder != null) {
                        mRecorder!!.stop()
                        mRecorder = null
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                isStopRecord = true
                //生成处理后的文件
                getInputCollection(list!!)
            } else {
                //若录音没有经过任何暂停
                try {
                    if (mRecorder != null) {
                        mRecorder!!.stop()
                        mRecorder = null
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
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
        try {
            if (mRecorder != null) {
                mRecorder!!.stop()
                mRecorder = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onStop()
    }

    fun getTime(): String {
        return "lame_" + System.currentTimeMillis()
    }

    /**
     * @return 将合并的流用字符保存
     */
    fun getInputCollection(list: ArrayList<String>) {
        // 创建音频文件,合并的文件放这里
        finishFile = File(list[0])
        //list里面为暂停录音 所产生的 几段录音文件的名字，中间几段文件的减去前面的6个字节头文件
        for (i in list.indices) {
            if (i != 0) {
                var heBingMp3 = CaoZuoMp3Utils.heBingMp3(finishFile!!.absolutePath, list[i], getTime())
                finishFile = File(heBingMp3)
            }
            println("合成文件长度：" + finishFile!!.length())
        }
        if (list.size > 1) {
            deleteListRecord()
        }
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
