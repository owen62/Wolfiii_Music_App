package com.example.wolfii;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Wolfii;

import java.util.ArrayList;
import java.util.List;

import static com.example.wolfii.MainActivity.database;
import static com.example.wolfii.MainActivity.mService;
import static com.example.wolfii.MainActivity.mesMusiques;
import static com.example.wolfii.MusiqueService.maMusique;

public class ClickOnArtist implements MyStringAdapter.ArtisteItemClickListener {

    private MyMusiqueAdapter monMusiqueAdapter;
    private ArrayList<Musique> musiques;
    private RecyclerView mRecyclerView;
    private Context context;
    private ImageView shuffleiv;
    private List<String> hiddenTitle = database.mainDao ().getHiddenTitle ();
    public static ArrayList<String> addToPlaylistsArray = new ArrayList<> ();



    public void setRecyclerViewForMusic(RecyclerView rv) { mRecyclerView = rv; }
    public void setContext(Context sContext){context = sContext;}
    public void setShuffle(ImageView shuffle) {this.shuffleiv = shuffle;}

    @Override
    public void onArtisteItemClick (View view, String artiste, int position) {
        musiques = HideMusic (recuperer_musique (artiste));

        shuffleiv.setVisibility (View.VISIBLE);

        shuffleiv.setVisibility (View.VISIBLE);
        ClickOnShuffle shuffle = new ClickOnShuffle ();
        shuffle.setContext (context);
        shuffle.setmRecyclerView (mRecyclerView);
        shuffle.setPlaylist (musiques);
        shuffleiv.setOnClickListener (shuffle);

        monMusiqueAdapter = new MyMusiqueAdapter (musiques, context);
        ClickOnMusic clicker = new ClickOnMusic();
        clicker.setMesMusiques (musiques);
        clicker.setContext (context);
        monMusiqueAdapter.setmMusiqueItemClickListener (clicker);
        mRecyclerView.setAdapter (monMusiqueAdapter);
    }
    ArrayList<Musique> HideMusic(ArrayList<Musique> musiques){
        ArrayList<Musique> trueList = new ArrayList<Musique> ();
        for(Musique m : musiques)
            if(!hiddenTitle.contains (m.getPath ()))
                trueList.add(m);
        return trueList;
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onArtisteItemLongClick (View view, String artiste, int position) {
        musiques = recuperer_musique (artiste);

        Dialog dialog = new Dialog(context);

        // set content view
        dialog.setContentView(R.layout.ajouter_a_une_playlist);

        // initialize width and height
        int width = WindowManager.LayoutParams.MATCH_PARENT;
        int height = WindowManager.LayoutParams.WRAP_CONTENT;
        //set layout
        dialog.getWindow().setLayout(width, height);
        dialog.show ();

        Button addToPlaylist = dialog.findViewById (R.id.add);
        Button addToCurrentPlaylist = dialog.findViewById (R.id.addToCurrentPlaylist);
        Button hiddenTitle = dialog.findViewById (R.id.hiddenTitle);
        RecyclerView rv = dialog.findViewById (R.id.myRecyclerView);

        hiddenTitle.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                for (Musique musique : musiques) {
                    DataHiddenMusic dataHiddenMusic = new DataHiddenMusic ();
                    dataHiddenMusic.setPath (musique.getPath ());

                    database.mainDao ().insertMusic (musique);
                    database.mainDao ().insertHiddenTitle (dataHiddenMusic);
                }
            }
        });

        ArrayList<String> mesPlaylists = (ArrayList<String>) database.mainDao ().getAllPlaylists ();
        Log.d("debug_playlist", mesPlaylists.toString ());
        MyStringAdapter adapter = new MyStringAdapter (mesPlaylists);
        adapter.setIsLongClickMusic(true);
        rv.setLayoutManager(new LinearLayoutManager (context.getApplicationContext(), LinearLayout.VERTICAL, false));

        rv.setAdapter (adapter);

        addToPlaylist.setOnClickListener(new View.OnClickListener() {
            public void onClick (View v) {
                for(String playlist : addToPlaylistsArray) {
                    for(Musique musique : musiques) {
                        DataPlaylist dataPlaylist = new DataPlaylist ();
                        dataPlaylist.setNom (playlist);

                        DataPlaylistMusic dataPlaylistMusic = new DataPlaylistMusic ();
                        dataPlaylistMusic.setPath (musique.getPath ());
                        dataPlaylistMusic.setPlaylist (playlist);

                        database.mainDao ().insertMusic (musique);
                        database.mainDao ().insertPlaylist (dataPlaylist);
                        database.mainDao ().insertPlaylistMusic (dataPlaylistMusic);

                        Log.d ("debug_data", dataPlaylist.getNom ());

                        dialog.dismiss ();
                    }
                }

            }
        });

        addToCurrentPlaylist.setOnClickListener(new View.OnClickListener() {
            public void onClick (View v) {
                for(Musique musique : musiques) maMusique.add (musique);

                mService.setMusiquePlaylist(maMusique, 0);
                mService.arretSimpleMusique();
                mService.musiqueDemaPause();

                dialog.dismiss ();
            }
        });
    }
    private ArrayList<Musique> recuperer_musique (String artiste) {
        // on recupere toutes les musiques selon l'artiste qui nous interesse
        ArrayList<Musique> musiques = new ArrayList<> ();
        for (Musique m : mesMusiques) if (m.getAuthor ().equals (artiste)) musiques.add (m);
        return musiques;
    }
}