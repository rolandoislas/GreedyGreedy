package com.rolandoislas.greedygreedy.core.util;

import com.rolandoislas.greedygreedy.core.data.Constants;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Rolando on 2/18/2017.
 */
public class ArgumentParser {
	private final List<String> argsList;
	public final boolean logDebug;
	public final boolean logExtra;
	public final boolean logVerbose;
	public final boolean localCallback;
	public final String preferencesProfile;

    public ArgumentParser(String[] args) {
		argsList = Arrays.asList(args);
		if (hasOption("-h", "--help"))
			showHelp();
		logDebug = hasOption("-d", "--debug");
		logExtra = hasOption("-e", "--extra", "-f", "--finer");
		logVerbose = hasOption("-v", "--verbose");
		localCallback = hasOption("--local-callback");
		preferencesProfile = getArgAfter("-profile");
	}

	private void showHelp() {
		System.out.printf("%s v%s is licensed under the GPLv2 license.\n", Constants.NAME, Constants.VERSION);
		System.out.printf("\nUsage: java -jar greedygreedy.jar [args] [flags]\n");
		System.out.printf("\nArgs\n");
		System.out.printf("\t-profile <name>: Use a specified user profile");
		System.out.printf("\nFlags\n");
		System.out.printf("\t--local-callback: Use localhost for the API/server calls");
		System.out.printf("\nLogging\n");
		System.out.printf("\t-d, --debug: debug logging\n");
		System.out.printf("\t-e, --extra: extra logging\n");
		System.out.printf("\t-f, --finer: most details are logged\n");
		System.out.printf("\t-v, --verbose: console spam\n");
		System.exit(1);
	}

	private boolean hasOption(String... options) {
		for (String opt : options)
			if (argsList.contains(opt))
				return true;
		return false;
	}

	private String getArgAfter(String arg) {
		if (!argsList.contains(arg)
				|| argsList.indexOf(arg) + 1 >= argsList.size()
				|| argsList.get(argsList.indexOf(arg) + 1).startsWith("-"))
			return "";
		return argsList.get(argsList.indexOf(arg) + 1);
	}

	public ArgumentParser() {
		this(new String[]{});
	}
}
