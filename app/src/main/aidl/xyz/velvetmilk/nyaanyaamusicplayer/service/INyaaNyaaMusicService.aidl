// INyaaNyaaMusicService.aidl
package xyz.velvetmilk.nyaanyaamusicplayer.service;

interface INyaaNyaaMusicService {
    boolean load(long musicId);
    void start();
    void pause();
    void stop();
    void reset();
}
