import java.util.Scanner;

public class
BeeInputTest
{
	public static void
	main(String[] args)
	{
		Scanner scanner = new Scanner(System.in);

		System.out.print("당신의 이름을 입력하세요: ");
		String name = scanner.next();
		System.out.println("당신의 이른은 " + name + " 입니다.");

		System.out.print(name + "님의 나이을 입력하세요: ");
		int age = scanner.nextInt();
		System.out.println(name + "님의 나이는 " + age + "세 입니다.");

		System.out.print(name + "님의 카와 몸무계을 입력하세요: ");
		double height = scanner.nextDouble();
		double weight = scanner.nextDouble();
		System.out.println(name + "님의 키는 " + height + ", 몸무계는 " + weight + " 입니다.");

		scanner.close();
	}
}
