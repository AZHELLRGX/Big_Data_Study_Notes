# 获取当前脚本的绝对路径
shell_path="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
echo $shell_path
jar_path="${shell_path}/../lib/core/spark-sql-universal-1.2.0.jar"
echo $jar_path

# 接收输入参数
queue_name=$1
driver_memory=$2
num_executors=$3
executor_memory=$4
executor_cores=$5
parallelism=$6


if [ $# == 7 ] ; then
	spark-submit --queue $queue_name --master yarn --driver-memory $driver_memory --num-executors $num_executors --executor-memory $executor_memory --executor-cores $executor_cores --conf spark.default.parallelism=$parallelism --jars $shell_path/../lib/third/sqljdbc4-4.0.jar,$shell_path/../lib/third/mysql-connector-java-5.1.47.jar --class cn.mastercom.bigdata.main.MainJob $jar_path $shell_path $7
else
	spark-submit --queue $queue_name --master yarn --driver-memory $driver_memory --num-executors $num_executors --executor-memory $executor_memory --executor-cores $executor_cores --conf spark.default.parallelism=$parallelism --jars $shell_path/../lib/third/sqljdbc4-4.0.jar,$shell_path/../lib/third/mysql-connector-java-5.1.47.jar --class cn.mastercom.bigdata.main.MainJob $jar_path $shell_path $7 $8 $9
fi


# spark-submit --queue default --master yarn --driver-memory 1g --num-executors 20 --executor-memory 2g --executor-cores 2 --conf spark.default.parallelism=50 --jars $shell_path/../lib/third/sqljdbc4-4.0.jar,$shell_path/../lib/third/mysql-connector-java-5.1.47.jar --class cn.mastercom.bigdata.main.MainJob $jar_path $shell_path