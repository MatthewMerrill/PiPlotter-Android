package mattmerr47.piplot.android;

import java.io.IOException;

import mattmerr47.piplot.io.Client;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ClientActivity extends Activity {
	
	private Client client;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		client = new Client();

		new SendTask().execute();
	}
	
	class SendTask extends AsyncTask<Void, String, Void> {

		private Button button;
		private TextView label;
		
		@Override
		protected void onPreExecute() {
			setContentView(R.layout.activity_client);
			
			this.button = (Button)findViewById(R.id.client_button_cancel);
			this.label = (TextView)findViewById(R.id.client_text_label);
			
			button.setOnClickListener(new OnClickListener(){
		
				@Override
				public void onClick(View v) {
					try {
						client.stopSending();
						client.disconnectFromServer();
					} catch (IOException e) {
				    	Log.e("PiPlot", "exception", e);
					}
					ClientActivity.this.setResult(Activity.RESULT_CANCELED);
					ClientActivity.this.finish();
				}
				
			});
		}
		
		@Override
	    protected Void doInBackground(Void... v) {
	    	try {
	    		this.publishProgress("Connecting");
				client.connectToServer();
	    		this.publishProgress("Sending DrawData");
				client.addToQueue(PiPlotMain.drawData);
				client.startSending();
	    		this.publishProgress("Sent!");
			} catch (Exception e) {
				
				ClientActivity.this.setResult(Activity.RESULT_CANCELED);
				ClientActivity.this.finish();
				
		    	Log.e("PiPlot", "exception", e);
			}
	    	
	    	return null;
	    }
		
		@Override
	    protected void onProgressUpdate(String... update) {
			if (update.length > 0) {
				label.setText(update[0]);
				
				if (update[0].equalsIgnoreCase("Sent!")) {
					
					button.setText("Okay");
					button.setOnClickListener(new OnClickListener(){
				
						@Override
						public void onClick(View v) {
							ClientActivity.this.setResult(Activity.RESULT_OK);
							ClientActivity.this.finish();
						}
						
					});
				}
			}
	    }
	}
}
