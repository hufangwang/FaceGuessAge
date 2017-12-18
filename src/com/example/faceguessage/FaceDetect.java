package com.example.faceguessage;

import android.graphics.Bitmap;
import android.util.Log;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;


//����Face��DetectAPI���������
public class FaceDetect 
{

	//���ش�����Ϣ�Ľӿ�
    public interface Callback
    {
        void success(JSONObject result);
        void error(FaceppParseException exeption);
    }


    //���ݹٷ�Ҫ�󣬽�����ͼƬҪΪ���������飬�����������������ͼƬת��Ϊ���������飬������������飬����json����
    public static void detect(final Bitmap bm, final Callback callBack){
        new Thread(new Runnable() {
            public void run() {

                try {
                	//Face����������
                    HttpRequests requests=new HttpRequests(Constant.key,Constant.secertKey,true,true);
                    //����ͼƬ�ĸ���
                    Bitmap bmSmall=Bitmap.createBitmap(bm,0,0,bm.getWidth(),bm.getHeight());
                    
                    //����һ���ֽ����������󣬻�ȡͼƬ�е����ݣ�ת�����ֽ�����
                    ByteArrayOutputStream stream=new ByteArrayOutputStream();
                    
                    //ʹ��compress������ͼƬת��Ϊ�ֽ�
                    bmSmall.compress(Bitmap.CompressFormat.JPEG,100,stream);
                    
                    //�����ݴ������ֽ�������
                    byte[] array=stream.toByteArray();
                    
                    //���ֽ��������params
                    PostParameters params=new PostParameters();
                    params.setImg(array);
                    //����������ݣ�����һ��json����
                    JSONObject jsonObject=requests.detectionDetect(params);


                    Log.e("TAG",jsonObject.toString());
                    //���ݽ����ɹ������÷��ش�����Ϣ�Ľӿ�
                    if(callBack!=null)
                    {
                    	//����json����
                        callBack.success(jsonObject);
                    }
                } catch (FaceppParseException e) {
                    e.printStackTrace();

                  //���ݽ���ʧ�ܣ����÷��ش�����Ϣ�Ľӿ�
                    if(callBack!=null)
                    {
                    	//���뷢���쳣����Ϣ
                        callBack.error(e);
                    }
                }

            }
        }).start();
    }
}
