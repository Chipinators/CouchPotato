package edu.wit.mobileapp.testhandle;

import android.content.DialogInterface;
import android.graphics.Region;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public int count = 30;
    private String[] vals = new String[2];
    private int click = 0;
    private boolean host = true;
    private int playerCount = 1;

    private ArrayList<String> pData = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler.postDelayed(runnable, 1000);


        Button button = (Button) findViewById(R.id.sub);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                EditText tmp = (EditText) findViewById(R.id.editText);
                vals[click] = tmp.getText().toString();
                tmp.setText("");
                click++;

                if (click == 2) {
                    findViewById(R.id.sub).setEnabled(false);
                    TextView tv = (TextView) findViewById(R.id.mText);
                    EditText et = (EditText) findViewById(R.id.editText);
                    et.setEnabled(false);
                    tv.setText("Please wait while others submit");
                    tv.invalidate();
                    ;
                }
            }
        });
    }

    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    //region Main Handler
    private Handler handler = new Handler();

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            TextView v = (TextView) findViewById(R.id.counter);
            v.setText("" + count);
            v.invalidate();
            count--;

            if (count != -1) {
                handler.postDelayed(this, 1000);
            } else {
                Button b = (Button) findViewById(R.id.sub);
                b.setEnabled(false);

                byte[] data;
                try{
                    data = ("pID\\|/Questions\\|/" + vals[0] + "\\|/" + vals[1]).getBytes("UTF-8");
                }
                catch(Exception e){
                    data = new byte[1];
                    data[0] = -1;//if issue with converting, send a single negative byte, if received by host,
                                 //nothing will happened and two random points will be set
                }
                parseTimerEnd(data);
            }
        }
    };

    //endregion
    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    public void parseTimerEnd(byte[] data) {
        if (click == 0) {
            //kick
        } else if (click == 1) {
            //set a random value to vals[1];
        } else {

            if (host) {
                try {
                    pData.add(new String(data, "UTF-8"));//add host's data directly
                    //for(int i = 0; i < players.count; i++){
                        //byte[] data = players.get(i).getData();
                        //pData.add(new String(data, "UTF-8");
                    //}
                    if(pData.size() == playerCount){
                        //do the next thing
                        TextView tv = (TextView) findViewById(R.id.mText);
                        tv.setText(pData.get(0));
                        tv.invalidate();
                    }
                }
                catch (Exception e){
                    //FUCK
                }

            } else {
                //send data
            }
        }
    }
}
