package pages.bibliographic_data_extraction;

import org.openqa.selenium.WebDriver;
import pages.JuneChatPage;

/**
 * Page Object for the EP Patent workflow.
 * All common chat interactions are inherited from {@link JuneChatPage}.
 *
 * @author Yash Shrivastava
 */
public class EPPatentAllScenariosPage extends JuneChatPage {

	public EPPatentAllScenariosPage(WebDriver driver) {
		super(driver);
	}

	@Override
	public void selectDropdownOptionByLabel(String fieldLabel, String optionText) {
		openDropdownAndSelect("EP Patent");
	}

	@Override
	protected String getWorkflowName() {
		return "EP Patent";
	}
}
