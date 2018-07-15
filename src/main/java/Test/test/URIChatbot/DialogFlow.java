package Test.test.URIChatbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;

public class DialogFlow {
	private Logger logger = LoggerFactory.getLogger(DialogFlow.class);
		public String dialog(String text) {
			String[] args = {"017c5fd9b64a4f2ca68debd74d10bbac"};
			AIConfiguration configuration = new AIConfiguration(args[0]);
			logger.info(configuration.getApiKey());

			AIDataService dataService = new AIDataService(configuration);
			logger.info(dataService.toString());

			String rsult = "";
			try {
				AIRequest request = new AIRequest(text);
				logger.info(request.getSessionId());

				AIResponse response = dataService.request(request);
				logger.info(response.getStatus().getCode().toString());

				String speechResponse = response.getResult().getFulfillment().getSpeech();
				if (response.getStatus().getCode() == 200) {
					rsult = speechResponse;
				}
				else {
					rsult =  "badInput";
				}
			}
			catch(Exception ex) {
				logger.info(ex.getMessage());
				ex.printStackTrace();
			}
			return rsult;
		
	}

}
