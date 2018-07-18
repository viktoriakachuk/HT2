package by.epam.selenium.test;

import by.epam.selenium.driver.WebDriverSingleton;
import by.epam.selenium.login.Login;
import by.epam.selenium.page.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class TestClass {
    private WebDriver driver = WebDriverSingleton.getInstance();
    static final String USERNAME = "someuser";
    static final String PASSWORD = "somepassword";
    static final String FULLNAME = "Some Full Name";
    List<String> buttonColorList;


    @Before
    public void setUp(){
        driver.get("localhost:8080");
        buttonColorList = new ArrayList<>();
        LoginPage loginPage = PageFactory.initElements(driver, LoginPage.class);
        buttonColorList.add(loginPage.getLoginButtonColor());
        Login loginObj = new Login();
        loginObj.login(loginPage);
    };

    @Test
    public void jenkinsInteractionTest() throws InterruptedException {
        WelcomePage welcomePage = PageFactory.initElements(driver,WelcomePage.class);
        welcomePage.clickManage();
        ManageJenkinsPage mjPage = PageFactory.initElements(driver,ManageJenkinsPage.class);
        JavascriptExecutor jse = (JavascriptExecutor)driver;
        jse.executeScript("window.scrollBy(0,900)", "");

        /*1.	После клика по ссылке «Manage Jenkins» на странице появляется элемент dt
        с текстом «Manage Users» и элемент dd с текстом «Create/delete/modify users that
        can log in to this Jenkins».*/
        Assert.assertEquals("Manage Users", mjPage.getDT());
        Assert.assertEquals("Create/delete/modify users that can log in to this Jenkins", mjPage.getDD());

        mjPage.clickManageUsers();
        ManageUsersPage muPage = PageFactory.initElements(driver,ManageUsersPage.class);

        /*2.	После клика по ссылке, внутри которой содержится элемент dt с текстом «Manage Users»,
        становится доступна ссылка «Create User».*/
        Assert.assertEquals("Create User", muPage.getCreateUserLink());

        muPage.clickCreateUser();
        CreateUserPage cuPage = PageFactory.initElements(driver,CreateUserPage.class);

        /*3.	После клика по ссылке «Create User» появляется форма с тремя полями
        типа text и двумя полями типа password, причём все поля должны быть пустыми.*/
        Assert.assertTrue(cuPage.isFormPresent());

        buttonColorList.add(cuPage.getCreateButtonColor());
        /*it is not specified what Jenkins version is required. My version (2.121.1) does not have
        an email field while creating user*/
        cuPage.fillinUsername(USERNAME)
                .fillinPassword(PASSWORD)
                .confirmPassword(PASSWORD)
                .fillinFullname(FULLNAME)
                .clickCreate();

        /*4.	После заполнения полей формы («Username» = «someuser», «Password» = «somepassword»,
        «Confirm password» = «somepassword», «Full name» = «Some Full Name», «E-mail address» =
        «some@addr.dom») и клика по кнопке с надписью «Create User» на странице появляется строка
        таблицы (элемент tr), в которой есть ячейка (элемент td) с текстом «someuser».*/
        Assert.assertEquals(USERNAME, muPage.getUsernameTable());

        muPage.clickDelete();
        DeletePage deletePage = PageFactory.initElements(driver,DeletePage.class);

        /*5.	После клика по ссылке с атрибутом href равным «user/someuser/delete» появляется
        текст «Are you sure about deleting the user from Jenkins?».*/
        Assert.assertTrue(deletePage.getDeleteConfirmText("Are you sure about deleting the user from Jenkins?"));

        buttonColorList.add(deletePage.getDeleteButtonColor());
        deletePage.clickConfirmDelete();

        /*6.	После клика по кнопке с надписью «Yes» на странице отсутствует строка таблицы
        (элемент tr), с ячейкой (элемент td) с текстом «someuser». На странице отсутствует
        ссылка с атрибутом href равным «user/someuser/delete».*/
        Assert.assertFalse(muPage.isUserPresent());
        Assert.assertFalse(muPage.isDeletePresent());

        /*7.	{На той же странице, без выполнения каких бы то ни было действий}.
        На странице отсутствует ссылка с атрибутом href равным «user/admin/delete».*/
        Assert.assertFalse(muPage.isAdminDeletePresent());

        /*1*.	У всех кнопок (элемент типа button), которые нужно кликать
        в основной части задания, цвет фона = #4b758b.*/
        for (int i = 0; i < buttonColorList.size(); i++) {
            String colorHex = toHex(buttonColorList.get(i));
            Assert.assertEquals("#4b758b", colorHex);
        }
    }

    /*method converts color from rgba format to Hex format*/
    public String toHex(String color) {
        color = color.replace("rgba(","");
        color = color.replace(")", "");;
        String colors[] = color.split(", ");
        String red = pad(Integer.toHexString(Integer.parseInt(colors[0])));
        String green = pad(Integer.toHexString(Integer.parseInt(colors[1])));
        String blue = pad(Integer.toHexString(Integer.parseInt(colors[2])));
        String hex = "#" + red + green + blue;
        return hex;
    }

    private static final String pad(String s) {
        return (s.length() == 1) ? "0" + s : s;
    }


    /*2*.	При попытке создать пользователя с пустым (незаполненным) именем на странице появляется
    текст «"" is prohibited as a full name for security reasons.»*/
    @Test
    public void createUserEmptyNameTest(){
        WelcomePage welcomePage = PageFactory.initElements(driver,WelcomePage.class);
        welcomePage.clickManage();
        ManageJenkinsPage mjPage = PageFactory.initElements(driver,ManageJenkinsPage.class);
        JavascriptExecutor jse = (JavascriptExecutor)driver;
        jse.executeScript("window.scrollBy(0,900)", "");
        mjPage.clickManageUsers();
        ManageUsersPage muPage = PageFactory.initElements(driver,ManageUsersPage.class);
        muPage.clickCreateUser();
        CreateUserPage cuPage = PageFactory.initElements(driver,CreateUserPage.class);
        cuPage.fillinUsername("")
                .fillinPassword(PASSWORD)
                .confirmPassword(PASSWORD)
                .fillinFullname("")
                .clickCreate();
        Assert.assertEquals("\"\" is prohibited as a full name for security reasons.", cuPage.getError());
    }

    /*3*.	При клике по ссылке с текстом «ENABLE AUTO REFRESH» эта ссылка пропадает,
    а вместо неё появляется ссылка с текстом «DISABLE AUTO REFRESH». При клике по ссылке
    с текстом «DISABLE AUTO REFRESH» эта ссылка пропадает, а вместо неё появляется ссылка
    с текстом «ENABLE AUTO REFRESH». Т.е. эти две ссылки циклически сменяют друг друга.*/
    @Test
    public void refreshTest(){
        WelcomePage welcomePage = PageFactory.initElements(driver,WelcomePage.class);
        Assert.assertEquals("ENABLE AUTO REFRESH", welcomePage.getRefreshText());
        welcomePage.clickRefresh();
        Assert.assertEquals("DISABLE AUTO REFRESH",welcomePage.getRefreshText());
        welcomePage.clickRefresh();
        Assert.assertEquals("ENABLE AUTO REFRESH", welcomePage.getRefreshText());
    }

    @After
    public void shutDown() throws IOException {
        WebDriverSingleton.getInstance().close();
        WebDriverSingleton.destroyInstance();
    }
}
