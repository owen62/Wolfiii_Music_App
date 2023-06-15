package com.example.wolfii;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.example.wolfii.MainActivity.database;
import static com.example.wolfii.MainActivity.mService;


public class FragmentControleMusique<mTextStatus, mScrollView> extends Fragment {

    private SeekBar seekBarMusique;//SeekBar de lecture de la musique

    private TextView txtViewMusiqueTemps, txtViewMusiqueDuree, txtViewTitreMusique, txtViewAuteurMusique;   //TextView du temps de lecture de la musique

    private ImageView imgViewMusique, like, add;

    private ArrayList<Musique> currentPlaylist;

    private FragmentManager fragmentManager;

    private FragmentTransaction fragmentTransaction;

    private ShowCurrentPlaylistFragment showCurrentPlaylistFragment = new ShowCurrentPlaylistFragment ();


    private float rotationImageValeur=0f;
    private Handler handlerRotation = new Handler();
    private boolean imageRotationDejaInit = false;
    private boolean imageRotationEnPause = true;

    private static ClickOnLike clickOnLike = new ClickOnLike();



////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////FONCTIONS DU CYCLE DE VIE DE LA PAGE/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*------------------------------------------FONCTION ONCREATE-----------------------------------------------------*/
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_controle_musique, container, false);

        // on initialise le fragment manager
        fragmentManager = getActivity().getSupportFragmentManager ();

        //Liaisons des Boutons, des TextViews et du SeekBar de l'interface dans la code.
        this.txtViewMusiqueTemps = root.findViewById(R.id.txtViewMusiqueTemps);

        this.txtViewMusiqueDuree = root.findViewById(R.id.txtViewMusiqueDuree);

        this.txtViewTitreMusique = root.findViewById(R.id.txtViewTitreMusique);

        this.txtViewAuteurMusique = root.findViewById(R.id.txtViewAuteurMusique);

        this.like = root.findViewById (R.id.like);
        clickOnLike.setContext (getActivity ());
        clickOnLike.setLike (this.like);
        this.like.setOnClickListener (clickOnLike);

        this.add = root.findViewById (R.id.addToPlaylist);
        this.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mService.getMusiquePlayerIsSet())
                    ClickOnMusic.longClickMusic(mService.maMusique.get(mService.getPositionMusique ()), getActivity());
            }
        });

        this.seekBarMusique=(SeekBar) root.findViewById(R.id.seekBarMusique);
        this.seekBarMusique.setSoundEffectsEnabled(false);
        this.seekBarMusique.setOnSeekBarChangeListener(new EcouteurSeekBar());

        this.imgViewMusique = (ImageView) root.findViewById(R.id.imgViewLogo);

        //Enregistrement du receiver pour la mise à jour de l'interface
        IntentFilter intentFilter = new IntentFilter(MusiqueService.DIRECTION_ACTIVITY);
        getActivity().registerReceiver(broadcastReceiverMajInterface, intentFilter);

        currentPlaylist = mService.getCurrentPlaylist ();

        showCurrentPlaylistFragment.setMaMusique(currentPlaylist);

        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.listes, showCurrentPlaylistFragment, null);
        fragmentTransaction.commit();


        if (mService.getMusiquePlayerIsSet()) {
            majInterfaceInit();//Mise à jour de l'interface
        }

        demaPauseRotationImage();

        return root;
    }


    /*--------------------------------------FONCTION ONPAUSE------------------------------------------------*/

    @Override
    public void onPause() {
        super.onPause();
        arretRotationImage();
        imageRotationEnPause=true;
    }


    /*--------------------------------------FONCTION ONRESUME------------------------------------------------*/

    @Override
    public void onResume() {
        super.onResume();
        if (mService.getMusiquePlayerIsSet())
            majInterfaceInit();
        else
            majInterfaceFin();

        imageRotationEnPause=false;
        demaPauseRotationImage();
    }

    /*--------------------------------------FONCTION ONDESTROY------------------------------------------------*/
    @Override
    public void onDestroy() {
        super.onDestroy();
        //Arrêt broadcast receiver de mise à jour de l'interface
        getActivity().unregisterReceiver(broadcastReceiverMajInterface);
    }




////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////GESTION ROTATION IMAGE/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*--------------------------------------RUNNABLE DE ROATION DE L'IMAGE------------------------------------------------*/
    private Runnable runnableTempsRotationImage = new Runnable() {
        @Override
        public void run() {
            if (rotationImageValeur>=360)
                rotationImageValeur=0;

            rotationImageValeur += 0.2f;
            imgViewMusique.setRotation(rotationImageValeur);
            handlerRotation.postDelayed(runnableTempsRotationImage,20);
        }
    };

    /*--------------------------------------DEMARRER ROTATION IMAGE------------------------------------------------*/
    public void demaPauseRotationImage()
    {
        if (!imageRotationEnPause) {
            if (mService.getMusiquePlayerIsSet()) {
                if (mService.getMusiquePlayerIsPlaying() && !imageRotationDejaInit) {
                    handlerRotation.post(runnableTempsRotationImage);
                    imageRotationDejaInit = true;
                } else if (!mService.getMusiquePlayerIsPlaying()) {
                    arretRotationImage();
                }
            }
        }
    }

    /*--------------------------------------ARRETER ROTATION IMAGE------------------------------------------------*/
    public void arretRotationImage()
    {
        handlerRotation.removeCallbacks(runnableTempsRotationImage);
        imageRotationDejaInit=false;
    }




////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////FONCTIONS D'ACTION DES BOUTONS/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*--------------------------------------FONCTION/CLASS SEEKBAR------------------------------------------------*/

    private class EcouteurSeekBar implements SeekBar.OnSeekBarChangeListener {

        //Evenement qui s'enclenche sur le déplacement du seekbar
        public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser)
        {
            if (mService.getMusiquePlayerIsSet() && fromUser ) {
                mService.setMusiquePlayerPosition(progress);
                txtViewMusiqueTemps.setText(millisecondesEnMinutesSeconde(progress));
            }
        }

        //Evenement qui s'enclenche sur l'appuit sur le seekbar
        public void onStartTrackingTouch(SeekBar seekBar) {}

        //Evenement qui s'enclenche sur la fin du déplacement du seekbar
        public void onStopTrackingTouch(SeekBar seekBar) {
            // On place la mise à jour une fois qu'on a finis de déplacer le seekbar (évite un
            // rechargement du mediasession nombreux et inutile, car en plus qu'il n'est pas visible lorsqu'on
            // déplace le seekbar de cette page)
            if (mService.getMusiquePlayerIsSet()) {
                mService.mediaSessionBoutonsMaj();
            }
        }
    }




////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////FONCTIONS MAJ INTERFACE/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*----------------------------------GESTION BROADCASTRECEIVER--------------------------------------------------*/

    private BroadcastReceiver broadcastReceiverMajInterface = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getStringExtra(MusiqueService.TYPE_MAJ)) {
                case MusiqueService.EXTRA_MAJ_INIT:
                    majInterfaceInit();//Mise à jour de l'interface au démarrage de la page
                    break;
                case MusiqueService.EXTRA_MAJ_INFOS:
                    majInterface();//Mise à jour de l'interface
                    break;
                case MusiqueService.EXTRA_MAJ_FIN:
                    majInterfaceFin();//Mise à jour interface d'arrêt de la lecture de musiques
                    break;
                case MusiqueService.EXTRA_MAJ_BOUTONS:
                    demaPauseRotationImage();
                    break;
            }
        }
    };


    public void majInterfaceInit() {
        showCurrentPlaylistFragment.setPositionMusique (mService.getPositionMusique ());
        currentPlaylist = mService.getCurrentPlaylist();
        seekBarMusique.setMax(mService.getMusiquePlayerDuration());
        imgViewMusique.setImageBitmap(mService.recupImageMusiquePageControle());
        txtViewMusiqueDuree.setText(millisecondesEnMinutesSeconde(mService.getMusiquePlayerDuration()));
        txtViewTitreMusique.setText(mService.getMusiqueTitre());
        txtViewAuteurMusique.setText(mService.getMusiqueAuteur());
        majInterface();
    }


    public void majInterface() {
        clickOnLike.setMusique(mService.maMusique.get(mService.getPositionMusique()));

        if (database.mainDao().getLikes().contains(mService.getMusiquePlayerPath()))
            this.like.setImageBitmap(drawableEnBitmap(R.drawable.like_white));
        else
            this.like.setImageBitmap(drawableEnBitmap(R.drawable.unlike_white));

        showCurrentPlaylistFragment.setPositionMusique(mService.getPositionMusique());
        seekBarMusique.setProgress(mService.getMusiquePlayerPosition());
        txtViewMusiqueTemps.setText(millisecondesEnMinutesSeconde(mService.getMusiquePlayerPosition()));
    }

    @SuppressLint("SetTextI18n")
    public void majInterfaceFin()
    {
        txtViewTitreMusique.setText("");
        txtViewAuteurMusique.setText("");
        txtViewMusiqueDuree.setText("00:00");
        txtViewMusiqueTemps.setText("00:00");
        seekBarMusique.setProgress(0);
        arretRotationImage();
        imgViewMusique.setRotation(0);
        imgViewMusique.setImageBitmap(drawableEnBitmap(R.drawable.logostyle));
        rotationImageValeur=0;
    }



////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////AUTRES FONCTIONS/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*--------------------------------------CONVERSION TEMPS EN MILLISECONDE EN MINTES ET SECONDES------------------------------------------------*/

    @SuppressLint("DefaultLocale")
    private String millisecondesEnMinutesSeconde(int tmpsMillisecondes) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(tmpsMillisecondes),
                TimeUnit.MILLISECONDS.toSeconds(tmpsMillisecondes) - TimeUnit.MILLISECONDS.toMinutes(tmpsMillisecondes) * 60);
    }



    /*--------------------------------------CONVERSION DRAWABLE EN BITMAP------------------------------------------------*/

    public Bitmap drawableEnBitmap (int drawableRes) {
        @SuppressLint("UseCompatLoadingForDrawables") Drawable drawable = getResources().getDrawable(drawableRes);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}