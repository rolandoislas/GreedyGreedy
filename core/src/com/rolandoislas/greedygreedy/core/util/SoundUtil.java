package com.rolandoislas.greedygreedy.core.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;

public class SoundUtil implements Disposable {
    private final Sound soundTimeUp;
    private final Sound soundRoll;
    private final Sound soundZilchWarning;
    private final Sound soundZilch;
    private final Sound soundStop;

    public SoundUtil() {
        soundTimeUp = Gdx.audio.newSound(Gdx.files.internal("audio/time_up.mp3"));
        soundRoll = Gdx.audio.newSound(Gdx.files.internal("audio/roll.mp3"));
        soundZilchWarning = Gdx.audio.newSound(Gdx.files.internal("audio/zilch_warn.mp3"));
        soundZilch = Gdx.audio.newSound(Gdx.files.internal("audio/zilch.mp3"));
        soundStop = Gdx.audio.newSound(Gdx.files.internal("audio/stop.mp3"));
    }

    @Override
    public void dispose() {
        soundTimeUp.dispose();
        soundRoll.dispose();
        soundZilchWarning.dispose();
        soundZilch.dispose();
        soundStop.dispose();
    }

    public void playTimeUp() {
        soundTimeUp.play();
    }

    public void playZilchWarning() {
        soundZilchWarning.play();
    }

    public void playRoll() {
        soundRoll.play();
    }

    public void playZilch() {
        soundZilch.play();
    }

    public void playStop() {
        soundStop.play();
    }
}
