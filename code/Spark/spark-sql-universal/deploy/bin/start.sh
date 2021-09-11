# 必须输入应用名称，程序名称是查找配置的关键信息
#appName=$1
# 可以接收输入的日期参数[例如：20210727]
#date=$2
# 可以输入的厂商参数[yd、lt、dx]
#provider=$3
queue_name=default
# driver内存设置
driver_memory=1g
# 执行器数量设置
num_executors=20
# 每个执行器的内存设置
executor_memory=2g
# 每个执行器核数设置
executor_cores=2
# 计算并行度设置
parallelism=50

shell_path="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
echo $shell_path
log_path=$shell_path/../log

# 如果脚本输入了日期和厂商参数，他们一并也会提交到程序中去
if [ $# != 3 ] ; then
	time=$(date "+%Y%m%d")
	current_day=`echo $time |cut -b 3-8`
	nohup sh $shell_path/run.sh $queue_name $driver_memory $num_executors $executor_memory $executor_cores $parallelism $1 > $log_path/$1/$current_day.log 2>&1 &
	tailf $log_path/$1/$current_day.log
else
	nohup sh $shell_path/run.sh $queue_name $driver_memory $num_executors $executor_memory $executor_cores $parallelism $1 $2 $3 > $log_path/$1/$2.log 2>&1 &
	tailf $log_path/$1/$2.log
fi
# 程序会自动tailf查看日志，可以随时使用ctrl + c结束