package com.rolandoislas.greedygreedy.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.rolandoislas.greedygreedy.android.util.AchievementHandler;
import com.rolandoislas.greedygreedy.core.GreedyClient;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new GreedyClient(new AchievementHandler()), config);
	}
}
