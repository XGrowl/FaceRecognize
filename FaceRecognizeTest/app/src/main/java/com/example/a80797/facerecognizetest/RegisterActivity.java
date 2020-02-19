package com.example.a80797.facerecognizetest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.Face3DAngle;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.FaceSimilar;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.VersionInfo;
import com.arcsoft.face.enums.CompareModel;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;
import com.arcsoft.face.enums.DetectModel;
import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.arcsoft.imageutil.ArcSoftImageUtilError;
import com.bumptech.glide.Glide;
import com.ddz.floatingactionbutton.FloatingActionButton;
import com.example.a80797.facerecognizetest.camera.CameraHelper;
import com.guo.android_extend.widget.ExtImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = RegisterActivity.class.getSimpleName();


    private String mFilePath;//人脸图片的文件路径
    private FaceEngine faceEngine;
    private int faceEngineCode = -1;
    private ImageView mImageView;
    //    用于显示照片
    private Bitmap mBitmap;
    private Bitmap temp;
    private Rect src = new Rect();
    //这个是将人脸加个框框标记出来
    private Rect dst = new Rect();
    private EditText mEditText;
    private ImageView FaceImageView;
    //    将src圈出的人脸按比例缩小给显示出来
  private   Bitmap face_bitmap;
  private FloatingActionButton  addFaceFloatBtn;
    private AlertDialog mDialog;
private UIHandler mUIHandler=new UIHandler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        if (!getIntentData(getIntent().getExtras())) {
            Log.d(TAG, "getData fail ");
            this.finish();
        }
        initEngine();
        initView();
        processFaceImage();

    }

    private void initView() {
        mBitmap = CameraHelper.decodeImage(mFilePath);
  //      src.set(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
        mImageView = findViewById(R.id.face_register_image);
        addFaceFloatBtn=findViewById(R.id.add_face);
//        Glide.with(mImageView.getContext())
//                .load(mBitmap)
//                .into(mImageView);
        Log.e(TAG, "initView: bitmap"+mBitmap.toString()  );
        temp=mBitmap;
        addFaceFloatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popRegisterMessage();
            }
        });
    }

    private boolean getIntentData(Bundle bundle) {
        mFilePath = bundle.getString("imagePath");
        if (mFilePath == null || mFilePath.isEmpty()) {
            return false;
        }
        Log.e(TAG, "getIntentData: " + mFilePath);
        return true;

    }

    void processFaceImage() {
//        Observable.create(new ObservableOnSubscribe<Object>() {
//            @Override
//            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
//                processImage();
//                emitter.onComplete();
//
//            }
//        })
//                .subscribeOn(Schedulers.computation())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<Object>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//
//                    }
//
//                    @Override
//                    public void onNext(Object o) {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                });
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                processImage();
            }
        });
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                processImage();
//            }
//        }).start();

    }

    private void initEngine() {
        faceEngine = new FaceEngine();
        faceEngineCode = faceEngine.init(this, DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_ALL_OUT,
                16, 10, FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_FACE_DETECT | FaceEngine.ASF_AGE | FaceEngine.ASF_GENDER | FaceEngine.ASF_FACE3DANGLE | FaceEngine.ASF_LIVENESS);
        VersionInfo versionInfo = new VersionInfo();
        faceEngine.getVersion(versionInfo);
        Log.i(TAG, "initEngine: init: " + faceEngineCode + "  version:" + versionInfo);

        if (faceEngineCode != ErrorInfo.MOK) {
            Toast.makeText(this, getString(R.string.init_failed, faceEngineCode), Toast.LENGTH_LONG).show();
        }
    }

    public void processImage() {
        /**
         * 1.准备操作（校验，显示，获取BGR）
         */
        if (mBitmap == null) {
            mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.faces);
        }
        // 图像对齐
        Bitmap bitmap = ArcSoftImageUtil.getAlignedBitmap(mBitmap, true);

        final SpannableStringBuilder notificationSpannableStringBuilder = new SpannableStringBuilder();
        if (faceEngineCode != ErrorInfo.MOK) {
            Toast.makeText(this, " face engine not initialized!", Toast.LENGTH_LONG).show();
            return;
        }
        if (bitmap == null) {
            Toast.makeText(this, " bitmap is null!", Toast.LENGTH_LONG).show();
            return;
        }
        if (faceEngine == null) {
            Toast.makeText(this, " faceEngine is null!", Toast.LENGTH_LONG).show();

            return;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        final Bitmap finalBitmap = bitmap;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Glide.with(mImageView.getContext())
                        .load(finalBitmap)
                        .into(mImageView);
            }
        });

        // bitmap转bgr24
        long start = System.currentTimeMillis();
        byte[] bgr24 = ArcSoftImageUtil.createImageData(bitmap.getWidth(), bitmap.getHeight(), ArcSoftImageFormat.BGR24);
        int transformCode = ArcSoftImageUtil.bitmapToImageData(bitmap, bgr24, ArcSoftImageFormat.BGR24);
        if (transformCode != ArcSoftImageUtilError.CODE_SUCCESS) {
            Log.e(TAG, "transform failed, code is " + transformCode);
            return;
        }
//        Log.i(TAG, "processImage:bitmapToBgr24 cost =  " + (System.currentTimeMillis() - start));
        List<FaceInfo> faceInfoList = new ArrayList<>();

        /**
         * 2.成功获取到了BGR24 数据，开始人脸检测
         */
        long fdStartTime = System.currentTimeMillis();
//        ArcSoftImageInfo arcSoftImageInfo = new ArcSoftImageInfo(width,height,FaceEngine.CP_PAF_BGR24,new byte[][]{bgr24},new int[]{width * 3});
//        Log.i(TAG, "processImage: " + arcSoftImageInfo.getPlanes()[0].length);
//        int detectCode = faceEngine.detectFaces(arcSoftImageInfo, faceInfoList);
        int detectCode = faceEngine.detectFaces(bgr24, width, height, FaceEngine.CP_PAF_BGR24, DetectModel.RGB, faceInfoList);
        if (detectCode == ErrorInfo.MOK) {
//            Log.i(TAG, "processImage: fd costTime = " + (System.currentTimeMillis() - fdStartTime));
        }

        //绘制bitmap
        Bitmap bitmapForDraw = bitmap.copy(Bitmap.Config.RGB_565, true);
        Canvas canvas = new Canvas(bitmapForDraw);
        Paint paint = new Paint();
        /*
         * 3.若检测结果人脸数量大于0，则在bitmap上绘制人脸框并且重新显示到ImageView，若人脸数量为0，则无法进行下一步操作，操作结束
         */
        if (faceInfoList.size() > 0) {

            paint.setAntiAlias(true);
            paint.setStrokeWidth(5);
            paint.setColor(Color.YELLOW);
            for (int i = 0; i < faceInfoList.size(); i++) {
                //绘制人脸框
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawRect(faceInfoList.get(i).getRect(), paint);
                //绘制人脸序号
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                int textSize = faceInfoList.get(i).getRect().width() / 2;
                paint.setTextSize(textSize);

                canvas.drawText(String.valueOf(i), faceInfoList.get(i).getRect().left, faceInfoList.get(i).getRect().top, paint);

            }
            //显示
            final Bitmap finalBitmapForDraw = bitmapForDraw;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Glide.with(mImageView.getContext())
                            .load(finalBitmapForDraw)
                            .into(mImageView);
                }
            });
        } else {

            return;
        }


        /**
         * 将图片内的所有人脸进行一一比对并添加到提示文字中
         */
        if (faceInfoList.size() > 0) {

            FaceFeature[] faceFeatures = new FaceFeature[faceInfoList.size()];
            int[] extractFaceFeatureCodes = new int[faceInfoList.size()];


            for (int i = 0; i < faceInfoList.size(); i++) {
                faceFeatures[i] = new FaceFeature();
                //从图片解析出人脸特征数据

                long frStartTime = System.currentTimeMillis();
                extractFaceFeatureCodes[i] = faceEngine.extractFaceFeature(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList.get(i), faceFeatures[i]);

            }

        }


        int face_width=faceInfoList.get(0).getRect().width();
        int face_height=faceInfoList.get(0).getRect().height();
        Log.e(TAG, "processImage: +top"+faceInfoList.get(0).getRect().top );
        Log.e(TAG, "processImage: +left"+faceInfoList.get(0).getRect().left );
       face_bitmap=Bitmap.createBitmap(temp,faceInfoList.get(0).getRect().left,faceInfoList.get(0).getRect().top,face_width,face_height);
        Log.e(TAG, "processImage: face_bitmap"+face_bitmap.toString() );
//        Canvas face_canvas=new Canvas(face_bitmap);
//        face_canvas.drawBitmap(mBitmap,faceInfoList.get(0).getRect(),new Rect(0,0,face_width,face_height),null);
//        Message reg = Message.obtain();
//        reg.what = MSG_CODE;
//        reg.arg1 = MSG_EVENT_REG;
//        reg.obj = face_bitmap;
     //   mUIHandler.sendMessage(null);

//        popRegisterMessage();



    }
    class UIHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            {

                Log.e(TAG, "popRegisterMessage: init" );
                LayoutInflater inflater = LayoutInflater.from(RegisterActivity.this);
                View layout = inflater.inflate(R.layout.dialog_register, null);
                mEditText = (EditText) layout.findViewById(R.id.editview);
                mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});//限制最长字数
                FaceImageView = layout.findViewById(R.id.face_imageView);
                FaceImageView.setImageBitmap(mBitmap);
                final Bitmap face = face_bitmap;
                // TODO: 2020/2/18 人脸注册
                //  ((MyApplication) RegisterActivity.this.getApplicationContext()).mFaceDB.addFace(mEditText.getText().toString(), mAFR_FSDKFace, face);
//                                    try {
//                                        byte[] bytes = mAFR_FSDKFace.getFeatureData();
//                                        dsssss = new String(mAFR_FSDKFace.getFeatureData(),"ISO_8859_1");
//                                        byte[] myBytes = dsssss.getBytes("ISO_8859_1");
//                                        Log.i("dsa","dsa");
//                                    } catch (UnsupportedEncodingException e) {
//                                        e.printStackTrace();
//                                    }
//
//                                    preferencesService.save(dsssss,mEditText.getText().toString());
//                                    mRegisterViewAdapter.notifyDataSetChanged();
//                            网络请求
                mDialog = new AlertDialog.Builder(RegisterActivity.this)
                        .setTitle("请输入注册名字")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setView(layout)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //  ((MyApplication) RegisterActivity.this.getApplicationContext()).mFaceDB.addFace(mEditText.getText().toString(), mAFR_FSDKFace, face);



                                //                                    try {
                                //                                        byte[] bytes = mAFR_FSDKFace.getFeatureData();
                                //                                        dsssss = new String(mAFR_FSDKFace.getFeatureData(),"ISO_8859_1");
                                //                                        byte[] myBytes = dsssss.getBytes("ISO_8859_1");
                                //                                        Log.i("dsa","dsa");
                                //                                    } catch (UnsupportedEncodingException e) {
                                //                                        e.printStackTrace();
                                //                                    }
                                //
                                //                                    preferencesService.save(dsssss,mEditText.getText().toString());
                                //                                    mRegisterViewAdapter.notifyDataSetChanged();




                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //                            网络请求
                                    }
                                }).start();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        }
    }
//    PreferencesService preferencesService = new PreferencesService(this);
void popRegisterMessage() {

    Log.e(TAG, "popRegisterMessage: init" );
    LayoutInflater inflater = LayoutInflater.from(RegisterActivity.this);
    View layout = inflater.inflate(R.layout.dialog_register, null);
    mEditText = (EditText) layout.findViewById(R.id.editview);
    mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});//限制最长字数
    FaceImageView = layout.findViewById(R.id.face_imageView);
    Log.e(TAG, "popRegisterMessage: face_bitmap"+face_bitmap.toString() );
    Log.e(TAG, "popRegisterMessage: bitmap"+mBitmap.toString() );
    Glide.with(FaceImageView.getContext())
            .load(face_bitmap)
            .into(FaceImageView);
  //  FaceImageView.setImageBitmap(temp);
    final Bitmap face = face_bitmap;
    // TODO: 2020/2/18 人脸注册
    //  ((MyApplication) RegisterActivity.this.getApplicationContext()).mFaceDB.addFace(mEditText.getText().toString(), mAFR_FSDKFace, face);
//                                    try {
//                                        byte[] bytes = mAFR_FSDKFace.getFeatureData();
//                                        dsssss = new String(mAFR_FSDKFace.getFeatureData(),"ISO_8859_1");
//                                        byte[] myBytes = dsssss.getBytes("ISO_8859_1");
//                                        Log.i("dsa","dsa");
//                                    } catch (UnsupportedEncodingException e) {
//                                        e.printStackTrace();
//                                    }
//
//                                    preferencesService.save(dsssss,mEditText.getText().toString());
//                                    mRegisterViewAdapter.notifyDataSetChanged();
//                            网络请求
    mDialog = new AlertDialog.Builder(RegisterActivity.this)
             .setTitle("请输入注册名字")
             .setIcon(android.R.drawable.ic_dialog_info)
             .setView(layout)
             .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     //  ((MyApplication) RegisterActivity.this.getApplicationContext()).mFaceDB.addFace(mEditText.getText().toString(), mAFR_FSDKFace, face);



 //                                    try {
 //                                        byte[] bytes = mAFR_FSDKFace.getFeatureData();
 //                                        dsssss = new String(mAFR_FSDKFace.getFeatureData(),"ISO_8859_1");
 //                                        byte[] myBytes = dsssss.getBytes("ISO_8859_1");
 //                                        Log.i("dsa","dsa");
 //                                    } catch (UnsupportedEncodingException e) {
 //                                        e.printStackTrace();
 //                                    }
 //
 //                                    preferencesService.save(dsssss,mEditText.getText().toString());
 //                                    mRegisterViewAdapter.notifyDataSetChanged();




                     new Thread(new Runnable() {
                         @Override
                         public void run() {
 //                            网络请求
                         }
                     }).start();
                     dialog.dismiss();
                 }
             })
             .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     dialog.dismiss();
                 }
             })
             .show();
}
    String dsssss = null;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDialog.dismiss();
    }
}
