package pages.bibliographic_data_extraction;

import org.openqa.selenium.WebDriver;
import pages.JuneChatPage;

/**
 * Page Object for the US Patent workflow.
 * All common chat interactions are inherited from {@link JuneChatPage}.
 *
 * @author Yash Shrivastava
 */
public class USPatentAllScenariosPage extends JuneChatPage {

	public USPatentAllScenariosPage(WebDriver driver) {
		super(driver);
	}

	@Override
	public void selectDropdownOptionByLabel(String fieldLabel, String optionText) {
		openDropdownAndSelect("US Patent");
	}

	@Override
	protected String getWorkflowName() {
		return "US Patent";
	}
}
