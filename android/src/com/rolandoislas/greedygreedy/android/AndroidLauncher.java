package com.rolandoislas.greedygreedy.android;

import android.os.Bundle;
import android.view.View;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.rolandoislas.greedygreedy.android.auth.AuthenticationHandlerAndroid;
import com.rolandoislas.greedygreedy.android.util.AchievementHandler;
import com.rolandoislas.greedygreedy.android.util.AdHandler;
import com.rolandoislas.greedygreedy.core.GreedyClient;
import com.rolandoislas.greedygreedy.core.stage.StageLoad;
import com.rolandoislas.greedygreedy.core.stage.StageLogin;
import com.rolandoislas.greedygreedy.core.util.ArgumentParser;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		AdHandler adHandler = new AdHandler(this, config);
		View libGdxView = initializeForView(new GreedyClient(
						new ArgumentParser(new String[]{"-f"}),
						new AchievementHandler(),
						new AuthenticationHandlerAndroid(this),
						adHandler),
				config);
		adHandler.initialize(libGdxView);
		// Auth code check
		if (getIntent().getData() != null) {
			for (String paramName : getIntent().getData().getQueryParameterNames()) {
				if (paramName.equals("code")) {
					final String code = getIntent().getData().getQueryParameter("code");
					Gdx.app.postRunnable(new Runnable() {
						@Override
						public void run() {
							GreedyClient.setStage(new StageLogin(new StageLoad(), new StageLoad(), code));
						}
					});
				}
			}
		}
	}
}
