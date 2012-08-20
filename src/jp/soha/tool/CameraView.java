package jp.soha.tool;

import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback,
		Camera.PictureCallback, Camera.AutoFocusCallback {
	private SurfaceHolder surfaceHolder;
	private Camera camera;
	private static ContentResolver contentResolver = null;

	private Uri img_uri;

	public CameraView(Context context) {
		super(context);

		surfaceHolder = getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		contentResolver = context.getContentResolver();
	}

	public void surfaceCreated(SurfaceHolder holder) {
		camera = Camera.open();
		setCameraParameters(camera);
		try {
			camera.setPreviewDisplay(holder);
		} catch (Exception e) {
			cameraRelease();
		}
	}

	public void onAutoFocus( boolean success, Camera camera ){
		camera.autoFocus( null );	//　オートフォーカスとめる
		camera.takePicture(null, null, this);
	}	
	
	private void setCameraParameters(Camera camera) {
		boolean portrait = isPortrait();
		if(portrait) {
			camera.setDisplayOrientation(90);
		}else{
			camera.setDisplayOrientation(0);
		}
		
		Parameters parameters = camera.getParameters();
		//int length = parameters.getSupportedPictureSizes().size();
		List<Size> sizeList = parameters.getSupportedPictureSizes();
//		for(Size size : sizeList) {
//			if(size.width <= 2048) {
				Size size = sizeList.get(13);
				parameters.setPictureSize(size.width, size.height); // Default:2048x1536
				camera.setParameters(parameters);
//				break;
//			}
//		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		cameraRelease();
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Parameters parameters = camera.getParameters();
		//parameters.setPreviewSize(width, height);
		boolean portrait = isPortrait();
		if(portrait) {
			camera.setDisplayOrientation(90);
		}else{
			camera.setDisplayOrientation(0);
		}
		List<Size> sizeList = parameters.getSupportedPreviewSizes();
		Size size = sizeList.get(3);
		parameters.setPreviewSize(size.width, size.height); // Default:2048x1536
		//parameters.setPreviewSize(width, height);
		camera.setParameters(parameters);
		camera.startPreview();
	}

	protected boolean isPortrait() {
	    return (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent e) {
		if (e.getAction() == MotionEvent.ACTION_DOWN) {
			//camera.takePicture(null, null, this);
			camera.autoFocus(this);
		}
		return true;
	}

	public void onPictureTaken(byte[] data, Camera camera) {
		try {
			String dataName = "photo_"
					+ String.valueOf(Calendar.getInstance().getTimeInMillis())
					+ ".jpg";
			// saveDataToSdCard(data, dataName);
			saveDataToSdCard(data, dataName);
		} catch (Exception e) {
			cameraRelease();
		}
		camera.startPreview();
	}

	private void saveDataToSdCard(byte[] data, String dataName)
			throws Exception {
		FileOutputStream fileOutputStream = null;
		try {
			//String sdcardPath = Environment.getExternalStorageDirectory().getCanonicalPath();
			String sdcardPath = "/sdcard2/MyCamera";
			fileOutputStream = new FileOutputStream(sdcardPath + "/" + dataName);
			fileOutputStream.write(data);
		} catch (Exception e) {
			cameraRelease();
		} finally {
			if (fileOutputStream != null) {
				fileOutputStream.close();
			}
		}
	}

//	private void saveDataToURI(byte[] data, String dataName) {
//		Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//		ContentValues values = new ContentValues();
//		values.put(Media.DISPLAY_NAME, dataName);
//		values.put(Media.DESCRIPTION, "taken with IceNow");
//		values.put(Media.MIME_TYPE, "image/jpeg");
//		Uri uri = contentResolver.insert(Media.EXTERNAL_CONTENT_URI, values);
//		try {
//			OutputStream outStream = contentResolver.openOutputStream(uri);
//			bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outStream);
//			outStream.close();
//		} catch (Exception e) {
//			cameraRelease();
//		}
//
//		this.img_uri = uri;
//		confirm_fileupload();
//	}

	private void cameraRelease() {
		if (camera != null) {
			camera.release();
			camera = null;
		}
	}

	public void onResume() {
		cameraRelease();
	}

	public void onPause() {
		cameraRelease();
	}

//	/**
//	 * 撮影した写真をアップロードするか確認する
//	 */
//	private void confirm_fileupload() {
//		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this
//				.getContext());
//		alertDialogBuilder.setTitle("タイトル");
//		alertDialogBuilder.setMessage("メッセージ");
//		alertDialogBuilder.setPositiveButton("送信",
//				new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int which) {
//						UploadFile();
//					}
//				});
//		alertDialogBuilder.setNegativeButton("送信しない",
//				new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int which) {
//					}
//					// DO NOTHING
//				});
//		alertDialogBuilder.setCancelable(true);
//		AlertDialog alertDialog = alertDialogBuilder.create();
//		alertDialog.show();
//	}
//
//	/**
//	 * 撮影した写真をアップロード
//	 * 
//	 * @param FileName
//	 */
//	private void UploadFile() {
//		new UploadFileTask(this.getContext()).execute(this.img_uri);
//		Toast.makeText(this.getContext(), "now uploading", Toast.LENGTH_LONG)
//				.show();
//
//	}
//
//	private class UploadFileTask extends AsyncTask<Uri, Integer, Exception> {
//
//		Context context;
//		//private ProgressDialog progressDialog = null;
//
//		public UploadFileTask(Context context) {
//			this.context = context;
//		}
//
//		protected Exception doInBackground(Uri... image_uris) {
//			Uri uri = image_uris[0];
//
//			 int respcode = 0;
//			 // タイトル作成
//			 String title = URLEncoder.encode("あいすなぅ");
//			 // 更新日時
//			 Date nowTime = Calendar.getInstance().getTime();
//			 String timestamp = nowTime + "更新";
//			 timestamp = URLEncoder.encode(timestamp);
//
//			try {
//				 String lineEnd = "\r\n";
//				 String twoHyphens = "--";
//				 String boundary = "*****MultiPartBoundary******";
//				
//				 InputStream filein = contentResolver.openInputStream(uri);
//				
//				 BufferedInputStream fileInputStream = new
//				 BufferedInputStream(
//				 filein);
//				
//				 String UPLOADSCRIPT_URL =
//				 "http://gotenyamaisland.appspot.com/add";
//				
//				 URL url = new URL(UPLOADSCRIPT_URL);
//				 HttpURLConnection conn = (HttpURLConnection) url
//				 .openConnection();
//				
//				 conn.setDoInput(true);
//				 conn.setDoOutput(true);
//				 conn.setUseCaches(false);
//				
//				 conn.setRequestMethod("POST");
//				 conn.setRequestProperty("User-Agent", "IceNowCamera");
//				 conn.setRequestProperty("Content-Type",
//				 "multipart/form-data;boundary=" + boundary);
//				 conn.connect();
//				
//				 PrintStream ps = new PrintStream(conn.getOutputStream());
//				
//				 // boundary start
//				 ps.print(twoHyphens + boundary + lineEnd);
//				 ps
//				 .print("Content-Disposition: form-data;name=\"photo\";filename=\""
//				 + img_uri + "\"" + lineEnd);
//				 ps.print(lineEnd);
//				
//				 int bytesAvailable;
//				
//				 while ((bytesAvailable = fileInputStream.available()) > 0) {
//				 int bufferSize = Math.min(bytesAvailable, 4096);
//				 byte[] buffer = new byte[bufferSize];
//				 int bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//				 ps.write(buffer, 0, bytesRead);
//				 }
//				
//				 // boundary end
//				 ps.print(twoHyphens + boundary + lineEnd);
//				 fileInputStream.close();
//				
//				 // boundary start
//				 ps.print(twoHyphens + boundary + lineEnd);
//				 ps.print("Content-Disposition: form-data; name=\"title\""
//				 + lineEnd);
//				 ps.print(lineEnd);
//				
//				 // write body
//				 ps.print(title + lineEnd);
//				
//				 // boundary end and start
//				 ps.print(twoHyphens + boundary + lineEnd);
//				
//				 String comment = timestamp;
//				 ps.print("Content-Disposition: form-data; name=\"comment\""
//				 + lineEnd);
//				 ps.print(lineEnd);
//				
//				 // write body
//				 ps.print(comment + lineEnd);
//				
//				 // boundary finally end
//				 ps.print(twoHyphens + boundary + twoHyphens + lineEnd);
//				
//				 ps.flush();
//				 ps.close();
//				
//				 // DataInputStream dis = new
//				 // DataInputStream(conn.getInputStream());
//				 BufferedReader in = new BufferedReader(new InputStreamReader(
//				 conn.getInputStream()));
//				 in.close();
//				 respcode = conn.getResponseCode();
////				 String res_str = conn.getResponseMessage();
//				 conn.disconnect();
//			} catch (Exception e) {
//				Log.e("IceNowCamera", e.getMessage());
//				return e;
//			}
//			 if (respcode == 302) {
//			 return null;
//			 }
//			 return new Exception("respcodeerr:" + respcode);
//		}
//
//		protected void onPostExecute(Exception resulterr) {
//			super.onPostExecute(resulterr);
//			 if (resulterr == null) {
//				Toast.makeText(context, "送信しました。", Toast.LENGTH_LONG).show();
//			 } else {
//				Toast.makeText(context,
//						 "エラーが発生しました。\n" + resulterr.getMessage(),
//				Toast.LENGTH_LONG).show();
//			 }
//		}
//
//		protected void onPreExecute() {
////			progressDialog = new ProgressDialog(context);
////			progressDialog.setTitle("progress title");
////			progressDialog.setIndeterminate(true);
////			progressDialog.show();
//		}
//
//		protected void onProgressUpdate(Integer... progress) {
////			progressDialog.setProgress(progress[0]);
//		}
//	}
}
