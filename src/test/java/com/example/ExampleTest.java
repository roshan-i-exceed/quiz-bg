package com.example;
 
import org.junit.jupiter.api.Test;
 
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
 
public class ExampleTest {
 
    @Test
    void quizAppFlow() throws InterruptedException {
 
        Playwright playwright = Playwright.create();
 
        Browser browser = playwright.chromium().launch(
            new BrowserType.LaunchOptions()
                .setHeadless(false)
                .setChannel("chrome")
                .setDevtools(true)
        );
 
        BrowserContext context = browser.newContext();
        Page page = context.newPage();
 
        // Open application
        page.navigate(System.getenv().getOrDefault("APPZILLON_URL", "http://localhost:8018/QuizFr/"));
 
        System.out.println("Quiz application opened successfully");
        page.locator("html").click();
    page.locator("#QuizFr__QuizzesList__el_btn_1_0").click();
    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Ok")).click();
    page.locator("#QuizFr__Questions__el_btn_4_1").click();
    page.locator("#QuizFr__Questions__ct_lst_6_row_1 #QuizFr__Questions__sc_col_15_li").click();
    page.locator("#QuizFr__Questions__el_btn_4_1").click();
    page.getByText("What is the highest-grossing").click();
    page.locator("#QuizFr__Questions__el_btn_4_2").click();
    page.locator("#QuizFr__Questions__ct_lst_6_row_1 #QuizFr__Questions__sc_col_15_li").click();
    page.locator("#QuizFr__Questions__el_btn_4_2").click();
    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Submit")).click();
    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Ok")).click();
    page.locator("#td_QuizFr__Questions__el_btn_4_1").click();
 
        String holdTime = System.getenv().getOrDefault("PLAYWRIGHT_HOLD_MS", "30000");
        Thread.sleep(Long.parseLong(holdTime));
 
        // DO NOT close browser while inspecting
        // browser.close();
        // playwright.close();
    }
}