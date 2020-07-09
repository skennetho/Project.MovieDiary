package com.example.moviediary;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.w3c.dom.Text;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    //GZiC5ZEdU34yt4wyYTtQ - client id
    //uTW3YBItYQ - client secret
//추천알고리즘 - 파이어베이스 디비 사용
//단순 일기 저장 - sqlite 사용
    EditText tv;
    TextView tvUser;

    ArrayList<Item> diaryList = new ArrayList<>();
    ArrayList<Item> recommendList = new ArrayList<>();
    RecyclerView recyclerView;
    RecyclerVIewAdapter adapter;
    GridLayoutManager gridLayoutManager;
    LocalDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("영화일기");
        setContentView(R.layout.activity_main);
        tvUser = findViewById(R.id.tvUserNick);

        dbHelper = new LocalDBHelper(this);
        if (!isUser()) {
            dialogNickName();
        } else {
            dbHelper.firebase.createUser(getUser());
            tvUser.setText(getUser());
        }

        Button btn = findViewById(R.id.btnMakeDiary);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MovieSearchActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
        Button btnRecommend = findViewById(R.id.btnRecommend);
        btnRecommend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ArrayList<FireBase.Movie> list = dbHelper.firebase.getRecommend();  //앱시작과 동시에 추천할 영화들 목록불러오기
                if(!list.isEmpty())
                    getMovies(list.get(0).getTitle(), Integer.parseInt(list.get(0).getYear()));
                new Handler().postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        dialogRecommend();
                        dbHelper.firebase.readAllMovies();
                    }
                }, 1100);// 0.5초 정도 딜레이를 준 후 시작


            }
        });
        Button btn1 = findViewById(R.id.button);
        btn1.setText("NICK");
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogNickName();
            }
        });

        recyclerView = findViewById(R.id.diaryRecyclerView);
        gridLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
    }

    public void onResume() {
        super.onResume();
        diaryList = dbHelper.getAllDiary();
        dbHelper.firebase.readAllMovies();
        dbHelper.firebase.setMyMovieList(diaryList);

        adapter = new RecyclerVIewAdapter(this, diaryList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter.notifyDataSetChanged();




    }

    public Boolean isUser() {
        SharedPreferences sp = getSharedPreferences("nickName", MODE_PRIVATE);
        if (sp.getString("nickName", "").equals("") || sp.getString("nickName", "") == null) {
            return false;
        }
        ;
        return true;
    }

    public String getUser() {
        SharedPreferences sp = getSharedPreferences("nickName", MODE_PRIVATE);
        return sp.getString("nickName", "");
    }

    public void setUser(String nickName) {
        SharedPreferences sp = getSharedPreferences("nickName", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("nickName", nickName);
        editor.commit();
    }

    public void dialogNickName() {
        final View dView = View.inflate(getApplicationContext(), R.layout.dialog_nickname, null);
        tv = dView.findViewById(R.id.edNickname);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("닉네임 생성하기");
        dialog.setMessage("닉네임 필수");
        dialog.setView(dView);

        dialog.setPositiveButton("생성", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setUser(tv.getText().toString());
                dbHelper.firebase.createUser(getUser());
                tvUser.setText(getUser());
            }
        });
        dialog.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialog.show();
    }

    public void getMovies(final String title, final int year) {
        Log.e("영화검색", "시작 ============");
        ApiInterface apiInterface = ServiceGenerator.createService(ApiInterface.class);
        Call<MovieInfo> call = apiInterface.getMovies(title, 2, 1, year);
        call.enqueue(new Callback<MovieInfo>() {
            @Override
            public void onResponse(Call<MovieInfo> call, Response<MovieInfo> response) {
                if (response.isSuccessful()) {
                    ArrayList<Item> movies = new ArrayList(response.body().getItems());
                    if (movies.size() == 0) {
                        showNotFoundMessage(title);
                    } else {
                        movies.get(0).setTitle(movies.get(0).getTitle().replaceAll("<b>|</b>", ""));
                        recommendList.add(movies.get(0));
                    }
                    Log.e("영화검색", "성공 ============");
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

    public void showNotFoundMessage(String keyword) {
        Toast.makeText(this, "추천영화가 없습니다...", Toast.LENGTH_SHORT).show();
    }

    public void dialogRecommend() {
        final View dView = View.inflate(this, R.layout.dialog_movie_info, null);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("영화추천");
        dialog.setView(dView);

        if (recommendList.size() > 0)
            for (Item item : recommendList) {
                Log.d("영화 추천 목록", item.getTitle() + " => " + item.getPubDate());
            }

        if (recommendList.size() > 0) {
            ImageView iv = dView.findViewById(R.id.ivPoster);
            Glide.with(this)
                    .load(recommendList.get(0).getImage())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(iv);
            TextView tv = dView.findViewById(R.id.tvMovieInfo);
            tv.setText(recommendList.get(0).getTitle() + "\n" + recommendList.get(0).getPubDate());
        } else {
            dialog.setMessage("추천영화 없음...");
        }
        dialog.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialog.show();
    }
}

class RecyclerVIewAdapter extends RecyclerView.Adapter<RecyclerVIewAdapter.MyViewHolder> {

    Context context;
    final ArrayList<Item> list;

    public RecyclerVIewAdapter(Context context, ArrayList<Item> list) {
        super();
        this.context = context;
        this.list = list;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.setItem(list.get(position));
        holder.title.setText(list.get(position).getTitle() + "\n(" + list.get(position).getPubDate() + ")");
        Glide.with(context)
                .load(list.get(position).getImage())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.image);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie2, parent, false);
        return new MyViewHolder(view);
    }

    public void refresh(Item item) {
        list.remove(item);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView title;
        Context context;
        Item movieItem = new Item();

        public MyViewHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();
            image = itemView.findViewById(R.id.imageView);
            title = itemView.findViewById(R.id.textView);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    dialogAskDelete(v);
                    return false;
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogDetail(v);
                }
            });
        }

        public void setItem(Item item) {
            movieItem = item;
        }

        public void dialogAskDelete(View v) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(v.getContext());
            dialog.setTitle(title.getText().toString());
            dialog.setMessage("해당영화를 삭제하시겠습니까?");
            dialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            dialog.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteMovie();
                }
            });
            dialog.show();
        }

        public void deleteMovie() {
            new LocalDBHelper(context).deleteMovie(movieItem.getTitle(), movieItem.getPubDate());
            refresh(movieItem);

        }

        public void dialogDetail(View v) {
            final View dView = View.inflate(v.getContext(), R.layout.dialog_movie_info, null);

            ImageView iv = dView.findViewById(R.id.ivPoster);
            iv.setImageDrawable(image.getDrawable());
            TextView tv = dView.findViewById(R.id.tvMovieInfo);
            tv.setText(movieItem.getDiary() + "\n" + movieItem.getDiaryDate());

            AlertDialog.Builder dialog = new AlertDialog.Builder(v.getContext());
            dialog.setTitle(title.getText().toString());
            dialog.setView(dView);

            dialog.setPositiveButton("일기수정", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(dView.getContext(), MakeDiaryActivity.class);

                    intent.putExtra("title", movieItem.getTitle());
                    intent.putExtra("year", movieItem.getPubDate());
                    intent.putExtra("isEditing", true);     //ture: 수정모드

                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    dView.getContext().startActivity(
                            intent);
                }
            });
            dialog.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            dialog.show();
        }
    }


}