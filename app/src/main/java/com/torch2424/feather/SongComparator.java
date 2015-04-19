package com.torch2424.feather;

import android.media.MediaMetadataRetriever;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Class to compare two files, retrieve their meta data and sort them
 */
public class SongComparator implements Comparator<File>
{

    //Our metdata retriever
    private MediaMetadataRetriever mmr = new MediaMetadataRetriever();

    //Our compared int, to return a positive or negative value
    int compared;

    //Arrays to store our album and track number
    String[] array1 = new String[2];
    String[] array2 = new String[2];

    @Override
    public int compare(File song1, File song2)
    {
        //Place into a try/catch block to catch our exceptions and return 0
        try {
            //Check if they are not music files
            //Do it this way to avoid 'violating the comparator general contract'
            if (!Manly.isMusic(song1)) return -10;
            else if(!Manly.isMusic(song2))return 10;

            //Since they are music

            //Get our metaData album, song

            //Get the album for song 1 and 2
            mmr.setDataSource(song1.getAbsolutePath());
            array1[0] = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            mmr.setDataSource(song2.getAbsolutePath());
            array2[0] = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);

            //Check they are not null
            //Do it this way to avoid 'violating the comparator general contract'
            if (array1[0] == null)  return -10;
            else if(array2[0] == null) return 10;

            //Compare their albums
            compared = array1[0].compareTo(array2[0]);

            //Our check if we can stop here
            if (compared < 0) return 10;
            else if (compared > 0) return -10;

            //Since their albums are equal

            //Get the song number for song 1 and 2
            mmr.setDataSource(song1.getAbsolutePath());
            array1[1] = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER);
            mmr.setDataSource(song2.getAbsolutePath());
            array2[1] = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER);

            //Check they are not null
            //Do it this way to avoid 'violating the comparator general contract'
            if (array1[1] == null)  return -10;
            else if(array2[1] == null) return 10;

            //Compare their songs
            //First split up the string we get, and return the answer
            //Get the string of the first half of a cd number (eg. 1/10) and convert it
            //and integer
            int trackno1 = Integer.valueOf(array1[1].split("/")[0]);
            int trackno2 = Integer.valueOf(array2[1].split("/")[0]);
            //now compare the track numbers to get compared
            if (trackno1 > trackno2) return 10;
            else if (trackno1 < trackno2) return -10;
            else return 0;
        }
        catch(Exception e)
        {
            //Return 0 if there is an exception
            return 0;
        }
    }
}
