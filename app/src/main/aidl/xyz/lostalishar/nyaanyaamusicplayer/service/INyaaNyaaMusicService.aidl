// INyaaNyaaMusicService.aidl
package xyz.lostalishar.nyaanyaamusicplayer.service;

interface INyaaNyaaMusicService {
    boolean load(long musicId);
    void start();
    void pause();
    void stop();
    void reset();
    long[] getQueue();
    void addToQueue(long musicId);
    void removeFromQueue(long musicId);
}
