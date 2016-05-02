package Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.acl.LastOwnerException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;

import MyInterface.FloatViewParamsListener;

public class fileUtils {

    public static Map<String, Map<String, Bitmap>> filemap = new LinkedHashMap<String, Map<String, Bitmap>>();

    public static boolean getImageFile(String fName) {
        boolean re;

        String end = fName
                .substring(fName.lastIndexOf(".") + 1, fName.length())
                .toLowerCase();


        if (end.equals("jpg") || end.equals("gif") || end.equals("png")
                || end.equals("jpeg") || end.equals("bmp")) {
            re = true;
        } else {
            re = false;
        }
        return re;
    }


    public static List<File> getSD() {
        List<File> it = new ArrayList<File>();
        File f = new File(Environment.getExternalStorageDirectory().getPath() + "/dcim/camera");
        File in = new File("/storage/emulated/0/dcim/camera");
        if (f.exists()) {
            Log.i("path", "getSD:目录存在 ");
            if (f.listFiles() != null) {
                Log.i("path", "getSD:目录存在照片 ");
                it = getFileList(f.getAbsolutePath());
                if (it.size() != 0) {
                    Collections.sort(it, new FileComparator());
                    Boolean flag = true;
                    String date;
                }
            }
        } else if (in.exists()) {
            Log.i("path", "本地目录存在 ");
            if (in.listFiles() != null) {
                Log.i("path", "getSD:目录存在照片 ");
                it = getFileList(in.getAbsolutePath());
                if (it.size() != 0) {
                    System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
                    Collections.sort(it, new FileComparator());
                    Boolean flag = true;
                    String date;
                }
            } else {
                Log.i("path", "目录里面内容为空");
            }
        } else {
            Log.i("path", "getSD:目录不存在 ");
        }
        return it;
    }

    /**
     * 通过ContentProvider获取到系统相册中所有相册的图片文件
     * @param context
     * @return
     */
    public static ArrayList<File> getContentprovider(Context context) {
        ArrayList<File> it = new ArrayList<File>();
        Uri mImageUri= MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver=context.getContentResolver();
        Cursor mCursor=contentResolver.query(mImageUri,null,MediaStore.Images.Media.MIME_TYPE+"=? or "+MediaStore.Images.Media.MIME_TYPE+"=?",new String[]{"image/jpeg","image/png"},MediaStore.Images.Media.DATE_MODIFIED);
        if(mCursor==null)
        {
            return null;
        }
        while(mCursor.moveToNext())
        {
            String path=mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
            it.add(new File(path));
            Log.i("gexuelian", path);
        }
        mCursor.close();
        Collections.sort(it, new FileComparator());
        return it;
    }




    public static List<File> getFileList(String strPath) {
        File dir = new File(strPath);
        List<File> filelist = new ArrayList<File>();
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String fileName = files[i].getName();
                if (files[i].isDirectory()) {
                    getFileList(files[i].getAbsolutePath());
                } else if (getImageFile(files[i].getPath())) {
                    String strFileName = files[i].getAbsolutePath();
                    filelist.add(files[i]);
                } else {
                    continue;
                }
            }
        }
        return filelist;
    }


    public static class FileComparator implements Comparator<File> {
        @Override
        public int compare(File lhs, File rhs) {
            if (paishedate(lhs).before(paishedate(rhs)))
                return 1;
            else if(paishedate(lhs).after(paishedate(rhs)))
                return -1;
            else
                return 0;
        }
    }


    public static String lastModifiedTodate(File file) {
        String dddd = null;
        ExifInterface exif;
        Date date1 = null;
        try {
            exif = new ExifInterface(file.getAbsolutePath());
            String date = exif.getAttribute(ExifInterface.TAG_DATETIME);
            try {
                if (!TextUtils.isEmpty(date)) {
                    date1 = dateUtils.ConverToDate(date);
                } else {
                    date1 = dateUtils.ConverToDate("1995:03:13 22:38:20");
                }
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                dddd = df.format(date1);

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return dddd;
    }


    /**
     * 获取照片信息的经纬度和拍摄时间
     * @param list
     * @return
     */
    public static String[] getNeedmoveFileLocation(ArrayList<String> list) {
        String [] information=new String[3];
        ExifInterface exif;
        for(int i=0;i<list.size();i++) {
            try {
                exif = new ExifInterface(list.get(i));
                information[0]=String.valueOf("nothing");
                information[1]=String.valueOf("nothing");
                information[2]=lastModifiedTodate(new File(list.get(i)));
                try {
                  if(!TextUtils.isEmpty(exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)))
                  {
                      final float[] jingweidu={-1,-1};
                      exif.getLatLong(jingweidu);
                      information[0]=String.valueOf(jingweidu[0]);
                      information[1]=String.valueOf(jingweidu[1]);
//                      LogUtils.loggxl("weidu "+exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)+" jindu"+exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE ));
//                      LogUtils.loggxl("weidu "+jingweidu[0]+" jindu"+jingweidu[1]);
                      //这里需要根据经纬度变成城市名
                      return information;
                  }else
                  {
                      LogUtils.loggxl("weidu nothing");
                      continue;
                  }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return information;
    }



//
//    static String getCityname(float jingdu, float weidu)
//    {
//         AMapLocationClient mLocationClient = null;
//         AMapLocationClientOption mLocationOption = null;
//        return null;
//    }






    public static Date paishedate(File file) {
        ExifInterface exif = null;
        Date date1 = null;
        try {
            exif = new ExifInterface(file.getAbsolutePath());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String date = exif.getAttribute(ExifInterface.TAG_DATETIME);
        try {
            if (!TextUtils.isEmpty(date)) {
                date1 = dateUtils.ConverToDate(date);
            } else {
                date1 = dateUtils.ConverToDate("1995:03:13 22:38:20");
            }
            Log.i("date", date);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return date1;
    }


    public static void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) {
                InputStream inStream = new FileInputStream(oldPath);
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread;
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
                deleteFile(oldfile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void deleteFile(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                File files[] = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    deleteFile(files[i]);
                }
            }
            file.delete();
        } else {
        }
    }

    public static void deleteFilelist(ArrayList<String> list) {
        for (int i = 0; i < list.size(); i++) {
            deleteFile(new File(list.get(i)));
        }
    }

    public static void moveFilelist(ArrayList<String> list) {
        for (int i = 0; i < list.size(); i++) {
            moveFile(new File(list.get(i)));
        }
        deleteFilelist(list);
    }

    public static void movePhotolist(ArrayList<String> list) {
        for (int i = 0; i < list.size(); i++) {
            copyFile(new File(list.get(i)).getAbsolutePath(), Environment.getExternalStorageDirectory().getPath() + "/dcim/camera/" + new File(list.get(i)).getName());
        }
        deleteFilelist(list);
    }


    public static void moveFile(File file) {
        if (file.exists()) {
            File files[] = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                copyFile(files[i].getAbsolutePath(), Environment.getExternalStorageDirectory().getPath() + "/dcim/camera/" + files[i].getName());
            }
        }
    }

    public static ArrayList<String> getExistFileList(String path) {
        ArrayList<String> filelist = new ArrayList<String>();
        File dir = new File(path);
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String fileName = files[i].getName();
                if (files[i].isDirectory()) {
                    filelist.add(files[i].getAbsolutePath());
                } else {
                    continue;
                }
            }
        }
        return filelist;
    }


    public static ArrayList<String> getExistImageList(String path) {
        ArrayList<String> filelist = new ArrayList<String>();
        File dir = new File(path);
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String fileName = files[i].getName();
                filelist.add(files[i].getAbsolutePath());
            }
        }
        return filelist;
    }

    /**
     * 获取已经存在的文件夹中的第一个图片的地址
     *
     * @param path
     * @return
     */
    public static String getExistFileBitmap(String path) {
        ArrayList<String> filelist = new ArrayList<String>();
        File dir = new File(path);
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String fileName = files[i].getName();
                if (!files[i].isDirectory() && getImageFile(fileName)) {
                    return files[i].getAbsolutePath();
                } else {
                    continue;
                }
            }
        }
        return null;
    }


}