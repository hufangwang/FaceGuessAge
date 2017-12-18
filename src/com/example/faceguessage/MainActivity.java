package com.example.faceguessage;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.faceguessage.FaceDetect.Callback;
import com.facepp.error.FaceppParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class MainActivity extends Activity implements View.OnClickListener {

    private static final int PICK_CODE=0x110;
    private static final int CAMERA_PHOTO = 0x113;
    private ImageView mPhoto;
    private Button mCamera;
    private Button mGetImage;
    private Button mDetect;
    private TextView mTip;
    private File phoneFile;
    private View mWaiting;
    private Boolean isCamera=false;
    private Boolean isAlbum=false;

    private Bitmap mPhotoImg;
    private String facephoto;

    private String mCurrentPhotoStr;

    private Paint mPaint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        initViews();
        initEvents();
        mPaint=new Paint();

    }

    public void initEvents(){
        mGetImage.setOnClickListener(this);
        mDetect.setOnClickListener(this);
        mCamera.setOnClickListener(this);
    }

    public void initViews()
    {
        mPhoto=(ImageView)findViewById(R.id.id_image);
        mGetImage=(Button)findViewById(R.id.id_getImage);
        mDetect=(Button)findViewById(R.id.id_detect);
        mCamera=(Button)findViewById(R.id.id_camera);
        mTip=(TextView)findViewById(R.id.id_tip);
        mWaiting=findViewById(R.id.id_waiting);
        
    }

    protected void onActivityResult(int requestCode, int resultCode,Intent intent){
        if(requestCode==PICK_CODE)
        {
            if(intent!=null){
                Uri uri=intent.getData();
                Cursor cursor=getContentResolver().query(uri, null, null, null, null);
                cursor.moveToFirst();
                //��ȡͼƬ����
                int idx=cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                mCurrentPhotoStr=cursor.getString(idx);

                cursor.close();
                //ѹ����Ƭ
                resizePhoto(mCurrentPhotoStr);
                mPhoto.setImageBitmap(mPhotoImg);
                mTip.setText("�ҽ��У��J�J");
            }
        }
        else if (requestCode==CAMERA_PHOTO) 
        {
        	Bitmap faceFile = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory()
					+ "/Facephoto.jpg");
        	facephoto=Environment.getExternalStorageDirectory()
					+ "/Facephoto.jpg";
        	if(faceFile!=null){
        		resizePhoto(facephoto);
        		mPhoto.setImageBitmap(mPhotoImg);
        		mTip.setText("�����յ���J�J");
        	}
		}

        super.onActivityResult(requestCode,resultCode,intent);
    }

    //������ѹ��ͼƬ(���)
    private void resizePhoto(String pathName) {

        BitmapFactory.Options options=new BitmapFactory.Options();
        //trueΪ����������ȥ����ͼƬ�������ǿ��Ի�ȡͼƬ�Ŀ����Ϣ�����ߣ�
        options.inJustDecodeBounds=true;
        //��Ҫ�����ͼƬ����
        BitmapFactory.decodeFile(pathName,options);

        //�������ű�
        double ratio=Math.max(options.outWidth*1.0d/1024f,options.outHeight*1.0d/1024f);

        //���ò�����
        options.inSampleSize=(int) Math.ceil(ratio);
        //����ͼƬ
        options.inJustDecodeBounds=false;
        //��ȡ����õ�ͼƬ
        Bitmap decodeFile = BitmapFactory.decodeFile(pathName,options);
        //��ȡ�Ƕ�
        int degree=readPictureDegree(pathName);
        //�����µ����ýǶȵ�ͼƬ���ã��Ƕ�Ϊ��������Ϊ˳ʱ����ת���෴����ʱ�룩
        mPhotoImg = rotaingImageView(degree, decodeFile);
    }
    
    // ��ȡͼƬ���ԣ���ת�ĽǶ� 
	public static int readPictureDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
	 //����������1.tag���ԣ������ȡ���Ƿ��򣩣�2.����Ĭ��ֵ������Ĭ��Ϊ�����Ƕȣ���������
	 //����ֵΪ����ķ���ֵΪ���������Ե����Դ�룩
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) 
			{
			//���orientation��ȡ��Ϊ��ת90�ȣ�����6�������ýǶ�Ϊ90
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			//��ʱ����ת180�ȣ�����3��
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			 //������ת270�ȣ�����8��
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	//��ͼƬ������ת������Ϊ˳ʱ�룬����Ϊ��ʱ�룬�����ƶ�������֮��ģ�������ˣ�
	public static Bitmap rotaingImageView(int angle , Bitmap bitmap) {
        //��תͼƬ ����
		Matrix matrix = new Matrix();;
        matrix.postRotate(angle);
        // �����µ�ͼƬ
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
        		bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		return resizedBitmap;
	}
    
    private static final int MSG_SUCCESS=0x111;
    private static final int MSG_ERROR=0x112;


    private android.os.Handler mHandler=new android.os.Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case MSG_SUCCESS:
                    mWaiting.setVisibility(View.GONE);
                    JSONObject rs=(JSONObject) msg.obj;

                    prePareRsBitmap(rs);

                    mPhoto.setImageBitmap(mPhotoImg);
                    break;
                case MSG_ERROR:
                    mWaiting.setVisibility(View.GONE);

                    String errorMsg=(String) msg.obj;
                    if(TextUtils.isEmpty(errorMsg)){
                        mTip.setText("�������磡");
                    }else{
                        mTip.setText(errorMsg);
                    }
                    break;
            }

            super.handleMessage(msg);
        }
    };
    
    //�������ݣ������Ƶ���ʾ��
    private void prePareRsBitmap(JSONObject rs) 
    {
        Bitmap bitmap=Bitmap.createBitmap(mPhotoImg.getWidth(),mPhotoImg.getHeight(),mPhotoImg.getConfig());

        Canvas canvas=new Canvas(bitmap);
        //���µ�ͼ����
        canvas.drawBitmap(mPhotoImg,0,0,null);

        try {
            JSONArray faces=rs.getJSONArray("face");
            
            //��ȡ����������
            int faceCount=faces.length();

            mTip.setText("��Ⱥ:"+faceCount);

            //ѭ������
            for(int i=0;i<faceCount;i++)
            {
                JSONObject face= faces.getJSONObject(i);
                //ȡ��λ����Ϣ
                JSONObject posObj=face.getJSONObject("position");

                //�������ĵ�����꣨���ص�ֵΪ�ٷֱȣ�x��Ϊռx��İٷֱȣ�y��Ϊռy��İٷֱȣ�
                float x=(float )posObj.getJSONObject("center").getDouble("x");
                float  y=(float )posObj.getJSONObject("center").getDouble("y");
                
                //�����Ŀ�Ⱥ͸߶ȣ����ص�ֵΪ�ٷֱȣ�w��ΪռͼƬ��İٷֱȣ�h��ΪռͼƬ�ߵİٷֱȣ�
                float  w=(float )posObj.getDouble("width");
                float  h=(float )posObj.getDouble("height");

                //���ٷֱ�ת��Ϊ�������ֵ
                x=x/100*bitmap.getWidth();
                y=y/100*bitmap.getHeight();

                w=w/100*bitmap.getWidth();
                h=h/100*bitmap.getHeight();

                //���û�����ɫ�ʹ�ϸ
                mPaint.setColor(0xffffffff);
                mPaint.setStrokeWidth(2);

                //����ס�����������ߣ��ֱ����������Ϻᣬ�������º�
                canvas.drawLine(x-w/2,y-h/2,x-w/2,y+h/2,mPaint);
                canvas.drawLine(x-w/2,y-h/2,x+w/2,y-h/2,mPaint);
                canvas.drawLine(x+w/2,y-h/2,x+w/2,y+h/2,mPaint);
                canvas.drawLine(x-w/2,y+h/2,x+w/2,y+h/2,mPaint);

                //��ȡ������Ա���Ϣ
                int age=face.getJSONObject("attribute").getJSONObject("age").getInt("value");
                String gender=face.getJSONObject("attribute").getJSONObject("gender").getString("value");

                
                //ȡ��TextView�ϵ�ͼƬ
                Bitmap ageBitmap=bulidAgeBitmap(age,"Male".equals(gender));

                int ageWidth=ageBitmap.getWidth();
                int ageHeight=ageBitmap.getHeight();

               /**����ͼƬ��ImageView�Ĵ�С�жϣ���߶�С��ImageView����Ҫ����ʾ�����һ����������С
                *����ͼƬ��Сʱ����ʾ��ͻ��Եúܴ�������ǰ�ͼƬ�Ŵ���ʾ��ImageView�ϣ���ô��ʾ��Ҳ�ᱻ�Ŵ�*/
                if(bitmap.getWidth()<mPhoto.getWidth()&&bitmap.getHeight()<mPhoto.getHeight())
                {
                	//���ű�����ͼƬ�Ŀ�ȳ���imageView�Ŀ�ȣ�
                    float ratio=Math.max(bitmap.getWidth()*1.0f/mPhoto.getWidth(),bitmap.getHeight()*1.0f/mPhoto.getHeight());
                    //��������ͼƬ
                    ageBitmap=Bitmap.createScaledBitmap(ageBitmap,(int)(ageWidth*ratio),(int)(ageHeight*ratio),false);
                }
                
                //�������ֿ�
                canvas.drawBitmap(ageBitmap,x-ageBitmap.getWidth()/2,y-h/2-ageBitmap.getHeight(),null);

                mPhotoImg=bitmap;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

   /**��ȡTextView�е�ͼƬ
    * ��View ����е����ݿ���ͨ��Cache���Ʊ���Ϊbitmap������Ҫͨ��setDrawingCacheEnabled(true)����Cache
    * Ȼ����ͨ�� getDrawingCache() ��ȡbitmap��Դ������ڶ�����ǽ���ȡ��bitmap��Դ���ٴ���һ��bitmap
    * setDrawingCacheEnabled(false)������Cache
    * �������Cache �����Դ˷�ʽ��ȡ�µ�bitmap��Ҫ�����������   ��Ȼ��õ���ʼ����ͬһbitmap��*/
    private Bitmap bulidAgeBitmap(int age, boolean isMale) 
    {
        TextView tv=(TextView)mWaiting.findViewById( R.id.id_age_and_gender);
        tv.setText(age+"");
        if(isMale)
        {
        	//����TextView��ߵ�ͼƬΪŮ��
            tv.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.male),null,null,null);

        }
        else{
        	//����TextView��ߵ�ͼƬΪ����
            tv.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.female),null,null,null);
        }

        //ͬ���Textview�л�ȡͼƬ������cache��
        tv.setDrawingCacheEnabled(true);
        //��TextView�л�ȡͼƬ
        Bitmap bitmap=Bitmap.createBitmap(tv.getDrawingCache());
        //����Cache����ʾ�µ�cache��������ÿ�ζ��ܻ�ȡ��������TextView��ͼƬ��
        tv.destroyDrawingCache();
        //����Ĭ����ʾ����
        tv.setText("11");
        return bitmap;
    }

    //����¼�
    public void onClick(View view) {
        switch (view.getId()){
                case R.id.id_getImage:
                	isCamera=false;
                	isAlbum=true;
                    Intent intent=new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent,PICK_CODE);
                    break;
                case R.id.id_camera:
                	isCamera=true;	
                	isAlbum=false;
                	Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        			phoneFile = new File(Environment.getExternalStorageDirectory()
        					 + "/Facephoto.jpg");
        			intent1.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(phoneFile));
        			startActivityForResult(intent1,CAMERA_PHOTO);
        			break;
                case R.id.id_detect:
                	 //set frame layout as visible
                	if (mPhotoImg!= null) 
                	{
                		 mWaiting.setVisibility(View.VISIBLE);                   
                         if (facephoto!=null&&isCamera) 
                         {
     						resizePhoto(Environment.getExternalStorageDirectory()
     	                			+ "/Facephoto.jpg");
     					}
                         else  if(mCurrentPhotoStr!=null&&!mCurrentPhotoStr.trim().equals("")&&isAlbum)
                         {
                             resizePhoto(mCurrentPhotoStr);
                         }
                         FaceDetect.detect(mPhotoImg,new FaceDetect.Callback(){

                             public void success(JSONObject result) {
                                 Message msg=Message.obtain();
                                 msg.what=MSG_SUCCESS;
                                 msg.obj=result;
                                 mHandler.sendMessage(msg);
                             }

                             public void error(FaceppParseException exeption) {
                                 Message msg=Message.obtain();
                                 msg.what=MSG_ERROR;
                                 msg.obj=exeption.getErrorMessage();
                                 mHandler.sendMessage(msg);
                             }
                         });
					}
                	else 
                	{                     	
  						Toast.makeText(getApplicationContext(), "��ѡ���������Ƭ", -1).show();
  					}
                    break;
            }

    }
}