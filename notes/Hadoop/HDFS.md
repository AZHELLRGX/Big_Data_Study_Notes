# HDFS读写流程

## 概述
开始之前先看看其基本属性，HDFS（Hadoop Distributed File System）是GFS的开源实现。

### 优点如下：

- 能够运行在廉价机器上，硬件出错常态，需要具备高容错性
- 流式数据访问，而不是随机读写
- 面向大规模数据集，能够进行批处理、能够横向扩展
- 简单一致性模型，假定文件是一次写入、多次读取

### 缺点：

- 不支持低延迟数据访问
- 不适合大量小文件存储（因为每条元数据占用空间是一定的）
- 不支持并发写入，一个文件只能有一个写入者
- 不支持文件随机修改，仅支持追加写入

## HDFS中的block、packet、chunk

### block

文件上传前需要分块，这个块就是block，一般为128MB，当然你可以去改，不顾不推荐。因为**块太小：寻址时间占比过高。块太大：Map任务数太少，作业执行速度变慢。**它是最大的一个单位。

### packet

packet是第二大的单位，它是client端向DataNode，或DataNode的PipLine之间传数据的基本单位，默认64KB。

### chunk

它是client向DataNode，或DataNode的PipLine之间进行数据校验的基本单位，默认512Byte，因为用作校验，故每个chunk需要带有4Byte的校验位。所以实际每个chunk写入packet的大小为516Byte。由此可见真实数据与校验值数据的比值约为128 : 1。

### chunk与packet之间的关系

在client端向DataNode传数据的时候，HDFSOutputStream会有一个chunk buff，写满一个chunk后，会计算校验和并写入当前的chunk。之后再把带有校验和的chunk写入packet，当一个packet写满后，packet会进入dataQueue队列，其他的DataNode就是从这个dataQueue获取client端上传的数据并存储的。同时一个DataNode成功存储一个packet后之后会返回一个ack packet，放入ack Queue中。

## 数据读取流程

![img](HDFS.assets/172166c4c05e38e5tplv-t2oaga2asx-watermark.awebp)

- 客户端调用`DistrbutedFileSystem`的Open方法，发送请求到namenode。
- Namenode返回所有block的位置信息以及文件副本的保存位置（datanode节点的地址），并将这些信息返回给客户端。
- 客户端通过namenode返回的信息调用`FSDataInputStream`方法读取最适合的副本节点（本地→同机架→数据中心）。
- 下载完成block后，客户端会通过dn存储的校验和来确保文件的完整性。

## 数据写出流程

![img](HDFS.assets/1721661711daba6ctplv-t2oaga2asx-watermark.awebp)

举例说明：**假设写入一个150MB的文件，block默认为128MB，那么需要分两个块，默认三个副本（生产上3个是起码的）。**

1. 客户端（ClientNode）向HDFS写入数据首先调用`DistributedFileSystem`的create方法获取输出流（FSDataOutputStream）
2. DistributedFileSystem调用NN（namenode）的create方法发出创建文件请求。NN对准备上传的文件名称和路径做校验，确定是否拥有写权限、是否有重名文件，并将操作写入到edits文件中。
3. 客户端（CN)调用FSDataOutputStream向输出流输出数据，开始写入第一个块。
4. FSDataOutputStream调用NN的addBlock方法请求第一个块要存储在哪些DN上（配置副本数为3）。 如果管道（pipeline）没有建立，则根据位置信息建立pipeline。
5. 与第一个DN节点DN1建立连接（RPC协议），向其发送package。该package会保存到ackqueue确认队列中。写数据时先将数据写到一个校验块chunk中，写满512字节，对chunk计算校验和checksum值（4字节）。以带校验和的checksum为单位向本地缓存输出数据（本地缓存占9个chunk），本地缓存满了向package输入数据，一个package占64kb。当package写满后，将package写入dataqueue数据队列中。将package从dataqueue数据对列中取出，沿pipeline发送到DN1，DN1保存，然后将package发送到DN2，DN2保存，以此类推直至最后一个副本节点。
6. 最后一个副本校验完成后，逆方向通过pipeline回传给客户端（DN3→DN2→DN1→CN）。客户端根据校验结果判断，“成功”则将ackqueue确认队列中的package删除；如果“失败”，则将ackqueue队列中的package取出，从新放入到dataqueue数据队列末尾，等待回滚（从新沿pipeline发送）。
7. 当第一个块的所有package发送完毕，3个副本都存有block1的完整数据，则3个DN同时调用NN的blockReceivedAndDeleted方法。NameNode会更新记录内存中DataNode和block的关系。ClientNode关闭同DataNode建立的pipeline。开始执行未发送的第2个块，第3个块..........直到文件所有数据传输完成。
8. 全部数据执行完成后调用FSDataOutputStream的 close 方法关闭流。
9. 客户端（CN）调用NN的complete方法通知NN全部数据传输完成。