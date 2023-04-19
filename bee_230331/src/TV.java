
public class TV
{
	private String company_;
	private int year_;
	private int inch_;

	public TV(String company, int year, int inch)
	{
		company_ = company;
		year_ = year;
		inch_ = inch;
	}

	public void show()
	{
		System.out.println(
				String.format("%s에서 만든 %d년형 %d인치 TV",
				company_, year_, inch_));
	}
}
