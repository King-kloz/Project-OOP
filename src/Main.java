import Utilities.UserSession;
import Views.LoginFrame;

public class Main {
    public static void main(String[] args) {
        UserSession userSession = UserSession.getInstance();

        if (userSession.isLoggedIn()) {
            navigateToDashboard(userSession.getUserType());
        } else {
            showLoginPage();
        }
    }

    private static void navigateToDashboard(String userType) {
        switch (userType) {
            case "STUDENT":
                new Views.StudentDashboard().setVisible(true);
                break;
            case "INSTRUCTOR":
                new Views.InstructorDashboard().setVisible(true);
                break;
            case "ADMINISTRATOR":
                new Views.AdminDashboard().setVisible(true);
                break;
            default:
                showLoginPage();
        }
    }

    private static void showLoginPage() {
        new LoginFrame().setVisible(true);
    }
}