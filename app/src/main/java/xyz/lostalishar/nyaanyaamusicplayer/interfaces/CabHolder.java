package xyz.lostalishar.nyaanyaamusicplayer.interfaces;

import android.view.ActionMode;

/**
 * Interface that allows a passable CAB
 */

public interface CabHolder {
    ActionMode openCab(ActionMode.Callback callback);
    void closeCab();
    boolean isCabOpen();
}
