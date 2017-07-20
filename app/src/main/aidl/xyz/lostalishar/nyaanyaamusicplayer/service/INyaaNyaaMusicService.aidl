// INyaaNyaaMusicService.aidl
package xyz.lostalishar.nyaanyaamusicplayer.service;

import xyz.lostalishar.nyaanyaamusicplayer.model.MusicPlaybackState;
import xyz.lostalishar.nyaanyaamusicplayer.model.MusicPlaybackTrack;

interface INyaaNyaaMusicService {
    boolean load(int queuePos);
    void play();
    void pause();
    void stop();
    void reset();
    MusicPlaybackState getState();
    List<MusicPlaybackTrack> getQueue();
    int addToQueue(long musicId);
    long removeFromQueue(int pos);
    boolean isPlaying();
}
