package randori.plugin.util;

/**
 * @author Roland Zwaga <roland@stackandheap.com>
 */
public final class LogUtils
{

    public static String dumpStackTrace(StackTraceElement[] elements)
    {
        String dump = "";
        for (StackTraceElement element : elements)
        {
            dump += element.toString() + "\n";
        }
        return dump;
    }
}
