# 创建topic
kafka-topics.sh --zookeeper localhost:2181 --create --topic topic-demo --replication-factor 0 --partitions 4
kafka-topics.sh --zookeeper localhost:2181/kafka --create --topic topic-demo --replication-factor 0 --partitions 4


# 可以通过--describe展示主题的更多具体信息
kafka-topics.sh --zookeeper localhost:2181 --describe --topic topic-demo

# 提供了两个脚本 kafka-console-producer.sh 和 kafka-console-consumer.sh，通过控制台收发消息
kafka-console-consumer.sh --bootstrap-server 192.168.2.101:9092 --topic topic-demo

kafka-console-producer.sh --broker-list 192.168.2.101:9092 --topic topic-demo