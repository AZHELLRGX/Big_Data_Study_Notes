USE `rgx`;


CREATE EXTERNAL TABLE IF NOT EXISTS `data`
(
`id` STRING ,
`login_date` STRING
)
ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t'
LOCATION '/usr/rgx/learn/1';

-- 方案一：使用开窗函数，对用户登录时间分组排序，然后用登陆时间减去排序序号，如果一致那一定是连续登录
SELECT id,count(1) as login_times,min(login_date) as start_time,max(login_date) as end_date FROM 
(SELECT id,login_date,date_sub(login_date, dn) as diff_date FROM
(SELECT id,login_date,row_number() over(PARTITION BY id ORDER BY login_date) AS dn FROM data) a
) b
GROUP BY id,diff_date
HAVING login_times >= 3;


-- 方案二：利用lag和lead函数进行处理
SELECT 
id,
lag_login_date,
login_date,
lead_login_date
FROM
(SELECT 
id,
login_date,
lag(login_date,1,login_date) over(partition by id order by login_date) as lag_login_date,
lead(login_date,1,login_date) over(partition by id order by login_date) as lead_login_date
FROM data
) t1
where datediff(login_date,lag_login_date) =1 and datediff(lead_login_date,login_date) =1;

-- 行转列使用contact_ws函数
select id, 
       concat_ws(',',collect_list(login_date)) cw
from data
group by id;

-- 列转行使用explod函数
select id,login_date, time
from data 
lateral view explode(split(login_date,'-'))  b AS time;
-- lateral view默认是将虚拟表与原表进行了inner join操作，如果需要全连接，使用outer lateral view即可