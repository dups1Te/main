import java.util.Scanner;

public class Main {
    static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        String playAgain = "";
        do {
            int theNumbers = (int) (Math.random() * 100 + 1);
            //System.out.println(theNumbers);
            int guess = 0;
            while (guess != theNumbers) {
                System.out.println("Угадайте число от 1 до 100");
                guess = scan.nextInt();
                //System.out.println("Вы написали число:" + guess + ".");
                if (guess < theNumbers) System.out.println("Не угадали! " + guess + " меньше загадонного мной числа.");
                else if (guess > theNumbers) System.out.println("Не угадали! " + guess + " больше загадонного мной числа.");
                else System.out.println("Угадали! Именно число " + guess + " я и загадывал. Поздравляю! Вы победили.");
            }
                System.out.println("Хотите сыграть еще раз? Для ответа напишите 'y' (Да) или 'n' (Нет).");
                playAgain = scan.next();
        } while (!playAgain.equalsIgnoreCase("n") && playAgain.equalsIgnoreCase("y"));
                 System.out.println("Хорошо, поиграем позже!");
    }
}
