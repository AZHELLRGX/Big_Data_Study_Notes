package org.azhell.learn.flink.tool;

public class Utils {
    private Utils() {
    }

    public static void print(Object o) {
        System.out.println(o);
    }

    /**
     * 指定分隔符，拼接字符串
     *
     * @param delimiter 分隔符
     * @param strings   字符串集合
     * @return 拼接完成的字符串
     */
    public static String join(String delimiter, String... strings) {
        StringBuilder res = new StringBuilder();
        final int last = strings.length - 1;
        for (int i = 0; i < last; i++) {
            res.append(strings[i]).append(delimiter);
        }
        return res.append(strings[last]).toString();
    }

    public static void print(String logFormat, String... args) {
        for (String arg : args) {
            logFormat = logFormat.replaceFirst("\\{}", arg);
        }
        System.out.println(logFormat);
    }
}
