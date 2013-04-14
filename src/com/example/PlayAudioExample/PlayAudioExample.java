package com.example.PlayAudioExample;
 
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Random;
 
import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
 
public class PlayAudioExample extends ListActivity {
    private static final int UPDATE_FREQUENCY = 1;
    private static final int STEP_VALUE = 4000;
    
    private TextView time = null;
    private TextView ttime = null;
    private Random newrand;
    private ToggleButton repeat;
    private boolean rpt;
    private ToggleButton shuffle;
    private boolean mxd;
    private TextView Tracks = null;
    private MediaCursorAdapter mediaAdapter = null;
    private TextView selelctedFile = null;
    private SeekBar seekbar = null;
    private MediaPlayer player = null;
    private ImageButton playButton = null;
    private ImageButton prevButton = null;
    private ImageButton nextButton = null;
    private boolean isStarted = true;
    private String currentFile = "";
    private int pos = -1;
    private int size_list = 0;
    private String size_listSTR = null;
    private boolean isMoveingSeekBar = false;
     
    private final Handler handler = new Handler();
     
    private final Runnable updatePositionRunnable = new Runnable() {
            public void run() {
                    updatePosition();
            }
    };
     
    @Override
    public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
     
    newrand = new Random();
    shuffle = (ToggleButton)findViewById(R.id.shuffle);
    repeat = (ToggleButton)findViewById(R.id.repeat);
    Tracks = (TextView)findViewById(R.id.tracks);
    time = (TextView)findViewById(R.id.time);
    ttime = (TextView)findViewById(R.id.ttime);
    selelctedFile = (TextView)findViewById(R.id.selectedfile);
    seekbar = (SeekBar)findViewById(R.id.seekbar);
    playButton = (ImageButton)findViewById(R.id.play);
    prevButton = (ImageButton)findViewById(R.id.prev);
    nextButton = (ImageButton)findViewById(R.id.next);
    
    repeat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
               rpt=true; // The toggle is enabled
            } else {
               rpt=false; // The toggle is disabled
            }
        }
    });
    shuffle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
               mxd=true; // The toggle is enabled
            } else {
               mxd=false; // The toggle is disabled
            }
        }
    });
    
    player = new MediaPlayer();
     
    player.setOnCompletionListener(onCompletion);
    player.setOnErrorListener(onError);
    seekbar.setOnSeekBarChangeListener(seekBarChanged);
      
    
    Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
    
    size_list = cursor.getCount();
    
    if(null != cursor)
    {
            cursor.moveToFirst();
     
            mediaAdapter = new MediaCursorAdapter(this, R.layout.listitem, cursor);
             
            setListAdapter(mediaAdapter);
             
            playButton.setOnClickListener(onButtonClick);
            nextButton.setOnClickListener(onButtonClick);
            prevButton.setOnClickListener(onButtonClick);
    }
	size_listSTR = String.valueOf(size_list);   //number of element in list
    Tracks.setText(size_listSTR);
}
 
@Override
    protected void onListItemClick(ListView list, View view, int position, long id) {
            super.onListItemClick(list, view, position, id);
             
            currentFile = (String) view.getTag();
            
            pos = position;
             
            startPlay(currentFile);
    }
 
    @Override
    protected void onDestroy() {
            super.onDestroy();
             
            handler.removeCallbacks(updatePositionRunnable);
            player.stop();
            player.reset();
            player.release();
 
            player = null;
    }
 
    private void startPlay(String file) {
            Log.i("Selected: ", file);
             
            selelctedFile.setText(file);
            seekbar.setProgress(0);
            
            int temp_pos=pos+1;
            Tracks.setText(String.valueOf(temp_pos)+"/"+size_listSTR);
            
            player.stop();
            player.reset();
             
            try {
                    player.setDataSource(file);
                    player.prepare();
                    player.start();
            } catch (IllegalArgumentException e) {
                    e.printStackTrace();
            } catch (IllegalStateException e) {
                    e.printStackTrace();
            } catch (IOException e) {
                    e.printStackTrace();
            }
             
            seekbar.setMax(player.getDuration());
            playButton.setImageResource(R.drawable.ic_pause);
             
            updatePosition();
             
            isStarted = true;
            int tmin = 0, tsec = 0; 
            double total_time = player.getDuration()/1000.0;
            tmin = (int) (total_time/60);
            tsec = (int) (total_time - tmin*60);
            if (tsec < 10)
            	ttime.setText(String.valueOf(tmin)+".0"+String.valueOf(tsec));
            else 
            	ttime.setText(String.valueOf(tmin)+"."+String.valueOf(tsec));
    }
     
    private void stopPlay() {
            player.stop();
            player.reset();
            playButton.setImageResource(android.R.drawable.ic_media_play);
            handler.removeCallbacks(updatePositionRunnable);
            seekbar.setProgress(0);
             
            isStarted = false;
    }
         
    private void updatePosition(){
            handler.removeCallbacks(updatePositionRunnable);
             
            seekbar.setProgress(player.getCurrentPosition());
            int min = 0, sec = 0; 
            double total_time = player.getCurrentPosition()/1000.0;
            min = (int) total_time/60;
            sec = (int) (total_time - min*60);
            if (sec < 10)
            	time.setText(String.valueOf(min)+".0"+String.valueOf(sec));
            else 
            	time.setText(String.valueOf(min)+"."+String.valueOf(sec));
            
            handler.postDelayed(updatePositionRunnable, UPDATE_FREQUENCY);
    }
 
    private class MediaCursorAdapter extends SimpleCursorAdapter{
 
            public MediaCursorAdapter(Context context, int layout, Cursor c) {
                    super(context, layout, c, 
                                    new String[] { MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.TITLE, MediaStore.Audio.AudioColumns.DURATION},
                                    new int[] { R.id.displayname, R.id.title, R.id.duration });
            }
 
            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                    TextView title = (TextView)view.findViewById(R.id.title);
                    TextView name = (TextView)view.findViewById(R.id.displayname);
                    TextView duration = (TextView)view.findViewById(R.id.duration);
                     
                    name.setText(cursor.getString(
                                    cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)));
                     
                    title.setText(cursor.getString(
                                    cursor.getColumnIndex(MediaStore.MediaColumns.TITLE)));
 
                    long durationInMs = Long.parseLong(cursor.getString(
                                    cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION)));
                     
                    double durationInMin = ((double)durationInMs/1000.0);
                    
                    int secondsALL = (int) durationInMin;
                    int minutes = (int) durationInMin/60;
                    int seconds = secondsALL-minutes*60;

                    //durationInMin = new BigDecimal(Double.toString(durationInMin)).setScale(2, BigDecimal.ROUND_UP).doubleValue(); 

                    if (seconds<10){
                    	duration.setText("" + minutes + ":0" + seconds);
                    }
                    else{
                    	duration.setText("" + minutes + ":" + seconds);
                    }
                    view.setTag(cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA)));
            }
 
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                    LayoutInflater inflater = LayoutInflater.from(context);
                    View v = inflater.inflate(R.layout.listitem, parent, false);
                     
                    bindView(v, context, cursor);
                     
                    return v;
            }
}
     
    private View.OnClickListener onButtonClick = new View.OnClickListener() {
             
            @Override
            public void onClick(View v) {
                    switch(v.getId())
                    {
                            case R.id.play:
                            {
                                    if(player.isPlaying())
                                    {
                                            handler.removeCallbacks(updatePositionRunnable);
                                            player.pause();
                                            playButton.setImageResource(R.drawable.ic_play);
                                    }
                                    else
                                    {
                                            if(isStarted)
                                            {
                                                    player.start();
                                                    playButton.setImageResource(R.drawable.ic_pause);
                                                     
                                                    updatePosition();
                                            }
                                            else
                                            {
                                            	    startPlay(currentFile);
                                            }
                                    }
                                     
                                    break;
                            }
                            case R.id.next:
                            {
                            	if (mxd==true)
                            		pos=newrand.nextInt(size_list);
                            	else{
                            	if (pos>=size_list-1)
                            	{
                            		pos=0;
                            	}
                            	else
                            		pos++;
                            	}
                            	
                            	View VIEW=mediaAdapter.getView(pos, null, null);

                            	currentFile = (String) VIEW.getTag();
                            	
                            	startPlay(currentFile);
                                     
                                    break;
                            }
                            case R.id.prev:
                            {
                            	if (mxd==true)
                            		pos=newrand.nextInt(size_list);
                            	else{
                            	if (pos==0)
                            	{
                            		pos=size_list-1;
                            	}
                            	else
                            		pos--;
                            	}
                            	
                            	
                                View VIEW=mediaAdapter.getView(pos, null, null);

                            	currentFile = (String) VIEW.getTag();
                            	
                            	startPlay(currentFile);
                            }
                    }
            }
    };
     
    private MediaPlayer.OnCompletionListener onCompletion = new MediaPlayer.OnCompletionListener() {
        
        @Override
        public void onCompletion(MediaPlayer mp) {
        	if (mxd==true)
        	{
        		pos=newrand.nextInt(size_list);
        	}
        	else
        	{
        	if (rpt==true)
        		startPlay(currentFile);
        	else
        	{
        		if (pos>=size_list-1)
            	{
            		pos=0;
            	}
            	else
            		pos++;
        	}
            	
        	}	
                View VIEW=mediaAdapter.getView(pos, null, null);

            	currentFile = (String) VIEW.getTag();
            	
            	startPlay(currentFile);            	
        	
        		
        }
    };

     
    private MediaPlayer.OnErrorListener onError = new MediaPlayer.OnErrorListener() {
             
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                    // returning false will call the OnCompletionListener
                    return false;
            }
    };
     
    private SeekBar.OnSeekBarChangeListener seekBarChanged = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                    isMoveingSeekBar = false;
            }
             
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                    isMoveingSeekBar = true;
            }
             
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                    if(isMoveingSeekBar)
                    {
                            player.seekTo(progress);
                                                        
                            Log.i("OnSeekBarChangeListener","onProgressChanged");
                    }
            }
    };
}
