package com.victorman.memorycards.activities;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.victorman.memorycards.data.Card;
import com.victorman.memorycards.data.CardsCollection;
import com.victorman.memorycards.data.DataBaseHelper;
import com.victorman.memorycards.Utils;
import com.victorman.memorycards.dialogs.EditCardDialog;
import com.victorman.memorycards.R;
import com.victorman.memorycards.adapters.CardInListAdapter;
import com.victorman.memorycards.dialogs.EditCardsCollectionDialog;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;


public class CardsListActivity extends AppCompatActivity implements EditCardDialog.EditCardDialogListener, EditCardsCollectionDialog.EditCardsCollectionDialogListener {
    public static final int RESULT_CODE_COLLECTION_CREATED = 1; // created reversed cards collection
    public static final int RESULT_CODE_COLLECTION_EDITED = 2; // current collection changed
    public static final int RESULT_CODE_COLLECTION_CHANGED_AND_CREATED = 3;

    public static final int REQUEST_CODE_PERMISSION_WRITE_EXTERNAL_STORAGE = 0;
    public static final int REQUEST_CODE_CREATE_COLLECTION_FILE = 1;

    public static final String EXTRA_CARDS_COLLECTION = "EXTRA_CARDS_COLLECTION";
    public static final String EXTRA_CREATED_CARDS_COLLECTION = "EXTRA_CREATED_CARDS_COLLECTION";

    private CardsCollection cardsCollection;
    private CardsCollection createdCardsCollection;

    private boolean collectionEdited;
    private boolean reversedCollectionCreated;

    private Stack<Pair<CardInListAdapter.Item, Integer>> deletedCards;

    private ConstraintLayout rootLayout;
    private ContentLoadingProgressBar progressBar;
    private CardInListAdapter cardInListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.DarkTheme);
        } else {
            setTheme(R.style.LightTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cards_list);

        // get data from the last activity
        cardsCollection = getIntent().getParcelableExtra(CardsCollectionsListActivity.EXTRA_CARDS_COLLECTION);

        collectionEdited = false;
        reversedCollectionCreated = false;
        deletedCards = new Stack<>();

        setToolbar();
        setRecyclerView();
        setOtherViews();

        (new AsyncTaskExecutor()).execute(AsyncTaskExecutor.TASK_LOAD_CARDS);
    }


    @Override
    protected void onPause() {
        (new AsyncTaskExecutor()).execute(AsyncTaskExecutor.TASK_DELETE_CARDS);
        super.onPause();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) return;

        if (requestCode == REQUEST_CODE_CREATE_COLLECTION_FILE) {
            if (resultCode == Activity.RESULT_OK) {
                (new AsyncTaskExecutor()).execute(AsyncTaskExecutor.TASK_CREATE_COLLECTION_FILE, data.getData());
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSION_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCreateFileActivity();
            }
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cards_list_toolbar, menu);


        final SearchView searchView = (SearchView) menu.findItem(R.id.menuActionCardsSearch).getActionView();
        searchView.setOnQueryTextListener (new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                cardInListAdapter.getFilter().filter(newText);
                return false;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public boolean onClose() {
                searchView.setLayoutParams(new MaterialToolbar.LayoutParams(Gravity.END));
                searchView.clearFocus();
                Objects.requireNonNull(getSupportActionBar()).collapseActionView();
                return false;
            }
        });

        return true;
    }


    @Override
    public void onBackPressed() {
        goBack();
        super.onBackPressed();
    }


    @Override
    public void onEditCard(int position, Card card) {
        DataBaseHelper.updateCardSignature(card);
        cardInListAdapter.editCard(position, card);
    }


    @Override
    public void onCreateCard(Card card) {
        card.setId(DataBaseHelper.addCard(card));
        cardInListAdapter.insertCard(cardInListAdapter.getItemCount(), card);
    }


    @Override
    public void onEditCardsCollection(CardsCollection cardsCollection) {
        DataBaseHelper.updateCardsCollection(cardsCollection);
        Objects.requireNonNull(getSupportActionBar()).setTitle(cardsCollection.getName());
        collectionEdited = true;
    }


    protected void openEditCardDialog(int position) {
        EditCardDialog editCardDialog;
        if (position == -1) {
            editCardDialog = new EditCardDialog(position, new Card(cardsCollection.getId()));
        } else {
            editCardDialog = new EditCardDialog(position, cardInListAdapter.getItems().get(position).card);
        }
        editCardDialog.show(getSupportFragmentManager(), "EditCardDialog");
    }


    protected void openEditCardsCollectionDialog(CardsCollection cardsCollection) {
        (new EditCardsCollectionDialog(cardsCollection)).show(getSupportFragmentManager(), "EditCardsCollectionDialog");
    }


    private void setToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.activityCardsListToolbar);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(cardsCollection.getName());

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });
        toolbar.setOnMenuItemClickListener(new MaterialToolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menuActionCardsCollectionEdit) {
                    openEditCardsCollectionDialog(cardsCollection);
                    return true;
                }

                if (item.getItemId() == R.id.menuActionClearCounters) {
                    (new AsyncTaskExecutor()).execute(AsyncTaskExecutor.TASK_CLEAR_COUNTERS);
                    return true;
                }

                if (item.getItemId() == R.id.menuActionCreateReversedCardsCollection) {
                    if (!reversedCollectionCreated) {
                        (new AsyncTaskExecutor()).execute(AsyncTaskExecutor.TASK_CREATE_REVERSED_CARDS_COLLECTION);
                        reversedCollectionCreated = true;
                    }
                    return true;
                }

                if (item.getItemId() == R.id.menuActionCardsSearch) {
                    SearchView searchView = (SearchView) item.getActionView();
                    searchView.setIconifiedByDefault(true);
                    searchView.setFocusable(true);
                    searchView.setIconified(false);
                    searchView.requestFocusFromTouch();
                    return true;
                }

                if (item.getItemId() == R.id.menuActionCardsCollectionUpload) {
                    // check permissions
                    if (ContextCompat.checkSelfPermission(CardsListActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        openCreateFileActivity();
                    } else {
                        ActivityCompat.requestPermissions(
                                CardsListActivity.this,
                                new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                                REQUEST_CODE_PERMISSION_WRITE_EXTERNAL_STORAGE);
                    }
                    return true;
                }

                return false;
            }
        });
    }


    protected void setRecyclerView() {
        RecyclerView recyclerViewCards = findViewById(R.id.activityCardsListRecyclerViewCards);
        recyclerViewCards.setHasFixedSize(true);
        recyclerViewCards.setLayoutManager(new LinearLayoutManager(this));
        cardInListAdapter = new CardInListAdapter(getResources().getStringArray(R.array.cardColors));
        recyclerViewCards.setAdapter(cardInListAdapter);
        cardInListAdapter.setOnItemClickListener(new CardInListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                openEditCardDialog(position);
            }
        });

        (new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN , ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int currentPosition = viewHolder.getAdapterPosition();
                int targetPosition = target.getAdapterPosition();

                Card currentCard = cardInListAdapter.getItems().get(currentPosition).card;
                Card targetCard = cardInListAdapter.getItems().get(targetPosition).card;

                Card.swapIds(currentCard, targetCard);
                DataBaseHelper.updateCard(currentCard);
                DataBaseHelper.updateCard(targetCard);

                cardInListAdapter.swapItems(currentPosition, targetPosition);

                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int currentPosition = viewHolder.getAdapterPosition();

                // delete
                CardInListAdapter.Item deletedItem = cardInListAdapter.getItems().get(currentPosition);
                deletedCards.add(new Pair<>(deletedItem, currentPosition));
                cardInListAdapter.deleteCard(currentPosition);

                Snackbar.make(rootLayout, "Card \"" + deletedItem.card.getTerm() + "\" will be deleted.", BaseTransientBottomBar.LENGTH_LONG).setAction("undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Pair<CardInListAdapter.Item, Integer> pair = deletedCards.pop();
                        cardInListAdapter.insertItem(pair.second, pair.first);
                    }
                }).show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addBackgroundColor(ContextCompat.getColor(CardsListActivity.this, R.color.colorDelete))
                        .addActionIcon(R.drawable.ic_delete_white_24dp)
                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        })).attachToRecyclerView(recyclerViewCards);

    }


    protected void setOtherViews() {
        findViewById(R.id.activityCardsListButtonInsertCard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditCardDialog(-1);
            }
        });

        rootLayout = findViewById(R.id.activityCardsListRootLayout);
        progressBar = findViewById(R.id.activityCardsListProgressBar);
    }


    protected void goBack() {
        if (reversedCollectionCreated && collectionEdited) {
            Intent intent = new Intent();
            intent.putExtra(EXTRA_CARDS_COLLECTION, cardsCollection);
            intent.putExtra(EXTRA_CREATED_CARDS_COLLECTION, createdCardsCollection);
            setResult(RESULT_CODE_COLLECTION_CHANGED_AND_CREATED, intent);
        } else if (reversedCollectionCreated) {
            Intent intent = new Intent();
            intent.putExtra(EXTRA_CREATED_CARDS_COLLECTION, createdCardsCollection);
            setResult(RESULT_CODE_COLLECTION_CREATED, intent);
        } else if (collectionEdited) {
            Intent intent = new Intent();
            intent.putExtra(EXTRA_CARDS_COLLECTION, cardsCollection);
            setResult(RESULT_CODE_COLLECTION_EDITED, intent);
        }

        finish();
    }


    protected void openCreateFileActivity() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, cardsCollection.getName() + "CardsCollection");
        startActivityForResult(intent, REQUEST_CODE_CREATE_COLLECTION_FILE);
    }


    @SuppressLint("StaticFieldLeak")
    protected class AsyncTaskExecutor extends android.os.AsyncTask<Object, Object, Void> {
        public static final int TASK_LOAD_CARDS = 0;
        public static final int TASK_CREATE_REVERSED_CARDS_COLLECTION = 1;
        public static final int TASK_CLEAR_COUNTERS = 2;
        public static final int TASK_DELETE_CARDS = 3;
        public static final int TASK_CREATE_COLLECTION_FILE = 4;

        public static final int ERROR_UPLOAD_COLLECTION_TO_FILE = 0;

        private int currentTask;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            rootLayout.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Object... objects) {
            // integers[0] - task id
            currentTask = (Integer) objects[0];

            if (currentTask == TASK_LOAD_CARDS) {
                cardInListAdapter.setCards(DataBaseHelper.getCards(cardsCollection.getId()));
            }
            else if (currentTask == TASK_CLEAR_COUNTERS) {
                Card editingCard;
                List<CardInListAdapter.Item> adapterItems = cardInListAdapter.getItems();
                for (int i = 0; i < adapterItems.size(); i++) {
                    editingCard = adapterItems.get(i).card;
                    editingCard.discardCounters();

                    publishProgress(i, editingCard);

                    DataBaseHelper.updateCardCounters(editingCard);
                }
            }
            else if (currentTask == TASK_CREATE_REVERSED_CARDS_COLLECTION) {
                createdCardsCollection = new CardsCollection(cardsCollection.getName() + " (r)", false, System.currentTimeMillis());
                createdCardsCollection.setId(DataBaseHelper.createCardsCollection(createdCardsCollection));

                Card newCard;
                for (Card card: cardInListAdapter.getCardsAll()) {


                    newCard = new Card(createdCardsCollection.getId());
                    newCard.setDefinition(card.getTerm());
                    newCard.setTerm(card.getDefinition());

                    DataBaseHelper.addCard(newCard);
                }
            }
            else if (currentTask == TASK_DELETE_CARDS) {
                while (!deletedCards.empty()) {
                    DataBaseHelper.deleteCard(deletedCards.pop().first.card.getId());
                }
            }
            else if (currentTask == TASK_CREATE_COLLECTION_FILE) {
                try {
                    Utils.writeToStream(
                            getContentResolver().openOutputStream((Uri) objects[1]),
                            Utils.cardsCollectionToText(cardsCollection.getName(), cardInListAdapter.getCardsAll())
                    );
                } catch (IOException | NullPointerException e) {
                    e.printStackTrace();
                    publishProgress(ERROR_UPLOAD_COLLECTION_TO_FILE);
                }
            }

            return null;
        }


        @Override
        protected void onProgressUpdate(Object... objects) {
            super.onProgressUpdate(objects);

            if (currentTask == TASK_CLEAR_COUNTERS) {
                cardInListAdapter.editCard((Integer) objects[0], (Card) objects[1]);
            } else if (currentTask == TASK_CREATE_COLLECTION_FILE && ((Integer) objects[0]) == ERROR_UPLOAD_COLLECTION_TO_FILE) {
                Toast.makeText(CardsListActivity.this, "Error while uploading collection.", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            rootLayout.setEnabled(true);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
