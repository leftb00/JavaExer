
public class BeeHello
{
	public static void main(String[] args)
	{
		//final double PI = 3.141592;
		int i = 20;
		int j;
		char a;

		j = sum(i, 10);
		a = '?';

		System.out.println(a);
		System.out.println("Hello");
		System.out.println(j);

		double d = 1.77;
		j = (int)d;
		System.out.println(j);

		String str = "123";
		j = Integer.parseInt(str);
		System.out.println(j);
	}

	public static int
	sum(int n, int m)
	{
		return n + m;
	}
}
