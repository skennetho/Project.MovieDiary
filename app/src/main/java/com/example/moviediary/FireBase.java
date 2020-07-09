package com.example.moviediary;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FireBase {
    //** 영화의 document아이디는 이름:년도 로한다.

    private static String USER_NICK = "KangsobSo";
    private final String TAG = "<파이어베이스>";

    ArrayList<Movie> myMovieList = new ArrayList<>();
    static ArrayList<Movie> todaysRecommend = new ArrayList<>();
    ArrayList<ArrayList<Movie>> moviesForCompare = new ArrayList<ArrayList<Movie>>();

    FirebaseFirestore db;
    CollectionReference userDataRef;
    CollectionReference watchedRef;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date time = new Date();
    final int NUM_USER_SAMPLE = 4;
    final int NUM_MOV_SAMPLE = 7;

    public FireBase() {
        db = FirebaseFirestore.getInstance();
        userDataRef = db.collection("userData");
    }

    //파베에 유저 추가 하고 랜덤으로 생성된 유저아이디 저장
    public void createUser(String nickname) {
        USER_NICK = nickname;
        Map<String, String> map = new HashMap<>();
        map.put("nickName", USER_NICK);
        map.put("lastUpdate", dateFormat.format(time));
        userDataRef.document(USER_NICK).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "createUser  " + USER_NICK);
                } else {
                    Log.d(TAG, "createUser failure");
                }
            }
        });
    }

    public void updateUser() {
        Map<String, String> map = new HashMap<>();
        map.put("nickName", USER_NICK);
        map.put("lastUpdate", dateFormat.format(time));
        userDataRef.document(USER_NICK).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "createUser  " + USER_NICK);
                } else {
                    Log.d(TAG, "createUser failure");
                }
            }
        });
    }

    public void createMovie(final Movie mov) {
        watchedRef = userDataRef.document(USER_NICK).collection("watchedMovies");
        watchedRef.document(mov.getTitle() + ":" + mov.getYear())
                .set(mov)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!myMovieContains(mov)) myMovieList.add(mov);
                    }
                });
    }

    public void createMovie(String title, String year) {
        final Movie mov = new Movie(title, year, dateFormat.format(time));
        watchedRef = userDataRef.document(USER_NICK).collection("watchedMovies");
        watchedRef.document(mov.getTitle() + ":" + mov.getYear())
                .set(mov)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!myMovieContains(mov)) myMovieList.add(mov);
                    }
                });
    }

    public void deleteMovie(String title, String year) {
        watchedRef = userDataRef.document(USER_NICK).collection("watchedMovies");
        watchedRef.document(title + ":" + year).delete();
        deleteFromMyMovieList(new Movie(title, year, ""));
    }

    public void deleteMovie(Movie mov) {
        watchedRef = userDataRef.document(USER_NICK).collection("watchedMovies");
        watchedRef.document(mov.getTitle() + ":" + mov.getYear()).delete();
    }

    public void readAllMovies() {
        Log.d(TAG, " readAllMovies : 시작=============================================");
        todaysRecommend.clear();
        moviesForCompare.clear();
        userDataRef.orderBy("lastUpdate", Query.Direction.DESCENDING).limit(NUM_USER_SAMPLE).get()      //샘플갯수만큼 샘플 구하기
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "readAllMovies 성공" + task.getResult().size());
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                if (doc.getId().equals(USER_NICK)) {
                                    continue;
                                }
                                Log.d(TAG, doc.getId() + " => " + doc.getData());
                                getMoviesFrom(doc.getId());
                            }

                        } else {
                            Log.d(TAG, "readAllMovies 실패");
                        }
                    }
                });
    }

    public void getMoviesFrom(final String documentId) {
        watchedRef = userDataRef.document(documentId).collection("watchedMovies");
        watchedRef.orderBy("time", Query.Direction.DESCENDING).limit(NUM_MOV_SAMPLE).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("로그 getMoviesFrom", USER_NICK+" != " +documentId + ":" + task.getResult().size());
                            ArrayList<Movie> tastes = new ArrayList<>();
                            Movie mov;
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                Log.d("로그 getMoviesFrom", doc.getId() + " => " + doc.getData());
                                tastes.add(
                                        new Movie(
                                                doc.getData().get("title") + "",
                                                doc.getData().get("year") + "",
                                                doc.getData().get("time") + ""
                                        ));
                            }
                            moviesForCompare.add(tastes);
                        } else {
                            Log.d("로그 getMoviesFrom", "실패");
                        }
                        Log.d("로그 getMoviesFrom", "==============");
                    }
                });
    }


    //영화추천 뽑기
    public ArrayList<Movie> getRecommend() {
        int max = 0;
        int location = 0;
        int count = 0;
        for (ArrayList<Movie> list : moviesForCompare) {
            int i = getHowSimilar(list);
            Log.d(TAG, "getRecommend list: " + list.toString() + " 비교점수:" + i + "," + location);
            if (i > max) {
                max = i;
                location = count;
            }
            count++;
        }
        todaysRecommend.clear();
        if (moviesForCompare.get(location).size() > 0) {
            for (Movie mov : moviesForCompare.get(location)) {
                if (myMovieContains(mov)) {
                    continue;
                } else {
                    todaysRecommend.add(mov);
                }
            }
        }
        Log.d("내 비교대상", moviesForCompare.get(location).toString());
        Log.d("내 영화목록", myMovieList.toString());
        Log.d("오늘의 영화 추천", todaysRecommend.toString());
        return todaysRecommend;
    }

    //표본과의 유사도 구하기
    public int getHowSimilar(ArrayList<Movie> list) {
        int count = 0;
        for (Movie v : list) {
            for (Movie mov : myMovieList) {
                if (mov.equals(v)) count++;
            }
        }
        if (count == NUM_MOV_SAMPLE) {    //만일 나랑 너무 같은 사람이면 패쓰
            count = 0;
        }
        return count;
    }

    public void setMyMovieList(ArrayList<Item> list) {
        myMovieList.clear();
        for (Item item : list) {
            myMovieList.add(
                    new Movie(
                            item.getTitle(),
                            item.getPubDate(),
                            item.getDiaryDate()
                    ));
        }
    }

    //나의 영화 목록 가져오기
    public void updateMyMovieList() {
        watchedRef = userDataRef.document(USER_NICK).collection("watchedMovies");
        watchedRef.orderBy("time", Query.Direction.DESCENDING).limit(NUM_MOV_SAMPLE).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("로그 getMoviesFrom", USER_NICK + ":" + task.getResult().size());
                            myMovieList.clear();
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                if (doc.getId().equals(USER_NICK)) {
                                    continue;
                                }
                                Log.d("로그 getMoviesFrom", doc.getId() + " => " + doc.getData());
                                myMovieList.add(
                                        new Movie(
                                                doc.getData().get("title") + "",
                                                doc.getData().get("year") + "",
                                                doc.getData().get("time") + ""
                                        ));
                            }
                        } else {
                            Log.d("로그 getMoviesFrom", "실패");
                        }
                        Log.d("로그 getMoviesFrom", "==============");
                    }
                });
    }

    public void updataeFromDocument(Movie mov, String docId) {
        DocumentReference dRef = db.collection("userData").document(USER_NICK)
                .collection("watchedMovies").document(docId);
        dRef.set(mov);
    }

    public String getTimeNow() {
        return dateFormat.format(time);
    }

    public boolean myMovieContains(Movie mov) {
        for (Movie m : myMovieList) {
            if (m.equals(mov)) return true;
        }
        return false;
    }

    public void deleteFromMyMovieList(Movie mov) {
        int i = getIndexFromMyMovieList(mov);
        if (i >= 0) {
            myMovieList.remove(i);
        }
    }

    public int getIndexFromMyMovieList(Movie mov) {
        for (int i = 0; i < myMovieList.size(); i++) {
            if (myMovieList.get(i).equals(mov)) return i;
        }
        return -1;
    }

    //////////////////////////////////////////////////////////////////////////////
    static class Movie {

        private String title;
        private String year;
        private String time;

        public Movie(String title, String year, String time) {
            this.title = title;
            this.year = year;
            this.time = time;
        }

        public Movie(Item item) {
            this.year = item.getPubDate();
            this.title = item.getTitle();
            this.time = item.getDiaryDate();
        }

        public Movie(Map<String, Object> map) {
            this.title = "" + map.get("title");
            this.year = "" + map.get("year");
            this.time = "" + map.get("time");
        }

        public Movie() {

        }

        public boolean equals(Movie mov) {
            if (mov.getTitle().equals(title) && mov.getYear().equals(year)) {
                return true;
            } else {
                return false;
            }
        }

        public String toString() {
            return "<" + time + " :" + title + " : " + year + ">\n";
        }

        //firestore에 인자로 클래스를 넘겨주려면 모든 필드에대해 get()이 필요하다.
        public String getTitle() {
            return this.title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getYear() {
            return this.year;
        }

        public void setYear(String year) {
            this.year = year;
        }

        public String getTime() {
            return this.time;
        }

        public void setTime(String time) {
            this.year = time;
        }
    }
}

