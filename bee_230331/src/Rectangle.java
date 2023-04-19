
public class Rectangle
{
	private int x_, y_, width_, height_;

	public Rectangle(int x, int y, int width, int height)
	{
		x_ = x;
		y_ = y;
		width_ = width;
		height_ = height;
	}

	public int square()
	{
		return width_ * height_;
	}

	public boolean contains(Rectangle r)
	{
		return x_<= r.x_ && y_ <= r.y_
				&& (x_+width_) >= (r.x_+r.width_)
				&& (y_+height_) >= (r.y_+r.height_);
	}

	public void show()
	{
		System.out.println(
				String.format("(%d,%d)에서 크기가 %dx%d인 사각형",
				x_, y_, width_, height_));
	}
}
