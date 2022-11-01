# 钛极智能网关AT指令集

钛极智能网关是钛云物联开发的支持NBIOT/4G/以太网等多种网络的可编程边缘计算网关,支持RS485, RS232等多种设备通讯方式， 满足用户对设备的接入需求, 能够支持目前主流的网络接入协议和公有云平台，包括MQTT, COAP, LWM2M以及阿里云、腾讯云、中国移动ONENET、中国电信AEP等等。

钛极智能网关内置钛云物联自主知识产权的钛极OS(TiJOS)物联网操作系统，支持用户通过Java语言进行功能扩展，适用各种工况， 它强大的可编程功能允许用户根据项目需求通过Java语言开发相关所需的功能， 如串口通讯， 协议解析，上传云端等等。

钛极智能网关内置AT指令集，通过其提供编程端口即可下发指令对设备进行操作并测试，通讯配置为115200，8，1,N, 用户通过串口工具或程序即可进行操作， 注意只有在可编程状态下才接收指令。



## AT指令格式

### 下发命令格式

at+op[=value,arg]    

所有下发指令以at开始， op为操作码，如果有控制值和参数则在op后加上=分隔值和参数，值和参数通过逗号分隔，如 at+getprop=os.verion

指令与回车换行结束

### 命令回复格式

命令回复以回车换行结束

**成功响应**

ok[,value]                                 

value为返回值

**失败响应**

fail,class ?    

失败后，可通过at+geterr获到详细信息

​                             

## 支持的指令列表

|      OP      |       value       |                  arg                   |                                       |
| :----------: | :---------------: | :------------------------------------: | :-----------------------------------: |
|   getprop    |   java.vm.name    |                   无                   |            获取虚拟机名字             |
|              |  java.vm.version  |                   无                   |            获取虚拟机版本             |
|              |      os.name      |                   无                   |             获取系统名字              |
|              |    os.version     |                   无                   |             获取系统版本              |
|              |      os.arch      |                   无                   |             获取硬件平台              |
|              |     host.name     |                   无                   |             获取主机名字              |
|              |      host.sn      |                   无                   |            获取主机序列号             |
|              | host.logger.level |                   无                   |           获取主机日志等级            |
|              |   host.calendar   |                   无                   |           获取主机日历时间            |
|              |     app.space     |                   无                   |         获取应用存储空间大小          |
|              |   app.freespace   |                   无                   |       获取应用剩余存储空间大小        |
|              |   app.capacity    |                   无                   |         获取应用最大存储数量          |
|              |     host.caps     |                   无                   |     获取主机能力**见“指令举例”**      |
|              |                   |                                        |                                       |
|   setprop    |     host.name     |                name                |             设置主机名字          |
|              | host.logger.level |               level                |           设置主机日志等级        |
|              |   host.calendar   | year,month,date,hour,minute,second |          设置主机日历时间          |
|              |    app.autorun    |                 id                 |    设置指定应用程序为自动运行程序     |
|              |                   |                                        |                                       |
|    lsapp     |        无         |                   无                   |           获取应用程序列表            |
|              |                   |                                        |                                       |
|    runapp    |      id       |      param0, param1, param2...      |      执行指定应用程序(可带参数)       |
|              |                   |                                        |                                       |
|    rmapp     |      format       |                   无                   |           格式化应用程序区            |
|              |        all        |                   无                   |    删除除当前应用外的所用应用程序     |
|              |      **id**       |                   无                   |           删除指定应用程序            |
|              |                   |                                        |                                       |
|    dlapp     |       start       |              size,id              |     启动应用程序下载并指定ID      |
|              |                   |                size                |     启动应用程序下载不指定ID      |
|              |                   |                                        |       下载过程见“指令举例”        |
|              |                   |                                        |                                       |
|              |      finish   |              generic               |       结束并激活为普通应用程序    |
|              |                   |               shell                |     结束并激活为壳(终端)应用程序      |
|              |                   |                                        |                                       |
|    geterr    |        无         |                   无                   |           获取失败详细信息            |
|              |        msg        |                   无                   |             获取失败描述              |
|              |       trace       |                   无                   |           获取失败详细信息            |
|              |                   |                                        | 失败信息获取后将清除 |
|   setvalue   |     group     |               key,value               | 将KEY/VALUE键值对写入到group中 |
|   getvalue   |     group      |                  key                   |       获取group中key的值       |
| getkeyvalues |     group      |                                        |   获取group中所有KEY/VALUE键值对   |
|  removegroup  |    group      |                                        |             删除指定group              |



## 指令举例

**1.**获取虚拟机名字

at+getprop=java.vm.name[回车符]

ok,TiJVM



**2.**运行ID=3的应用程序(不带参数)

at+runapp=3[回车符]



**3.**运行ID=3的应用程序(带参数)

at+runapp=3,param0, param1[回车符]



**4.**设置主机日历时间

at+setprop=host.calendar,2018, 4, 17, 8, 20, 0     ;设置时间为：2018年4月17日8点20分0秒



**5.**获取主机能力

at+getprop=host.caps[回车符]

ok,base     ;基础设施



**6.**下载应用程序(不指定ID)

at+dlapp=start,20480[回车符]    		 ;启动下载应用程序，并设定程序大小，单位：字节(B)

ok,8192					           ;返回启动成功，并给出后续单次**二进制**透传数据长度，即：单包最大数据长度

0102004032412d3f0a0... (共8191B)    ;发送第1包数据

ok,continue  ; //第1包数据处理完毕，可以继续发送后续数据

0502304032412a3f0e0... (共8191B)    ;发送第2包数据

ok,continue  ; //第2包数据处理完毕，可以继续发送后续数据

7192304232416a3a0c0... (共4096B)    ;发送第3包数据

ok,finish; //第3包数据处理完毕，传输完毕

at+dlapp=finish,generic[回车符]    	   ;下载应用程序结束，并设定应用类型进行激活，本列设定为：generic

ok,1                                                           ;程序下载激活成功，应用ID=1



**7.** KV数据库键值对操作

键值对内部一般用于属性保存

at+setvalue=modbus,COM,9600/8/1/N

ok

at+getvalue=modbus,COM

ok,9600/8/1/N

at+setvalue=modbus,address,1

ok

at+getvalue=modbus,address

ok,1

at+getkeyvalues=modbus

ok,address=1,baudrate=9600/8/1/N,COM=9600/8/1/N