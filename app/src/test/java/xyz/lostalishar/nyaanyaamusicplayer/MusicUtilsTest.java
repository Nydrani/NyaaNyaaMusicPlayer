package xyz.lostalishar.nyaanyaamusicplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import xyz.lostalishar.nyaanyaamusicplayer.service.MusicPlaybackService;
import xyz.lostalishar.nyaanyaamusicplayer.util.MusicUtils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;

public class MusicUtilsTest {
    @Mock
    private Context context;

    private Intent serviceIntent;
    private ComponentName componentName;


    @Before
    public void before() {
        context = Mockito.mock(Context.class);

        serviceIntent = new Intent(context, MusicPlaybackService.class);
        componentName = new ComponentName("testpkg", "testclass");

        Mockito.when(context.startService(serviceIntent)).thenReturn(componentName);
        Mockito.when(context.bindService(serviceIntent, any(ServiceConnection.class), anyInt())).thenReturn(true);

        MusicUtils.startService(context);
        MusicUtils.bindToService(context);
    }

    @Test
    public void testStartServiceSuccess() throws Exception {
        assertEquals(componentName, MusicUtils.startService(context));
    }

}