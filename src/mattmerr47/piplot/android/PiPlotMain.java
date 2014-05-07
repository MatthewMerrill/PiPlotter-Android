package mattmerr47.piplot.android;

import mattmerr47.piplot.io.drawdata.DrawData;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class PiPlotMain extends Activity implements OnClickListener {
	
	public static DrawData drawData;
	
	Button sendData;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		sendData = (Button)findViewById(R.id.main_button_sendData);
		
		
		sendData.setOnClickListener(this);

		// TODO: If exposing deep links into your app, handle intents here.
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == sendData.getId()) {
			Intent cameraActivity = new Intent(getApplicationContext(), CameraActivity.class);
			startActivityForResult(cameraActivity, CAMERA_CODE);
		}
			
	}
	
	int CAMERA_CODE = 111;
	int CLIENT_CODE = 222;
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAMERA_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				Intent clientActivity = new Intent(getApplicationContext(), ClientActivity.class);
				startActivityForResult(clientActivity, CLIENT_CODE);
			}
		}
	}

	/**
	 * Callback method from {@link InputListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
/*	@Override
	public void onItemSelected(String id) {
		if (mTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putString(InputDetailFragment.ARG_ITEM_ID, id);
			InputDetailFragment fragment = new InputDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.input_detail_container, fragment).commit();

		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this, InputDetailActivity.class);
			detailIntent.putExtra(InputDetailFragment.ARG_ITEM_ID, id);
			startActivity(detailIntent);
		}
	}
*/
}
