package com.example.wolfii;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.ArrayList;
import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface MainDao {
    /*
    @Insert(onConflict = REPLACE)
    void createPlaylist(PlaylistData playlistData);
    // insert query
    @Insert(onConflict = REPLACE)
    void insert(MainData mainData);

    // delete query
    @Delete
    void delete(MainData mainData);

    // delete all query
    @Delete
    void reset(List<MainData> mainData);

    @Query ("DELETE FROM music WHERE playlist = :sPlaylist;")
    void deletePlaylistFromMusic(String sPlaylist);

    @Query ("DELETE FROM playlist WHERE nom = :sPlaylist;")
    void deletePlaylistFromPlaylist(String sPlaylist);

    @Query ("UPDATE music SET playlist = :newName WHERE playlist = :sPlaylist;")
    void renameFromMusic(String sPlaylist, String newName);

    @Query ("UPDATE playlist SET nom = :newName WHERE nom = :sPlaylist;")
    void renameFromPlaylist(String sPlaylist, String newName);

    // supprimer une musique depuis son path
    @Query ("DELETE FROM music WHERE path = :sPath;")
    void deleteFromPath(String sPath);

    // get all data query
    @Query("SELECT * FROM music")
    List<MainData> getAll();

    @Query ("SELECT nom FROM playlist;")
    List<String> getAllPlaylists();

    @Query ("SELECT * FROM music WHERE playlist= :sPlaylist;")
    List<MainData> getMusicFromPlaylist(String sPlaylist);

    @Insert (onConflict = REPLACE)
    void insertLike(LikeData data);

    @Query ("SELECT path FROM likedMusic;")
    List<String> getLikes();

    @Query ("DELETE FROM likedMusic WHERE path= :sPath;")
    void deleteLike(String sPath);

    @Query ("SELECT path FROM hiddenTitle")
    List<String> getHiddenTitle();

    @Insert(onConflict = REPLACE)
    void insertHiddenTitle(HiddenTitleData data);
     */

    @Query ("SELECT * FROM LikedMusic")
    List<String> getLikes();

    @Query ("DELETE FROM Music WHERE path = :sPath;")
    void deleteFromPath(String sPath);

    @Query ("DELETE FROM PlaylistMusic WHERE playlist = :sPlaylist;")
    void deleteFromPlaylistMusicWherePlaylist(String sPlaylist);

    @Insert (onConflict = REPLACE)
    void insertLike(DataLikedMusic dataLikedMusic);

    @Insert(onConflict = REPLACE)
    void insertHiddenTitle(DataHiddenMusic dataHiddenMusic);

    @Insert(onConflict = REPLACE)
    void insertMusic(Musique musique);

    @Insert(onConflict = REPLACE)
    void insertPlaylist(DataPlaylist dataPlaylist);

    @Insert(onConflict = REPLACE)
    void insertPlaylistMusic(DataPlaylistMusic dataPlaylistMusic);

    @Delete
    void deleteLike(DataLikedMusic dataLikedMusic);

    @Query ("DELETE FROM Playlist WHERE nom = :sPlaylist")
    void deletePlaylist(String sPlaylist);

    @Query("SELECT * FROM playlist")
    List<String> getAllPlaylists();

    @Query ("SELECT * FROM HiddenMusic")
    List<String> getHiddenTitle();

    @Query ("SELECT * FROM music m JOIN playlistmusic pm ON m.path = pm.path WHERE pm.playlist= :sPlaylist;")
    List<Musique> getMusicFromPlaylist(String sPlaylist);

    @Query ("UPDATE playlist SET nom = :newName WHERE nom = :oldName")
    void renamePlaylist(String oldName, String newName);
}
