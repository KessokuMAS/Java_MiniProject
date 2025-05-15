package Game;

public class Main {
    public static void main(String[] args) {
        try {
            new Game();
        } catch (Exception e) {
            System.err.println("Error starting game : " + e.getMessage());
            e.printStackTrace();
        }
    }
}