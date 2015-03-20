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
    @Override
    public int compare(File song1, File song2)
    {
        //Our metdata retriever
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();

        //First get our metaData, artist, album, song
        ArrayList<String> array1 = new ArrayList<String>();
        ArrayList<String> array2 = new ArrayList<String>();

        //Get all of the data for Song 1
        mmr.setDataSource(song1.getAbsolutePath());
        array1.add(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
        array1.add(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER));

        //Get all of the data for Song 2
        mmr.setDataSource(song2.getAbsolutePath());
        array2.add(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
        array2.add(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER));

        //Now sort by album, then track. Not doing artist for songs with like a million artists
        for(int i = 0; i < 2; ++i)
        {
            int compared = 0;
            //Compare album name
            if(i == 0) compared = array1.get(i).compareTo(array2.get(i));
                //Compare track number, but need to get substring of a number divided by another
            else
            {
                //Get the string of the first half of a cd number (eg. 1/10) and convert it
                //and integer
                int trackno1 = Integer.valueOf(array1.get(i).split("/")[0]);
                int trackno2 = Integer.valueOf(array2.get(i).split("/")[0]);
                //now compare the track numbers to get compared
                if(trackno1 == trackno2) compared = 0;
                else if(trackno1 < trackno2) compared = -10;
                else compared = 10;
            }
            if(compared == 0)
            {
                if(i == 1)
                {
                    return 0;
                }
            }
            else if(compared < 0)
            {
                return -10;
            }
            else
            {
                return 10;
            }
        }

        //Failsafe incase for some odd reason nothing was returned above
        return 0;

    }
}
