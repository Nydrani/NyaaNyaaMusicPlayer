package xyz.lostalishar.nyaanyaamusicplayer;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import xyz.lostalishar.nyaanyaamusicplayer.model.MusicPlaybackState;
import xyz.lostalishar.nyaanyaamusicplayer.service.MusicPlaybackService;
import xyz.lostalishar.nyaanyaamusicplayer.util.PreferenceUtils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class PreferenceUtilsTest {
    @Mock
    private SharedPreferences sharedPreferences;
    @Mock
    private SharedPreferences sharedBrokenPreferences;
    @Mock
    private SharedPreferences.Editor sharedPreferencesEditor;
    @Mock
    private SharedPreferences.Editor sharedPreferencesBrokenEditor;
    @Mock
    private Context context;

    private static final int TEST_POS = 1234;
    private static final int TEST_SEEK_POS = 4321;


    @Before
    public void before() {
        context = Mockito.mock(Context.class);
        sharedPreferences = Mockito.mock(SharedPreferences.class);
        sharedPreferencesEditor = Mockito.mock(SharedPreferences.Editor.class);
        sharedBrokenPreferences = Mockito.mock(SharedPreferences.class);
        sharedPreferencesBrokenEditor = Mockito.mock(SharedPreferences.Editor.class);

        Mockito.when(sharedPreferences.edit()).thenReturn(sharedPreferencesEditor);
        Mockito.when(sharedBrokenPreferences.edit()).thenReturn(sharedPreferencesBrokenEditor);

        Mockito.when(sharedPreferences.getInt(
                PreferenceUtils.SERVICE_QUEUE_PLAYING_POS, MusicPlaybackService.UNKNOWN_POS))
                .thenReturn(TEST_POS);
        Mockito.when(sharedPreferences.getInt(
                PreferenceUtils.SERVICE_QUEUE_PLAYING_SEEKPOS, 0)).thenReturn(TEST_SEEK_POS);
        Mockito.when(sharedBrokenPreferences.getInt(
                PreferenceUtils.SERVICE_QUEUE_PLAYING_POS, MusicPlaybackService.UNKNOWN_POS))
                .thenReturn(MusicPlaybackService.UNKNOWN_POS);
        Mockito.when(sharedBrokenPreferences.getInt(PreferenceUtils.SERVICE_QUEUE_PLAYING_SEEKPOS, 0))
                .thenReturn(0);
    }

    @Test
    public void testLoadCurPlayingSuccess() throws Exception {
        Mockito.when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences);

        assertEquals(new MusicPlaybackState(TEST_POS, TEST_SEEK_POS), PreferenceUtils.loadCurPlaying(context));
    }

    @Test
    public void testLoadCurPlayingFail() throws Exception {
        Mockito.when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedBrokenPreferences);

        assertEquals(new MusicPlaybackState(), PreferenceUtils.loadCurPlaying(context));
    }

    // assumes loadCurPlaying
    @Test
    public void testSaveCurPlayingSuccess() throws Exception {
        Mockito.when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences);

        MusicPlaybackState testState = new MusicPlaybackState(TEST_POS, TEST_SEEK_POS);

        // save and load test position to test they are same
        PreferenceUtils.saveCurPlaying(context, testState);
        assertEquals(testState, PreferenceUtils.loadCurPlaying(context));
    }

    @Test
    public void testSaveCurPlayingFail() throws Exception {
        Mockito.when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedBrokenPreferences);

        // save a test position
        PreferenceUtils.saveCurPlaying(context, new MusicPlaybackState(TEST_POS, TEST_SEEK_POS));

        // test position should have failed so loadCurPlaying returns a default state
        assertEquals(new MusicPlaybackState(), PreferenceUtils.loadCurPlaying(context));
    }
}