package xyz.lostalishar.nyaanyaamusicplayer;

import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeoutException;

import xyz.lostalishar.nyaanyaamusicplayer.service.MusicPlaybackService;

import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

@RunWith(AndroidJUnit4.class)
public class MusicServiceTest {
    @Rule
    public final ServiceTestRule serviceTestRule = new ServiceTestRule();

    @Test
    public void testWithBoundService() throws TimeoutException, RemoteException {
        // Create the service Intent.
        Intent serviceIntent =
                new Intent(InstrumentationRegistry.getTargetContext(), MusicPlaybackService.class);

        // AutoBind the service
        IBinder binder = serviceTestRule.bindService(serviceIntent);

        // grab the service stub
        MusicPlaybackService.NyaaNyaaMusicServiceStub serviceStub = (MusicPlaybackService.NyaaNyaaMusicServiceStub)binder;

        // Verify that the service is working correctly.
        assertThat(serviceStub.isPlaying(), is(any(Boolean.class)));
    }
}
