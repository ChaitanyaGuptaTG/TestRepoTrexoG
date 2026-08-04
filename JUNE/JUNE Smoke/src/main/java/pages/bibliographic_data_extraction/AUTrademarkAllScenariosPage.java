package pages.bibliographic_data_extraction;

import org.openqa.selenium.WebDriver;
import pages.JuneChatPage;

/**
 * Page Object for the AU Trademark workflow.
 * All common chat interactions are inherited from {@link JuneChatPage}.
 *
 * @author Yash Shrivastava
 */
public class AUTrademarkAllScenariosPage extends JuneChatPage {

	public AUTrademarkAllScenariosPage(WebDriver driver) {
		super(driver);
	}

	@Override
	public void selectDropdownOptionByLabel(String fieldLabel, String optionText) {
		openDropdownAndSelect("AU Trademark");
	}

	@Override
	protected String getWorkflowName() {
		return "AU Trademark";
	}
}
