package com.example.kev94.ttsbrowserjs;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

public class TTSBrowserJS extends AppCompatActivity implements OnInitListener, RecognitionListener, CustomActionModeCallback.iOnMenuSelected, JSInterface.iOnTextSelected {

    private static final String TAG = TTSBrowserJS.class.getSimpleName();
    private int MY_DATA_CHECK_CODE = 0;

    /* Named searches allow to quickly reconfigure the decoder */
    private final String KWS_SEARCH_SELECT = "select";
    private final String SELECTED = "selected";

    /* Keyword we are looking for to activate selection via asr */
    private final String KEYPHRASE_SELECT = "select";

    private HashMap<Integer, String> mNumberToStringMap;
    private HashMap<String, Integer> mStringToNumberMap;

    private EditText mEnterURL;
    private CustomWebView mWebView;
    private Button mButtonRead;
    private Button mButtonStop;

    private SpeechRecognizer mRecognizer;

    private TextToSpeech mTTS;

    private float mStandardSpeechRate = 1.0f;
    private float mCurrentSpeechRate;
    private boolean mIsRecognizerSetup = false;

    private HashMap<String, String> mTTSMap;
    private Bundle mIdParams;

    private Timer mNodTimer;
    private boolean mNodTimerActive = false;

    private Timer mShakeTimer;
    private boolean mShakeTimerActive = false;

    private Timer mTiltTimer;
    private boolean mTiltTimerActive = false;

    private NodGesture mNod;
    private ShakeGesture mShake;
    private TiltGesture mTilt;

    private String mSelectedText;

    private int mCurrentlySelected = 0;
    private String mNextSelectedString = "null";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ttsbrowser);

        mNumberToStringMap = new HashMap<>();
        mNumberToStringMap.put(1, "one");
        mNumberToStringMap.put(2, "two");
        mNumberToStringMap.put(3, "three");
        mNumberToStringMap.put(4, "four");
        mNumberToStringMap.put(5, "five");
        mNumberToStringMap.put(6, "six");
        mNumberToStringMap.put(7, "seven");
        mNumberToStringMap.put(8, "eight");
        mNumberToStringMap.put(9, "nine");

        mStringToNumberMap = new HashMap<>();
        mStringToNumberMap.put("one", 1);
        mStringToNumberMap.put("two", 2);
        mStringToNumberMap.put("three", 3);
        mStringToNumberMap.put("four", 4);
        mStringToNumberMap.put("five", 5);
        mStringToNumberMap.put("six", 6);
        mStringToNumberMap.put("seven", 7);
        mStringToNumberMap.put("eight", 8);
        mStringToNumberMap.put("nine", 9);

        mEnterURL = (EditText) findViewById(R.id.url);
        mWebView = (CustomWebView) findViewById(R.id.webview);
        mButtonRead = (Button) findViewById(R.id.button_read);
        mButtonStop = (Button) findViewById(R.id.button_stop);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);

        mWebView.setWebViewClient(new WebViewClient());

        mButtonRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decideNextStep("read");
            }
        });

        mButtonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Touch", "Click");
                if (mTTS != null && mTTS.isSpeaking()) {
                    mTTS.stop();
                }
            }
        });

        //custom html document with javascript function for selection
        mWebView.loadUrl("file:///android_asset/ThirdTest.html");

        //user presses ENTER (done)
        mEnterURL.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String url = mEnterURL.getText().toString();

                    if (!url.startsWith("www.") && !url.startsWith("http://") && !url.startsWith("https://")) {
                        url = "www." + url;
                    }
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        url = "http://" + url;
                    }

                    mWebView.loadUrl(url);
                }
                return false;
            }
        });

        //used for showing the custom action bar when text was highlighted
        mWebView.addJavascriptInterface(new JSInterface(getApplicationContext(), this, mWebView), "Android");

        //asynchronous task for initializing the speech recognition
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(TTSBrowserJS.this);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            //this part will be executed after execution of the background work
            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_asr_failed), Toast.LENGTH_LONG).show();
                    mIsRecognizerSetup = true;
                    Log.i("ASR", "ASR ready");
                    //((TextView) findViewById(R.id.caption_text)).setText("Failed to init recognizer " + result);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_asr_ready), Toast.LENGTH_LONG).show();
                    switchSearch(KWS_SEARCH_SELECT);
                }
            }
        }.execute();

        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //initialize Text-To-Speech engine
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                mTTS = new TextToSpeech(this, this);
            }
            else {
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    //called when TTS is initialized
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            //mTTS.setSpeechRate(mStandardSpeechRate);
            mCurrentSpeechRate = mStandardSpeechRate;
            Toast.makeText(this, getString(R.string.toast_tts_ready), Toast.LENGTH_LONG).show();
            mTTS.setLanguage(Locale.US);

            mTTSMap = new HashMap<>();
            mTTSMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");

            mIdParams = new Bundle();
            mIdParams.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");

            mTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(final String utteranceId) {
                    Log.i("Speech", "start speaking");

                    Log.i("Speech", "stop ASR");
                    mRecognizer.stop();

                    startShakeTimer();

                    if (mTiltTimerActive) {
                        stopTiltTimer();
                    }

                    if (mNodTimerActive) {
                        stopNodTimer();
                    }

                    //if recognizer is listening -> stop listening
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mButtonRead.setClickable(false);
                            mButtonRead.setEnabled(false);

                            mButtonStop.setEnabled(true);
                            mButtonStop.setClickable(true);
                        }
                    });
                }

                @Override
                public void onDone(String utteranceId) {
                    Log.i("Speech", "Done");

                    onSpeechEnd();
                }

                @Override
                public void onError(String utteranceId) {
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_err_speaking), Toast.LENGTH_LONG).show();
                }
            });

        } else if (status == TextToSpeech.ERROR) {
            Toast.makeText(this, getString(R.string.toast_tts_failed), Toast.LENGTH_LONG).show();
        }
    }

    private void onSpeechEnd() {

        if(mShakeTimerActive) {
            stopShakeTimer();
        }

        mRecognizer.stop();

        if (mCurrentlySelected == 0) {
            switchSearch(KWS_SEARCH_SELECT);
        } else {
            switchSearch(SELECTED);
        }

        if(!mTiltTimerActive) {
            startTiltTimer();
        }

        if(!mNodTimerActive) {
            startNodTimer();
        }

        //if recognizer is listening -> stop listening
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mButtonRead.setClickable(true);
                mButtonRead.setEnabled(true);

                mButtonStop.setEnabled(false);
                mButtonStop.setClickable(false);
            }
        });
    }

    private void startNodTimer() {

        mNodTimer = new Timer();

        mNod = new NodGesture(this);

        mNodTimerActive = true;

        mNodTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if ((mNod != null) && (mSelectedText != null) && (mNod.gestureCompleted() == 1)) {
                            decideNextStep("read");
                        }
                    }
                });
            }
        }, 0, 50);
    }

    private void stopNodTimer() {

        mNodTimer.cancel();

        if(mNod != null) {
            mNod.unregisterListener();
            mNod = null;
        }

        mNodTimerActive = false;

    }

    private void startShakeTimer() {

        mShakeTimer = new Timer();

        mShake = new ShakeGesture(this);

        mShakeTimerActive = true;

        mShakeTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if ((mShake != null) && mTTS.isSpeaking() && (mShake.gestureCompleted() == 1)) {
                            mTTS.stop();
                            onSpeechEnd();
                        }
                    }
                });
            }
        }, 0, 50);
    }

    private void stopShakeTimer() {

        mShakeTimer.cancel();

        if(mShake != null) {
            mShake.unregisterListener();
            mShake = null;
        }

        mShakeTimerActive = false;
    }

    private void startTiltTimer() {

        mTiltTimer = new Timer();

        mTilt = new TiltGesture(this);

        mTiltTimerActive = true;

        mTiltTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                if (mTilt != null) {

                    //don't use mTilt.gestureCompleted() in both the if and else if statement, instead
                    //save the value to a temporary variable. This way, the function gets called only
                    //once. Otherwise only the first statement can be true and the second statement
                    //will never be true
                    int gestureCompleted = mTilt.gestureCompleted();

                    if (gestureCompleted == -1) {
                        Log.i("SeonsorMain", "negative");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                decideNextStep("previous");
                            }
                        });
                    } else if (gestureCompleted == 1) {
                        Log.i("SeonsorMain", "positive");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                decideNextStep("next");
                            }
                        });
                    }
                }
            }
        }, 0, 50);
    }

    private void stopTiltTimer() {

        mTiltTimer.cancel();

        if(mTilt != null) {
            mTilt.unregisterListener();
            mTilt = null;
        }

        mTiltTimerActive = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mTTS != null) {
            mTTS.shutdown();
        }

        if(mRecognizer != null) {
            mRecognizer.stop();
        }

        if(mTiltTimerActive) {
            stopTiltTimer();
        }

        if(mShakeTimerActive) {
            stopShakeTimer();
        }

        if(mNodTimerActive) {
            stopNodTimer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if(mRecognizer != null && (mRecognizer.getSearchName() != null)) {
            if(!mRecognizer.getSearchName().equals(KWS_SEARCH_SELECT)) {
                switchSearch(KWS_SEARCH_SELECT);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mTTS != null) {
            mTTS.shutdown();
        }

        if(mRecognizer != null) {
            mRecognizer.cancel();
            mRecognizer.shutdown();
        }
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        // The mRecognizer can be configured to perform multiple searches
        // of different kind and switch between them

        //defaultSetup().setFloat("-vad_threshold", 3.0) can be used as threshold for voice detection

        mRecognizer = defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "dictionary.dic"))

                        // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                        // .setRawLogDir(assetsDir)

                        // Threshold to tune for keyphrase to balance between false alarms and misses
                .setKeywordThreshold(1e-30f)

                        // Use context-independent phonetic search, context-dependent is too slow for mobile
                .setBoolean("-allphone_ci", true)

                .getRecognizer();
        mRecognizer.addListener(this);

        // Create keyword-activation search.
        mRecognizer.addKeyphraseSearch(KWS_SEARCH_SELECT, KEYPHRASE_SELECT);

        File grammarNext = new File(assetsDir, "selected.gram");
        mRecognizer.addGrammarSearch(SELECTED, grammarNext);

        mIsRecognizerSetup = true;
    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null) {
            return;
        }

        String partialResult = hypothesis.getHypstr();

        decideNextStep(partialResult);
    }

    public void decideNextStep(String command) {

        mRecognizer.stop();

        if(command.equals(KEYPHRASE_SELECT)) {
            Log.i("Speech", command);

            // 1 as an argument of SelectText() to tell the function that it has been called by Android
            mWebView.evaluateJavascript("SelectText('one', 1)", null);

            //getSelectedText();

            mCurrentlySelected = 1;

            switchSearch(SELECTED);

        } else if(command.equals("next")){

            if((mCurrentlySelected > 0) && (mCurrentlySelected < 10)) {
                //nonsense expression?
                mCurrentlySelected = mStringToNumberMap.get(mNumberToStringMap.get(mCurrentlySelected));
            }

            if((mCurrentlySelected + 1) < 10) {
                mNextSelectedString = mNumberToStringMap.get(mCurrentlySelected + 1);

                // 1 as an argument of SelectText() to tell the function that it has been called by Android
                mWebView.evaluateJavascript("SelectText('" + mNextSelectedString + "', 1)", null);

                //getSelectedText();

                mCurrentlySelected = mStringToNumberMap.get(mNextSelectedString);

                switchSearch(SELECTED);

            }
        } else if(command.equals("previous")){

            if((mCurrentlySelected > 0) && (mCurrentlySelected < 10)) {
                mCurrentlySelected = mStringToNumberMap.get(mNumberToStringMap.get(mCurrentlySelected));
            }

            if((mCurrentlySelected - 1) > 0) {
                mNextSelectedString = mNumberToStringMap.get(mCurrentlySelected - 1);

                // 1 as an argument of SelectText() to tell the function that it has been called by Android
                mWebView.evaluateJavascript("SelectText('" + mNextSelectedString + "', 1)", null);

                //getSelectedText();

                mCurrentlySelected = mStringToNumberMap.get(mNextSelectedString);

                switchSearch(SELECTED);

            }
        } else if(command.equals("read")) {
            if((mSelectedText != null) && (!mSelectedText.isEmpty())) {
                speakSelection(mSelectedText);
            }
        }
    }

    @Override
    public void onResult(Hypothesis hypothesis) {

    }

    @Override
    public void onError(Exception error) {
        Toast.makeText(this, getString(R.string.toast_asr_error), Toast.LENGTH_LONG);
    }

    @Override
    public void onTimeout() {
        if(!mRecognizer.getSearchName().equals(SELECTED)) {
            switchSearch(SELECTED);
        }
    }

    private void getSelectedText() {
        mWebView.evaluateJavascript(
                "getSelectedText()"
                , new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String s) {
                        mSelectedText = s;
                    }
                });
    }

    @TargetApi(21)
    public void speakSelection(String selection) {
        if (android.os.Build.VERSION.SDK_INT < 19) {
            mTTS.speak(selection, TextToSpeech.QUEUE_FLUSH, mTTSMap);
        } else {
            mTTS.speak(selection, TextToSpeech.QUEUE_FLUSH, mIdParams, "UniqueID");
        }
    }

    public void switchSearch(String searchName) {
        mRecognizer.stop();

        Log.i("Speech", "start Listening: " + searchName);

        // If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
        if (searchName.equals(KWS_SEARCH_SELECT)) {
            mRecognizer.startListening(searchName);
        } else {
            mRecognizer.startListening(searchName, 10000);
        }
    }

    @Override
    public void onMenuSelected(String menu) {

    }

    @Override
    public void onTextSelected(String id, String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mButtonRead.setClickable(true);
                mButtonRead.setEnabled(true);
            }
        });

        if(!mTiltTimerActive) {
            startTiltTimer();
        }

        if(!mNodTimerActive) {
            startNodTimer();
        }

        if((id != null) && (!id.isEmpty())) {
            mCurrentlySelected = mStringToNumberMap.get(id);
            mSelectedText = text;
            if(!mRecognizer.getSearchName().equals(SELECTED)) {
                switchSearch(SELECTED);
            }
        } else {
            mSelectedText = null;
            mCurrentlySelected = 0;
            stopNodTimer();
            stopTiltTimer();
            switchSearch(KWS_SEARCH_SELECT);
        }

        if (mSelectedText == null) {
            Log.i("Text", "null");
        } else {
            Log.i("Text", mSelectedText);
        }
    }
}
