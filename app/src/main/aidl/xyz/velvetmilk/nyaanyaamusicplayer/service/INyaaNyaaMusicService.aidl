// INyaaNyaaMusicService.aidl
package xyz.velvetmilk.nyaanyaamusicplayer.service;

interface INyaaNyaaMusicService {
    void load(long musicId);
    void start();
    void pause();
    void stop();
}
