package mattmerr47.piplot.android;

import java.util.ArrayList;
import java.util.List;

import mattmerr47.piplot.io.drawdata.PathData;
import mattmerr47.piplot.io.path.Line;
import mattmerr47.piplot.io.path.Path;
import mattmerr47.piplot.io.path.Point;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class CameraActivity extends Activity implements OnClickListener, OnSeekBarChangeListener {
	
	private static final int CAMERA_REQUEST = 1888;
	
	private ImageView rawView;
	private ImageView preView;
	
	Button photoButton;
	Button doneButton;
	
	TextView textBlur;
	TextView textHigh;
	TextView textLow;
	
	SeekBar blurBar;
	SeekBar highBar;
	SeekBar lowBar;
	
	private static int percBlur = 0;
	private static int percHigh = 0;
	private static int percLow = 0;
	
	private PathData pathData;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
		
		
		//IMAGEVIEWS
		
		rawView = (ImageView)findViewById(R.id.camera_image_raw);
		preView = (ImageView)findViewById(R.id.camera_image_preview);
		
		rawView.setScaleType(ScaleType.FIT_CENTER);
		preView.setScaleType(ScaleType.FIT_CENTER);
		
		if (image != null)
			rawView.setImageBitmap(image);
		if (image2 != null)
			preView.setImageBitmap(image2);
		
		
		//BUTTONS
		
		photoButton = (Button) this.findViewById(R.id.camera_button_getImage);
		photoButton.setOnClickListener(this);
		
		doneButton = (Button) this.findViewById(R.id.camera_button_send);
		doneButton.setEnabled(false);
		doneButton.setOnClickListener(this);
		
		
		// TEXTVIEWS
		
		textBlur = (TextView)findViewById(R.id.camera_text_blur);
		textHigh = (TextView)findViewById(R.id.camera_text_high);
		textLow = (TextView)findViewById(R.id.camera_text_low);
		
		
		//SEEKBARS
		
		blurBar = (SeekBar)findViewById(R.id.camera_seekbar_blur);
		highBar = (SeekBar)findViewById(R.id.camera_seekbar_high);
		lowBar = (SeekBar)findViewById(R.id.camera_seekbar_low);
		
		blurBar.setOnSeekBarChangeListener(this);
		highBar.setOnSeekBarChangeListener(this);
		lowBar.setOnSeekBarChangeListener(this);
		
		blurBar.setMax(50);
		highBar.setMax(100);
		lowBar.setMax(100);
		
		if (percBlur >= 0)
			blurBar.setProgress((int)(percBlur * blurBar.getMax()));
		if (percHigh >= 0)
			highBar.setProgress((int)(percHigh * highBar.getMax()));
		if (percLow >= 0)
			lowBar.setProgress((int)(percLow * lowBar.getMax()));
		
		updateTexts();
	}
	
	private static Bitmap image;
	private static Bitmap image2;
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
		if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {  
			image = (Bitmap) data.getExtras().get("data");  
			image2 = (Bitmap) data.getExtras().get("data");
			rawView.setImageBitmap(image);
			preView.setImageBitmap(image2);
			
			update();
		}  
	} 
	
	public void update() {
		
		try {
			
			Mat mat = new Mat (image.getWidth(), image.getHeight(), CvType.CV_8UC1);
			Utils.bitmapToMat(image, mat);
			
			Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
			Imgproc.GaussianBlur(mat, mat, new Size( (percBlur%2==1)?(percBlur):(percBlur+1), (percBlur%2==1)?(percBlur):(percBlur+1)), 50);
			Imgproc.Canny(mat, mat, percHigh, percLow);
	
			List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
			Mat hierarchy = new Mat();
			Imgproc.findContours(mat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
			
			ArrayList<Line> lines = new ArrayList<Line>();
			double width = mat.width();
			double height = mat.height();
			
			double scale = 1;
			
			if (width > 8) {
				scale = 8 / width;
			}
			if (height * scale > 10) {
				scale = 10 / (height);
			}
			
			for (int i = 0; i < contours.size(); i++) {
				MatOfPoint c = contours.get(i);

				List<org.opencv.core.Point> points = c.toList();
				//Log.i("CHANNELS", c.size() + " * " + c.channels());
				for (int j = 0; j+1 < points.size(); j++) {
					
					Point p1 = new Point(points.get(j).x * scale - (4.0), points.get(j).y * scale);
					Point p2 = new Point(points.get(j+1).x * scale - (4.0), points.get(j+1).y * scale);

					Log.i("asdf: " + i + ":" + j, "(" + p1.X + ", " + p1.Y + ")");
					if (j+2 == c.rows())
						Log.i("asdf: " + i + ":" + (j+1), "(" + p2.X + ", " + p2.Y + ")");
					
					lines.add(new Line(p1, p2));
				}
				
				Imgproc.drawContours(mat, contours, i, new Scalar(128,32,128));
				Utils.matToBitmap(mat, image2);
				preView.setImageBitmap(image2);
			}
			
			final Path[] paths = new Path[lines.size()];
			for (int i = 0; i < lines.size(); i++)
				paths[i] = lines.get(i);
			
			pathData = new PathData(width, height, paths);
			
			doneButton.setEnabled(true);
		
		} catch (Exception e) {
		
			this.setResult(Activity.RESULT_CANCELED);
			this.finish();
			
	    	Log.e("PiPlot", "exception", e);
		}
	}
	
	private void updateTexts() {
		textBlur.setText("Blur: "+ ((percBlur%2==1)?(percBlur):(percBlur+1)) );
		textHigh.setText("High: "+percHigh);
		textLow.setText("Low: "+percLow);
	}
	
	private void send() {
		if (pathData != null) {
			PiPlotMain.drawData = pathData;
			
			this.setResult(Activity.RESULT_OK);
			this.finish();
		}
	}
	
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
				case LoaderCallbackInterface.SUCCESS:
				{
					Toast.makeText(getApplicationContext(), "OpenCV loaded successfully", Toast.LENGTH_LONG).show();
					//mOpenCvCameraView.enableView();
				} break;
				default:
				{
					super.onManagerConnected(status);
				} break;
			}
		}
	};
	
	@Override
	public void onResume()
	{
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.camera_button_getImage) {
			Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE); 
			startActivityForResult(cameraIntent, CAMERA_REQUEST); 
		} else if (v.getId() == R.id.camera_button_send) {
			send();
		}
	}

	int moving = 0;
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		moving++;
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		moving--;
		
		if (moving > 0) {
			Log.i("asdf", moving + "");
			return;
		}
		
		int perc = seekBar.getProgress();
		if (seekBar.getId() == R.id.camera_seekbar_blur) {
			percBlur = perc;
			blurBar.setProgress(perc);
		} else if (seekBar.getId() == R.id.camera_seekbar_high) {
			percHigh = perc;
			highBar.setProgress(perc);
		} else if (seekBar.getId() == R.id.camera_seekbar_low) {
			percLow = perc;
			lowBar.setProgress(perc);
		}
		
		updateTexts();
		update();
	}
}