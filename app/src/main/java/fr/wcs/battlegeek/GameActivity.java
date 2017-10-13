package fr.wcs.battlegeek;

import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import fr.wcs.battlegeek.controller.AI;
import fr.wcs.battlegeek.controller.GameController;
import fr.wcs.battlegeek.model.Result;
import fr.wcs.battlegeek.ui.EndGameDefeatFragment;
import fr.wcs.battlegeek.ui.EndGameVictoryFragment;
import fr.wcs.battlegeek.ui.GameView;
import fr.wcs.battlegeek.ui.MapView;
import fr.wcs.battlegeek.ui.QuitGameFragment;

import static fr.wcs.battlegeek.R.id.viewFlipper;
import static fr.wcs.battlegeek.model.Result.Type.DROWN;
import static fr.wcs.battlegeek.model.Result.Type.MISSED;
import static fr.wcs.battlegeek.model.Result.Type.VICTORY;

public class GameActivity extends AppCompatActivity {

    private final String TAG = "GameActivity";

    private AI mAI;
    private GameController mGameController;
    private boolean canPlay = true;

    private Toast mToast;
    private Context mContext;

    private MapView mMapView;
    private GameView mGameView;
    private ViewFlipper mViewFlipper;
    private TextView mTextViewPlayer;
    private TextView mTextViewAI;
    private Button mButtonSwitchView;
    private Button mButtonRandomPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_game);

        Intent intent = getIntent();
        final String level = intent.getStringExtra("Level");

        mContext = getApplicationContext();
        mMapView = (MapView) findViewById(R.id.mapView);
        mViewFlipper = (ViewFlipper) findViewById(viewFlipper);
        mTextViewPlayer = (TextView) findViewById(R.id.textViewPlayer);
        mTextViewAI = (TextView) findViewById(R.id.textViewAI);
        mButtonRandomPosition = (Button) findViewById(R.id.buttonRandomPositions);
        mButtonRandomPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMapView.setRandomPositions();
            }
        });
        mButtonSwitchView = (Button) findViewById(R.id.buttonSwitchView);

        final Button buttonLaunchGame = (Button) findViewById(R.id.buttonLaunchGame);

        buttonLaunchGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mButtonSwitchView.setVisibility(View.VISIBLE);
                mButtonRandomPosition.setVisibility(View.GONE);
                mTextViewPlayer.setText(R.string.player_turn);
                mMapView = (MapView) findViewById(R.id.mapView);
                char[][] mapData = mMapView.getMapData();
                mGameController = new GameController(mapData);
                mMapView.setMode(MapView.Mode.PLAY);
                mAI = new AI();
                if (level.equals("Impossible")) {
                    mAI.setPlayerMap(mapData);
                    mAI.setLevel(AI.Level.IMPOSSIBLE);
                }
                buttonLaunchGame.setVisibility(View.GONE);
                mTextViewAI.setTextColor(Color.parseColor("#FF960D"));
                mTextViewAI.setText(R.string.AITurn);
                mViewFlipper.showNext();
            }

        });

        mButtonSwitchView.setVisibility(View.GONE);

        mButtonSwitchView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mButtonSwitchView.setPressed(true);
                        mTextViewAI.setText(R.string.playermap_view);
                        mTextViewAI.setTextColor(Color.parseColor("#FFEE00"));
                        mViewFlipper.showPrevious();
                        break;
                    case MotionEvent.ACTION_UP:
                        mButtonSwitchView.setPressed(false);
                        mViewFlipper.showNext();
                        mTextViewAI.setText(" ");
                        mTextViewAI.setTextColor(Color.parseColor("#FF960D"));
                        break;
                }
                return true;
            }
        });

        mGameView = (GameView) findViewById(R.id.gameView);
        mGameView.setOnPlayListener(new GameView.PlayListener() {
            @Override
            public void onPlayListener(int x, int y) {

                if(!canPlay) {
                    return;
                }

                if(mGameController.alreadyPlayed(x, y)) {
                    showToast(R.string.alreadyPlayedMessage);
                    return;
                }

                playerPlay(x, y);
            }
        });

        Typeface mainFont = Typeface.createFromAsset(getAssets(), "fonts/Curvy.ttf");
        Typeface buttonFont = Typeface.createFromAsset(getAssets(), "fonts/DirtyClassicMachine.ttf");

        TextView titleMessage = (TextView) findViewById(R.id.textViewSettings);

        mTextViewAI.setTypeface(mainFont);
        mTextViewPlayer.setTypeface(mainFont);

        mButtonRandomPosition.setTypeface(buttonFont);
        mButtonSwitchView.setTypeface(buttonFont);
        buttonLaunchGame.setTypeface(buttonFont);

    }

    private void playerPlay(int x, int y) {
        canPlay = false;

        Result result = mAI.shot(x, y);
        mGameController.setPlayResult(x, y, result);
        switch (result.getType()) {
            case TOUCHED:
                mGameView.setTouch(x, y, result.getShape());
                mTextViewPlayer.setText(R.string.touchedPlayAgain);
                canPlay = true;
                return;
            case DROWN:
                mGameView.setTouch(x, y, result.getShape());
                mTextViewPlayer.setText(R.string.drownPlayAgain);
                showToast(R.string.itemDrownMessage);
                canPlay = true;
                return;
            case VICTORY:
                mGameView.setTouch(x, y, result.getShape());
                FragmentManager fm = getFragmentManager();
                EndGameVictoryFragment endGameVictoryFragment = new EndGameVictoryFragment();
                endGameVictoryFragment.show(fm, String.valueOf(R.string.end_game_fragment_title));
                endGameVictoryFragment.setCancelable(false);
                return;
            case MISSED:
                mGameView.setPlouf(x, y);
                mTextViewPlayer.setText(R.string.missed);
                // Show the result
                new CountDownTimer(650, 500) {
                    public void onTick(long millisUntilFinished) {
                    }

                    // Move to MapView and AI Turn
                    public void onFinish() {
                        mTextViewPlayer.setText(R.string.player_turn);
                        mViewFlipper.showPrevious();
                        aiPlay();
                    }
                }.start();

                break;
        }
    }

    private void aiPlay() {
        mButtonSwitchView.setVisibility(View.GONE);
        mTextViewAI.setTextColor(Color.parseColor("#FF960D"));
        mTextViewAI.setText(R.string.AITurn);

        final Point aiPlayCoordinates = mAI.play();
        final Result iaResult = mGameController.shot(aiPlayCoordinates.x, aiPlayCoordinates.y);
        final Result.Type resultType = iaResult.getType();
        Log.d(TAG, "onPlayListener: " + aiPlayCoordinates + " " + iaResult);

        mAI.setResult(iaResult);

        new CountDownTimer(1750, 350) {
            private int cursor = 0;
            @Override
            public void onTick(long l) {
                if(cursor == 1) {
                    if(resultType == MISSED) {
                        mMapView.setPlouf(aiPlayCoordinates.x, aiPlayCoordinates.y);
                        mTextViewAI.setText(R.string.missed);
                    }
                    else {
                        mMapView.setDead(aiPlayCoordinates.x, aiPlayCoordinates.y);
                        mTextViewAI.setText(R.string.AITouched);
                        if (resultType == DROWN) {
                            mTextViewAI.setText(R.string.AIDrown);
                        }
                    }
                }
                cursor++;
            }
            @Override
            public void onFinish() {
                mButtonSwitchView.setVisibility(View.VISIBLE);
                if(resultType == MISSED) {
                    mTextViewAI.setText(R.string.AITurn);
                    canPlay = true;
                    mViewFlipper.showPrevious();
                }
                else if(resultType == VICTORY) {
                    FragmentManager fm = getFragmentManager();
                    EndGameDefeatFragment endGameDefeatFragment = new EndGameDefeatFragment();
                    endGameDefeatFragment.show(fm, String.valueOf(R.string.end_game_fragment_title));
                    endGameDefeatFragment.setCancelable(false);
                }
                else {
                    aiPlay();
                }
            }
        }.start();
    }

    private void showToast(int stringResource) {
        if(mToast == null) {
            mToast = Toast.makeText(mContext, getString(stringResource), Toast.LENGTH_SHORT);
        }
        mToast.setText(getString(stringResource));
        mToast.setDuration(Toast.LENGTH_SHORT);
        mToast.show();
    }

    @Override
    public void onBackPressed() {

        FragmentManager fm = getFragmentManager();
        QuitGameFragment quitGameFragment = new QuitGameFragment();
        quitGameFragment.show(fm, String.valueOf(R.string.end_game_fragment_title));
        quitGameFragment.setCancelable(false);

    }
}