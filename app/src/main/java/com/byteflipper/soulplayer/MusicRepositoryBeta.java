package com.byteflipper.soulplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MusicRepositoryBeta {

    public static class Track {
        public long id;
        public String title;
        public String artist;
        public String album;
        public long duration;
        public String genre;
        public String data;
    }

    public static class Artist {
        public long id;
        public String name;
        public List<Track> tracks = new ArrayList<>();
    }

    public static class Album {
        public long id;
        public String name;
        public String artist;
        public List<Track> tracks = new ArrayList<>();
    }

    public static class Playlist {
        public long id;
        public String name;
        public List<Track> tracks = new ArrayList<>();
    }

    public static List<Track> getTracks(Context context) {
        List<Track> tracks = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER
        );
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Track track = new Track();

                // Проверка индекса столбца
                int titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                if (titleIndex != -1) {
                    track.title = cursor.getString(titleIndex);
                }
                int artistIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                if (artistIndex != -1) {
                    track.artist = cursor.getString(artistIndex);
                }
                int albumIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                if (albumIndex != -1) {
                    track.album = cursor.getString(albumIndex);
                }
                int durationIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
                if (durationIndex != -1) {
                    track.duration = cursor.getLong(durationIndex);
                }
                int genreIndex = cursor.getColumnIndex(MediaStore.Audio.Media.GENRE);
                if (genreIndex != -1) {
                    track.genre = cursor.getString(genreIndex);
                }
                int dataIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                if (dataIndex != -1) {
                    track.data = cursor.getString(dataIndex);
                }

                // Добавьте track в список, даже если некоторые значения не были получены
                tracks.add(track);
            }
            cursor.close();
        }
        return tracks;
    }

    public static List<Artist> getArtists(Context context) {
        List<Artist> artists = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                MediaStore.Audio.Artists.ARTIST
        );
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Artist artist = new Artist();
                int idIndex = cursor.getColumnIndex(MediaStore.Audio.Artists._ID);
                if (idIndex != -1) {
                    artist.id = cursor.getLong(idIndex);
                }
                int nameIndex = cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST);
                if (nameIndex != -1) {
                    artist.name = cursor.getString(nameIndex);
                }
                artists.add(artist);
            }
            cursor.close();
        }
        return artists;
    }

    public static List<Album> getAlbums(Context context) {
        List<Album> albums = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                MediaStore.Audio.Albums.ALBUM
        );
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Album album = new Album();
                int idIndex = cursor.getColumnIndex(MediaStore.Audio.Albums._ID);
                if (idIndex != -1) {
                    album.id = cursor.getLong(idIndex);
                }
                int nameIndex = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM);
                if (nameIndex != -1) {
                    album.name = cursor.getString(nameIndex);
                }
                int artistIndex = cursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST);
                if (artistIndex != -1) {
                    album.artist = cursor.getString(artistIndex);
                }
                albums.add(album);
            }
            cursor.close();
        }
        return albums;
    }

    public static List<Playlist> getPlaylists(Context context) {
        List<Playlist> playlists = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                MediaStore.Audio.Playlists.NAME
        );
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Playlist playlist = new Playlist();
                int idIndex = cursor.getColumnIndex(MediaStore.Audio.Playlists._ID);
                if (idIndex != -1) {
                    playlist.id = cursor.getLong(idIndex);
                }
                int nameIndex = cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME);
                if (nameIndex != -1) {
                    playlist.name = cursor.getString(nameIndex);
                }
                playlists.add(playlist);
            }
            cursor.close();
        }
        return playlists;
    }

    public static void populateData(Context context) {
        List<Track> tracks = getTracks(context);
        List<Artist> artists = getArtists(context);
        List<Album> albums = getAlbums(context);
        List<Playlist> playlists = getPlaylists(context);

        for (Track track : tracks) {
            for (Artist artist : artists) {
                if (track.artist.equals(artist.name)) {
                    artist.tracks.add(track);
                }
            }
            for (Album album : albums) {
                if (track.album.equals(album.name)) {
                    album.tracks.add(track);
                }
            }
        }

        ContentResolver contentResolver = context.getContentResolver();
        for (Playlist playlist : playlists) {
            Cursor cursor = contentResolver.query(
                    MediaStore.Audio.Playlists.Members.getContentUri("external", playlist.id),
                    null,
                    null,
                    null,
                    null
            );
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int trackIdIndex = cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID);
                    if (trackIdIndex != -1) {
                        long trackId = cursor.getLong(trackIdIndex);
                        for (Track track : tracks) {
                            if (track.id == trackId) {
                                playlist.tracks.add(track);
                                break;
                            }
                        }
                    }
                }
                cursor.close();
            }
        }

        // Сортировка треков в альбомах по номерам
        for (Album album : albums) {
            Collections.sort(album.tracks, new Comparator<Track>() {
                @Override
                public int compare(Track track1, Track track2) {
                    int trackNumber1 = 0;
                    try {
                        trackNumber1 = getTrackNumber(track1.data);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    int trackNumber2 = 0;
                    try {
                        trackNumber2 = getTrackNumber(track2.data);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    if (trackNumber1 != -1 && trackNumber2 != -1) {
                        return trackNumber1 - trackNumber2;
                    } else {
                        return track1.title.compareToIgnoreCase(track2.title);
                    }
                }
            });
        }
    }

    // Функция для извлечения номера трека
    private static int getTrackNumber(String filePath) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        int trackNumber = -1;

        try {
            retriever.setDataSource(filePath);
            String trackNumberString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS);
            if (trackNumberString != null) {
                trackNumber = Integer.parseInt(trackNumberString);
            }
        } catch (NumberFormatException e) {
            // Обработка ошибок при преобразовании строки в целое число
            e.printStackTrace();
        } catch (Exception e) {
            // Обработка других ошибок
            e.printStackTrace();
        } finally {
            retriever.release();
        }
        return trackNumber;
    }
}