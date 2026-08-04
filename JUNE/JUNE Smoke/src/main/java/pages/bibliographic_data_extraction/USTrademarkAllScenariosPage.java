package pages.bibliographic_data_extraction;

import org.openqa.selenium.WebDriver;
import pages.JuneChatPage;

/**
 * Page Object for the US Trademark workflow.
 * All common chat interactions are inherited from {@link JuneChatPage}.
 *
 * @author Yash Shrivastava
 */
public class USTrademarkAllScenariosPage extends JuneChatPage {

	public USTrademarkAllScenariosPage(WebDriver driver) {
		super(driver);
	}

	@Override
	public void selectDropdownOptionByLabel(String fieldLabel, String optionText) {
		openDropdownAndSelect("US Trademark");
	}

	@Override
	protected String getWorkflowName() {
		return "US Trademark";
	}
}
