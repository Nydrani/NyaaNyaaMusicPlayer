package xyz.lostalishar.nyaanyaamusicplayer.interfaces;

import android.view.ActionMode;

/**
 * Interface that allows a passable CAB
 */

public interface CabHolder {
    ActionMode openCab(int menuRes, ActionMode.Callback callback);
}
