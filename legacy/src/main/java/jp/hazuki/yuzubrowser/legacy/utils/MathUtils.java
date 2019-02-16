package jp.hazuki.yuzubrowser.legacy.utils;

public class MathUtils {
    private MathUtils() {
        throw new UnsupportedOperationException();
    }

    public static boolean equalsSign(int a, int b) {
        return Integer.signum(a) == Integer.signum(b);
    }

    public static boolean equalsSign(long a, long b) {
        return Long.signum(a) == Long.signum(b);
    }

    public static boolean equalsSign(float a, float b) {
        return Math.signum(a) == Math.signum(b);
    }

    public static boolean equalsSign(double a, double b) {
        return Math.signum(a) == Math.signum(b);
    }
}
