package pages.bibliographic_data_extraction;

import org.openqa.selenium.WebDriver;
import pages.JuneChatPage;

/**
 * Page Object for the AU Patent workflow.
 * All common chat interactions are inherited from {@link JuneChatPage}.
 *
 * @author Yash Shrivastava
 */
public class AUPatentAllScenariosPage extends JuneChatPage {

	public AUPatentAllScenariosPage(WebDriver driver) {
		super(driver);
	}

	@Override
	public void selectDropdownOptionByLabel(String fieldLabel, String optionText) {
		openDropdownAndSelect("AU Patent");
	}

	@Override
	protected String getWorkflowName() {
		return "AU Patent";
	}
}
