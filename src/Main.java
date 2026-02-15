import java.util.Scanner;

public class Main {
    static void main(String[] ignoredArgs) {
        Scanner scan = new Scanner(System.in);
        String playAgain = "";
        do {
            int theNumbers = (int) (Math.random() * 100 + 1);
            System.out.println(theNumbers);
            int guess = 0;
            int numbersOfTries = 0;
//            int finalTires = 0;
            while (guess != theNumbers) {
                System.out.println("Угадайте число от 1 до 100");
                guess = scan.nextInt();
                //System.out.println("Вы написали число:" + guess + ".");
                if (guess < theNumbers)
                    System.out.println("Не угадали! " + guess + " меньше загаданного мной числа.");
                else if (guess > theNumbers)
                    System.out.println("Не угадали! " + guess + " больше загаданного мной числа.");
                else
                    System.out.println("Угадали! Именно число " + guess + " я и загадывал. Поздравляю! Вы победили.");
                numbersOfTries = numbersOfTries + 1;

            }
                if (numbersOfTries > 8)
                    System.out.println ("Для победы вам потребовалось " + numbersOfTries + " попыток. Это очень плохой результат!");
                else if (numbersOfTries > 6)
                    System.out.println ("Для победы вам потребовалось " + numbersOfTries + " попыток. Это неплохо!");
                else if (numbersOfTries > 4)
                    System.out.println ("Для победы вам потребовалось " + numbersOfTries + " попыток. Это идеальный результат!");
                else if (numbersOfTries > 1)
                    System.out.println ("Для победы вам потребовалось " + numbersOfTries + " попытки. Это идеальный результат!");
                else
                    System.out.println ("Для победы вам потребовалось " + numbersOfTries + " попытка. Это идеальный результат!");
                System.out.println();
            System.out.println("Хотите сыграть еще раз? Для ответа напишите 'y' (Да) или 'n' (Нет).");
            playAgain = scan.next();
        } while (!playAgain.equalsIgnoreCase("n") && playAgain.equalsIgnoreCase("y"));
                 System.out.println("Хорошо, поиграем позже!");
    }
}
