package cn.mastercom.bigdata.util.time;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 操作时间的工具类
 */
public class DateUtil {
    private DateUtil() {
        // do nothing
    }

    private static final Logger log = LoggerFactory.getLogger(DateUtil.class);

    public static String getBeforeDay(String dateStr, int amount) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd");
            Date date = simpleDateFormat.parse(dateStr);
            Calendar calendar = Calendar.getInstance();   //得到日历
            calendar.setTime(date);//把当前时间赋给日历
            calendar.add(Calendar.DAY_OF_MONTH, amount);
            return simpleDateFormat.format(calendar.getTime());
        } catch (ParseException e) {
            log.error("提供的日期字符串[{}]解析错误，可能不符合yyMMdd格式", dateStr, e);
        }
        return null;
    }
}
