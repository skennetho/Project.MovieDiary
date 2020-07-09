package com.example.moviediary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MakeDiaryActivity extends AppCompatActivity {
    final String tag = "일기작성";
    //Diary(title, year , diary, quotes, song)
    TextView movieInfo;     //일기의 제목
    String title;
    String diary="", image="";
    int year;
    Item movieItem;
    LocalDBHelper dbHelper;
    boolean isEditing = true;
    EditText edDiary;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);
        movieInfo = findViewById(R.id.tvMovieInfo);
        edDiary = findViewById(R.id.edDiary);
        setTitle("일기작성");

        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        year = Integer.parseInt(intent.getStringExtra("year"));


        dbHelper = LocalDBHelper.getInstance(this);
        if(!dbHelper.hasMovie(title,year)) {
            Log.d(tag, "영화정보 디비에 없음 : 새로일기 생성");
            isEditing=false;                //처음 작성하는 일기
        }else{
            Log.d(tag, "영화정보 디비에 존재 : 수정모드 확인 ");
            edDiary.setText( dbHelper.getDiary(title,year));
        }

        movieInfo.setText(title+"\n"+year);

        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //일기 저장
                saveDiary();

            }
        });
    }
    public void onPause(){
        super.onPause();
        //일기 저장
        if(!isEditing) {
            saveDiary();
        }
    }

    public void saveDiary(){
        Log.d(tag, "저장~");
        diary = edDiary.getText().toString();
        dbHelper.updateDiary(title, year, diary);
        Intent intent = new Intent(MakeDiaryActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
