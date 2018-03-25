package ai.api.examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;

public class Test {

	private static final String INPUT_PROMPT = "> ";

	private static final int ERROR_EXIT_CODE = 1;

	public static void main(String[] args) {
		if (args.length < 1) {
			showHelp("Please specify API key", ERROR_EXIT_CODE);
		}

		AIConfiguration configuration = new AIConfiguration(args[0]);

		AIDataService dataService = new AIDataService(configuration);
		Seperate sper = new Seperate();
		String line;

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
			System.out.print(INPUT_PROMPT);
			while (null != (line = reader.readLine())) {

				try {

					AIRequest request = new AIRequest(line);

					AIResponse response = dataService.request(request);
					String speechResponse = response.getResult().getFulfillment().getSpeech();
					if (response.getStatus().getCode() == 200) {
						
						if (!speechResponse.equals("")) {
							System.out.println(speechResponse);
						} else {
							sper.apiai(line);
						}
					} else {
						System.err.println(response.getStatus().getErrorDetails());
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				System.out.print(INPUT_PROMPT);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		System.out.println("See ya!");
	}

	/**
	 * Output application usage information to stdout and exit. No return from
	 * function.
	 * 
	 * @param errorMessage
	 *            Extra error message. Would be printed to stderr if not null and
	 *            not empty.
	 * 
	 */
	private static void showHelp(String errorMessage, int exitCode) {
		if (errorMessage != null && errorMessage.length() > 0) {
			System.err.println(errorMessage);
			System.err.println();
		}

		System.out.println("Usage: APIKEY");
		System.out.println();
		System.out.println("APIKEY  Your unique application key");
		System.out.println("        See https://docs.api.ai/docs/key-concepts for details");
		System.out.println();
		System.exit(exitCode);
	}
}