package edu.utap.sharein

import edu.utap.sharein.model.Song

class SongReposit {
    private var songResources = hashMapOf(
        R.raw.american_dream to Song("American Dream", R.raw.american_dream, "1:58"),
        R.raw.cinematic to Song ("Cinematic", R.raw.cinematic, "3:46"),
        R.raw.nocturne to Song ("Nocturne", R.raw.nocturne, "3:44")
    )

    fun fetchSongs(): HashMap<Int, Song> {
        return songResources
    }
}