package com.example.moviediary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieSearchActivity extends AppCompatActivity {
    private RecyclerView mRecycleView;
    private RecyclerView.LayoutManager mLayoutManager;

    private MovieViewAdapter mMovieAdapter;

    private EditText editSearch;
    private Button btnSearch;

    private InputMethodManager mInputMethodManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_movie);
        setTitle("영화검색");

        setupRecyclerView();
        setupSearchView();
    }

    private void setupRecyclerView() {
        mRecycleView = findViewById(R.id.recycleView);
        mRecycleView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecycleView.setLayoutManager(mLayoutManager);

        // 어댑터 설정
        ArrayList<Item> movies = new ArrayList<>();
        mMovieAdapter = new MovieViewAdapter(this, movies);
        mRecycleView.setAdapter(mMovieAdapter);

        // 구분선 추가
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this,
                new LinearLayoutManager(this).getOrientation());
        mRecycleView.addItemDecoration(dividerItemDecoration);
    }

    private void setupSearchView() {
        editSearch = findViewById(R.id.editSearch);
        btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                startSearch(editSearch.getText().toString());
                Log.d("영화검색", editSearch.getText().toString());
            }
        });
        mInputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
    }


    public void hideKeyboard() {
        mInputMethodManager.hideSoftInputFromWindow(mRecycleView.getWindowToken(), 0);
    }

    public void showEmptyFieldMessage() {
        Toast.makeText(this, "검색어를 입력해주세요", Toast.LENGTH_SHORT).show();
    }

    public void showNotFoundMessage(String keyword) {
        Toast.makeText(this, "\'" + keyword + "\' 를 찾을 수 없습니다", Toast.LENGTH_SHORT).show();
    }

    // 검색어가 입력되었는지 확인 후 영화 가져오기
    public void startSearch(String title) {
        if (title.isEmpty()) {
            showEmptyFieldMessage();
        } else {
            mLayoutManager.scrollToPosition(0);
            getMovies(title);
        }
    }

    // 영화 가져오기
    public void getMovies(final String title) {
        ApiInterface apiInterface = ServiceGenerator.createService(ApiInterface.class);
        Call<MovieInfo> call = apiInterface.getMovies(title, 100, 1,0);
        call.enqueue(new Callback<MovieInfo>() {
            @Override
            public void onResponse(Call<MovieInfo> call, Response<MovieInfo> response) {
                if (response.isSuccessful()) {
                    ArrayList<Item> movies = new ArrayList(response.body().getItems());

                    if (movies.size() == 0) {
                        mMovieAdapter.clearItems();
                        showNotFoundMessage(title);
                    } else {
                        mMovieAdapter.clearAndAddItems(movies);
                    }
                } else {
                    Log.e("영화검색", response.message());
                }
            }

            @Override
            public void onFailure(Call<MovieInfo> call, Throwable t) {
                Log.e("영화검색", t.getMessage());
            }
        });
    }
}


class MovieViewAdapter extends RecyclerView.Adapter<MovieViewHolder> {
    private Context mContext;

    private ArrayList<Item> mMovieInfoArrayList;

    public MovieViewAdapter(Context context, ArrayList<Item> movieInfoArrayList) {
        mContext = context;
        mMovieInfoArrayList = movieInfoArrayList;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        Item item = mMovieInfoArrayList.get(position);
        holder.movieTitle.setText(item.getTitle());
        holder.movieRating.setRating(Float.parseFloat(item.getUserRating()) / 2);
        holder.movieYear.setText(item.getPubDate());
        holder.movieDirector.setText(item.getDirector());
        holder.movieActor.setText(item.getActor());
        Glide.with(mContext)
                .load(item.getImage())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.getImageView());
        holder.setItem(item);
        holder.movieImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mMovieInfoArrayList.size();
    }

    public void clearItems() {
        mMovieInfoArrayList.clear();
        notifyDataSetChanged();
    }

    public void clearAndAddItems(ArrayList<Item> items) {
        mMovieInfoArrayList.clear();
        //여기서 영화이름 <b></b>표시 지워주기
        for (Item movie : items) {
            movie.setTitle(movie.getTitle().replaceAll("<b>|</b>", ""));
        }
        mMovieInfoArrayList.addAll(items);
        notifyDataSetChanged();
    }
}


class MovieViewHolder extends RecyclerView.ViewHolder {
    public ImageView movieImage;
    public TextView movieTitle;
    public TextView movieYear; // published date =pubDate
    public TextView movieDirector;
    public TextView movieActor;
    public RatingBar movieRating;
    public View holderView;

    Item movieItem = new Item();

    MovieViewHolder(View view) {
        super(view);
        holderView = view;
        movieImage = view.findViewById(R.id.iv_poster);
        movieTitle = view.findViewById(R.id.tv_title);
        movieRating = view.findViewById(R.id.rb_user_rating);
        movieYear = view.findViewById(R.id.tv_pub_data);
        movieDirector = view.findViewById(R.id.tv_director);
        movieActor = view.findViewById(R.id.tv_actor);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View dView = View.inflate(v.getContext(), R.layout.dialog_movie_info, null);

                LocalDBHelper dbHelper = LocalDBHelper.getInstance(v.getContext());
                if (dbHelper.hasMovie(movieItem.getTitle(), Integer.parseInt(movieItem.getPubDate()))) {

                } else {
                    dbHelper.addMovieInDB(movieItem);
                }

                ImageView iv = dView.findViewById(R.id.ivPoster);
                iv.setImageDrawable(movieImage.getDrawable());
                TextView tv = dView.findViewById(R.id.tvMovieInfo);
                tv.setText(infoToString());

                AlertDialog.Builder dialog = new AlertDialog.Builder(v.getContext());
                dialog.setTitle(movieTitle.getText().toString());
                dialog.setView(dView);

                dialog.setPositiveButton("일기작성", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(dView.getContext(), MakeDiaryActivity.class);

                        intent.putExtra("title", movieTitle.getText().toString());
                        intent.putExtra("year", movieYear.getText().toString());
                        intent.putExtra("isEditing", false);

                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        dView.getContext().startActivity(intent);
                    }
                });
                dialog.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                dialog.show();
            }
        });
    }

    public void setItem(Item item) {
        movieItem = item;
    }

    public ImageView getImageView() {
        return movieImage;
    }

    public String infoToString() {
        return "Title: " + movieTitle.getText().toString() +
                "\nYear: " + movieYear.getText().toString() +
                "\nDirector(s):" + movieDirector.getText().toString() +
                "\nActor(s):" + movieActor.getText().toString();
    }

}

