// INyaaNyaaMusicService.aidl
package xyz.lostalishar.nyaanyaamusicplayer.service;

import xyz.lostalishar.nyaanyaamusicplayer.model.MusicPlaybackTrack;

interface INyaaNyaaMusicService {
    boolean load(int queuePos);
    void start();
    void pause();
    void stop();
    void reset();
    List<MusicPlaybackTrack> getQueue();
    int addToQueue(long musicId);
    void removeFromQueue(long musicId);
}
