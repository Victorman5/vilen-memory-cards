package com.victorman.memorycards.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.DefaultItemAnimator;

import com.google.android.material.appbar.MaterialToolbar;
import com.victorman.memorycards.AppPreferences;
import com.victorman.memorycards.data.Card;
import com.victorman.memorycards.data.DataBaseHelper;
import com.victorman.memorycards.R;
import com.victorman.memorycards.Utils;
import com.victorman.memorycards.adapters.CardInStackAdapter;
import com.victorman.memorycards.services.NotificationSenderService;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.StackFrom;
import com.yuyakaido.android.cardstackview.SwipeableMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_GET_SETTINGS = 0;

    public static final String EXTRA_CARDS_LIST_TERMS = "CARDS_LIST_TERMS";
    public static final String EXTRA_CARDS_LIST_DEFINITIONS = "CARDS_LIST_DEFINITIONS";
    public static final String EXTRA_CARDS_MIX_METHOD = "CardsMixMethod";
    public static final String EXTRA_CARDS_STACK_SIZE = "CardsStackSize";
    public static final String EXTRA_NOTIFY_SENDING_INTERVAL_MINUTES = "NotifySendingIntervalMinutes";

    public static final int CARDS_MIX_METHOD_ORDER = 1;
    public static final int CARDS_MIX_METHOD_RANDOM = 2;
    public static final int CARDS_MIX_METHOD_SMART = 3;

    private CardInStackAdapter cardInStackAdapter;

    private int notifySendingIntervalMinutes;
    private int cardsMixMethod;
    private int cardsStackSize;

    private Card currentCard;
    private int currentPosition = -1;

    private ConstraintLayout rootLayout;
    private ContentLoadingProgressBar progressBarPreparation;
    private TextView textViewNegativeCount;
    private TextView textViewPositiveCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppPreferences.initialization(getSharedPreferences(AppPreferences.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE));
        notifySendingIntervalMinutes = AppPreferences.getNotifySendingIntervalMinutes();
        cardsMixMethod = AppPreferences.getCardsMixMethod();
        cardsStackSize = AppPreferences.getCardsStackSize();

        if (AppPreferences.isDarkThemeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.DarkTheme);
        } else {
            setTheme(R.style.LightTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DataBaseHelper.openDataBase(this);

        setToolbar();
        setOtherViews();
        setCardStackView();

        refreshCardsStack(cardsMixMethod);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GET_SETTINGS && resultCode == SettingsActivity.RESULT_CODE_OK
            && data != null) {
            notifySendingIntervalMinutes = data.getIntExtra(EXTRA_NOTIFY_SENDING_INTERVAL_MINUTES, notifySendingIntervalMinutes);
            cardsMixMethod = data.getIntExtra(EXTRA_CARDS_MIX_METHOD, cardsMixMethod);
            cardsStackSize = data.getIntExtra(EXTRA_CARDS_STACK_SIZE, cardsStackSize);
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                setTheme(R.style.DarkTheme);
            } else {
                setTheme(R.style.LightTheme);
            }
        }
    }

    @Override
    protected void onDestroy() {
        DataBaseHelper.closeDataBase();
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_toolbar, menu);
        menu.findItem(R.id.menuStartSendingTermsNotifications).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startNotificationSenderService();
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    protected void onResume() {
        super.onResume();
        refreshCardsStack(cardsMixMethod);
    }

    private void setCardStackView()
    {
        final CardStackView cardStackView = findViewById(R.id.activityMainCardStackView);

        final CardStackLayoutManager cardStackLayoutManager = new CardStackLayoutManager(this, new CardStackListener() {

            @Override
            public void onCardDragging(Direction direction, float ratio) {

            }

            @Override
            public void onCardSwiped(Direction direction) {
                if (direction == Direction.Right) {
                    textViewPositiveCount.setText(String.valueOf(Integer.parseInt(textViewPositiveCount.getText().toString()) + 1));
                    currentCard.incrementPositiveCount();
                    DataBaseHelper.updateCardPositiveCount(currentCard);
                } else if (direction == Direction.Left) {
                    textViewNegativeCount.setText(String.valueOf(Integer.parseInt(textViewNegativeCount.getText().toString()) + 1));
                    currentCard.incrementNegativeCount();
                    DataBaseHelper.updateCardNegativeCount(currentCard);
                }

                if (currentPosition == cardInStackAdapter.getItemCount() - 1) {
                    refreshCardsStack(cardsMixMethod);
                }
            }

            @Override
            public void onCardRewound() {

            }

            @Override
            public void onCardCanceled() {

            }

            @Override
            public void onCardAppeared(View view, int position) {
                currentCard = cardInStackAdapter.getCards().get(position);
                currentPosition = position;
            }

            @Override
            public void onCardDisappeared(View view, int position) {

            }
        });
        cardStackLayoutManager.setStackFrom(StackFrom.Top);
        cardStackLayoutManager.setVisibleCount(5);
        cardStackLayoutManager.setTranslationInterval(8.0f);
        cardStackLayoutManager.setScaleInterval(0.95f);
        cardStackLayoutManager.setSwipeThreshold(0.1f);
        cardStackLayoutManager.setMaxDegree(60.0f);
        cardStackLayoutManager.setDirections(Direction.HORIZONTAL);
        cardStackLayoutManager.setCanScrollHorizontal(true);
        cardStackLayoutManager.setSwipeableMethod(SwipeableMethod.Manual);
        cardStackLayoutManager.setOverlayInterpolator(new LinearInterpolator());

        cardStackView.setLayoutManager(cardStackLayoutManager);
        cardInStackAdapter = new CardInStackAdapter(getResources().getStringArray(R.array.cardColors));
        cardStackView.setAdapter(cardInStackAdapter);
        cardStackView.setItemAnimator(new DefaultItemAnimator());
    }


    protected void setToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.activityMainToolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    protected void setOtherViews() {
        findViewById(R.id.activityMainButtonCollections).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CardsCollectionsListActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.activityMainButtonSettings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                intent.putExtra(EXTRA_NOTIFY_SENDING_INTERVAL_MINUTES, notifySendingIntervalMinutes);
                intent.putExtra(EXTRA_CARDS_MIX_METHOD, cardsMixMethod);
                intent.putExtra(EXTRA_CARDS_STACK_SIZE, cardsStackSize);
                startActivityForResult(intent, REQUEST_CODE_GET_SETTINGS);
            }
        });

        textViewNegativeCount = findViewById(R.id.activityMainTextViewNegativeCount);
        textViewPositiveCount = findViewById(R.id.activityMainTextViewPositiveCount);

        rootLayout = findViewById(R.id.activityMainRootLayout);
        progressBarPreparation = findViewById(R.id.activityMainProgressBar);
    }


    protected void refreshCardsStack(int task) {
        textViewNegativeCount.setText("0");
        textViewPositiveCount.setText("0");

        (new LoadCardsAsyncTask()).execute(task);
    }


    protected void startNotificationSenderService() {
        String[] terms = new String[cardInStackAdapter.getItemCount()];
        String[] definitions = new String[cardInStackAdapter.getItemCount()];
        List<Card> cards = cardInStackAdapter.getCards();
        for (int i = 0; i < cards.size(); i++) {
            terms[i] = cards.get(i).getTerm();
            definitions[i] = cards.get(i).getDefinition();
        }

        Intent intent = new Intent(this, NotificationSenderService.class);
        intent.putExtra(EXTRA_CARDS_LIST_TERMS, terms);
        intent.putExtra(EXTRA_CARDS_LIST_DEFINITIONS, definitions);
        intent.putExtra(EXTRA_NOTIFY_SENDING_INTERVAL_MINUTES, notifySendingIntervalMinutes);

        startService(intent);
    }


    @SuppressLint("StaticFieldLeak")
    protected class LoadCardsAsyncTask extends AsyncTask<Integer, Integer, Void> {
        public static final int TASK_SHUFFLE_ORDER = CARDS_MIX_METHOD_ORDER;
        public static final int TASK_SHUFFLE_RANDOM = CARDS_MIX_METHOD_RANDOM;
        public static final int TASK_SHUFFLE_SMART = CARDS_MIX_METHOD_SMART;

        private List<Card> loadedCards;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            rootLayout.setEnabled(false);
            progressBarPreparation.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Integer... integers) {
            // load cards from db
            loadedCards = new ArrayList<>();
            long[] collectionsIds = DataBaseHelper.getSelectedCardsCollectionsIds();
            for (int i = 0; i < collectionsIds.length; i++) {
                publishProgress((int) ((double) i / collectionsIds.length * 100));
                loadedCards.addAll(DataBaseHelper.getCards(collectionsIds[i]));
            }

            // integers[0] - TASK_ID
            if (integers[0] == TASK_SHUFFLE_ORDER) {
                // without changing
            }
            else if (integers[0] == TASK_SHUFFLE_RANDOM) {
                Collections.shuffle(loadedCards);
            }
            else if (integers[0] == TASK_SHUFFLE_SMART) {
                if (loadedCards.size() > 0) {
                    loadedCards = Utils.getSmartCardsCompilation(loadedCards, cardsStackSize);
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            // values[0] - current progress
            progressBarPreparation.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            cardInStackAdapter.setCards(loadedCards);

            rootLayout.setEnabled(true);
            progressBarPreparation.setVisibility(View.INVISIBLE);
        }
    }
}
