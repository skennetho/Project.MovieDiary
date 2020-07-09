package com.example.moviediary;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;


public class LocalDBHelper extends SQLiteOpenHelper {
    final static String tag = "디비헬퍼";
    //MovieDiaryDB(title, year , diary, quotes, song, date) <-시간상 포기
    //MovieDiaryDB(mTitle, mYear , diary, lastDate,mPoster)
    private SQLiteDatabase database;
    private static LocalDBHelper myDBManager = null;
    FireBase firebase;

    public LocalDBHelper(Context context) {
        super(context, "MovieDiaryDB", null, 1);
        firebase = new FireBase();
    }

    //싱글톤패턴
    public static LocalDBHelper getInstance(Context context)
    {
        if(myDBManager == null)
        {
            myDBManager = new LocalDBHelper(context);
        }
        return myDBManager;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE if not exists MovieDiaryDB (" +
                "mTitle TEXT ," +
                "mYear INTEGER, " +
                "diary TEXT, " +
                "lastDate TEXT, " +
                "mPoster TEXT not null," +
                "PRIMARY KEY(mTItle, mYear) );");
        Log.d(tag, "onCreate()");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists MovieDiaryDB");
        onCreate(db);
    }

    //디비에 영화 생성
    public void addMovieInDB(Item movie){
        database = this.getWritableDatabase();
        database.execSQL("Insert into movieDiaryDB Values('"+
                movie.getTitle()+"',"+
                movie.getPubDate() +", null, null, '"+movie.getImage()+"' )");
        Log.d(tag,"addMovieInDB "+movie.getTitle()+","+movie.getPubDate());
        firebase.createMovie(new FireBase.Movie(movie));
        firebase.updateUser();
    }

    public boolean hasMovie(String title, int year){
        database = this.getReadableDatabase();
        Cursor c = database.rawQuery("select * from MovieDiaryDB where mTitle ='" +title+"' AND mYear = "+year+";",null);
        if(c.getCount()>0) {//영화가 존재한다면
            return true;
        }else
            return false;
    }

    //디비에 있는 영화가 먼저 존재해야함.
    //수정을 통한 일기 저장
    public void updateDiary(String title, int year, String diary) {
        database = this.getWritableDatabase();
        try {
            if(hasMovie(title, year)) {
                database.execSQL("update MovieDiaryDB set diary = '" + diary + "', lastDate = strftime('%Y-%m-%d %H:%M:%S', 'now') " +
                        "where mTitle = '" + title + "' AND mYear = " + year + ";");
            }
        } catch (Exception e) {
            Log.d(tag, "saveDiary 에러" + e.toString());
        }
        Log.d(tag, "saveDiary :" + title + "," + year);
        firebase.createMovie(title, year+"");
        firebase.updateUser();
    }

    //삭제
    public void deleteMovie(String title, String year) {
        database = this.getWritableDatabase();
        try {
            database.execSQL("DELETE FROM MovieDiaryDB " +
                    "where mTitle = '" + title + "' AND " +
                    "mYear =" + year+ ";");
        } catch (Exception e) {
            Log.d(tag, "deleteDiary 에러 "+title+","+year +" : " + e.toString());
        }
        Log.d(tag, "deleteDiary 성공:" + title + "," +year);
        firebase.deleteMovie(title, year);
        firebase.updateUser();
    }

    //조회
    public ArrayList<Item> getAllDiary(){
        ArrayList<Item> list = new ArrayList<>();
        database= this.getWritableDatabase();
        Cursor cursor =  database.rawQuery("select * from MovieDiaryDB where diary NOTNULL order by lastDate  desc;", null);
        while(cursor.moveToNext()) {
            Item item = new Item("","","","","");
            item.setTitle(cursor.getString(0));
            item.setPubDate(cursor.getString(1));
            item.setDiary(cursor.getString(2));
            item.setDiaryDate(cursor.getString(3));
            item.setImage(cursor.getString(4));
            list.add(item);
        }
        Log.d(tag, "getAllDiary: "+cursor.getCount()+" showed");
        return list;
    }

    //일기 내용 가져오기
    public String getDiary(String title, int year){
        Cursor c= database.rawQuery("select diary from MovieDiaryDB where mTitle ='"+title+"' AND mYear ="+year+"; ", null);
        if(c.moveToNext()){
            return c.getString(0);
        }else {
            return "";
        }
    }

    //최근 10개 뽑기
    public void getMyTasteToday(){
        database= this.getReadableDatabase();
        Cursor c = database.rawQuery("select * from MovieDiaryDB where lastDate notnull order by lastDate desc LIMIT 10;", null);
        ArrayList<FireBase.Movie> myTaste = new ArrayList<>();
        while(c.moveToNext()){
            FireBase.Movie movie = new FireBase.Movie();
            movie.setTitle(c.getString(0));
            movie.setYear(c.getInt(1)+"");
            myTaste.add(movie);
        }
        StringBuffer log= new StringBuffer();
        for(FireBase.Movie i : myTaste){
            log.append("\n"+i.getTitle()+" , "+i.getYear());
        }
        Log.d(tag, log.toString());
        //파이어 베이스 에 10개올리기
    }

    public void closeLocalDBHelper(){
        this.close();
        database.close();
    }


}