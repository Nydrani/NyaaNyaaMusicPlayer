package xyz.lostalishar.nyaanyaamusicplayer.interfaces;

import com.afollestad.materialcab.MaterialCab;

/**
 * Interface that allows a passable CAB
 */

public interface CabHolder {
    MaterialCab openCab(MaterialCab.Callback callback);
    void closeCab();
}
