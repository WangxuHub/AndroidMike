package dysoft.com.mike;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;


public class MainActivity extends AppCompatActivity {

    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;
    private static final int RECORD_AUDIO = 2;
    private static final int READ_EXTERNAL_STORAGE = 3;
    private static final int DEFAULT_AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    /**
     * 以下三项为默认配置参数。Google Android文档明确表明只有以下3个参数是可以在所有设备上保证支持的。
     */
    private static final int DEFAULT_SAMPLING_RATE = 44100;//模拟器仅支持从麦克风输入8kHz采样率
    private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    /**
     * 下面是对此的封装
     * private static final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
     */

    CheckBox checkBox;
    String path;
    TextView textView;
    String filePath=null;
    boolean isRecording=false;
    private AudioRecord recorder;
    private int bufferSize;
    private short[] buffer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();

        // -----------------------------------------

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        } if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_EXTERNAL_STORAGE);
        }
        else if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO);
        }
        path= Environment.getExternalStorageDirectory().getAbsolutePath();
        //initRecorder();
    }

    private String getFileName() {
        filePath=path+"/"+System.currentTimeMillis()+".mp3";
        return filePath;
    }


    private void findView() {
        textView= (TextView) findViewById(R.id.hint_text);
        checkBox= (CheckBox) this.findViewById(R.id.checkbox);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((CheckBox)view).isChecked()) {
                    startRecord();
                }    else{
                    endRecord();
                }
            }
        });
    }

    private void endRecord() {
        textView.setText("断开连接了!");
        isRecording=false;
    }

    private void startRecord() {

        if (isRecording)return;
        initRecorder();
        recorder.startRecording();
        textView.setText("嗨起来!");
        new Thread(){
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                isRecording = true;
                while (isRecording) {
                    int readSize = recorder.read(buffer, 0,bufferSize);
                    if (readSize > 0) {
                        //此处用socket连接服务器，写入流
                        sendBuffer();
                    }
                }
                // release and finalize audioRecord
                recorder.stop();
                recorder.release();
                recorder = null;
            }
        }.start();
    }

    //发送流给服务器
    private void sendBuffer() {
    }

    private void initRecorder() {
        bufferSize = AudioRecord.getMinBufferSize(DEFAULT_SAMPLING_RATE,
                DEFAULT_CHANNEL_CONFIG, AudioFormat.ENCODING_PCM_16BIT);
        recorder = new AudioRecord(DEFAULT_AUDIO_SOURCE,
                DEFAULT_SAMPLING_RATE, DEFAULT_CHANNEL_CONFIG, AudioFormat.ENCODING_PCM_16BIT,
                bufferSize);
        buffer=new short[bufferSize];
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // mRecorder.stop();
    }


    private void readFile(File file) {
        try {
            FileOutputStream ots=new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
