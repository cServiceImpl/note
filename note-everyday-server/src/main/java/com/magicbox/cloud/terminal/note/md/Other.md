### idea 
1、commit中，设置开启

![image-20250113140314570](D:\Download\TyporaPic\image-20250113140314570.png)

自动整理代码格式，清楚无效引用

2、idea的application启动参数  vm -options   控制台打印日志

```vm options
-Dasync.log.output=console
```
3、starter-parent换了settings拉不下来时，直接install到本地

### git常用命令

```bash
git reflog show --date=local | grep 分支名   //查看分支引用记录，了解分支从哪个分支创建的
git branch -b 新分支名						//创建新分支
git merge name						    	//将名称为[name]的分支与当前分支合并
git log 									//历史记录
git reset --soft 版本							//用于回退到某个版本
git reset --hard 版本	       //撤销工作区中修改，将暂存区与工作区都回退版本，并删除之前的所有信息提交
```

### linux

```bash
nohup java -jar -Dasync.log.output=FILE -Dspring.profiles.active=dev mb-terminal-server.jar  > /dev/null 2>&1 &
```

1. **nohup**：这是一个Unix/Linux命令，用于运行一个命令在后台，并忽略挂起（HUP）信号。这样，即使你关闭了终端或SSH会话，该命令仍将继续运行。

2. **java -jar**：这是启动Java应用程序的标准命令。`-jar`选项告诉Java虚拟机要运行的是一个JAR文件（Java Archive）。

3. **-Dasync.log.output=FILE**：这是一个Java系统属性，用于配置异步日志的输出。这可能是告诉程序将日志输出到某个特定的文件。

4. **-Dspring.profiles.active=dev**：这是另一个Java系统属性，它设置了Spring框架的活跃配置文件为“dev”。Spring框架使用不同的配置文件来管理不同的环境设置，例如开发（dev）、测试（test）和生产（prod）。

5. **mb-terminal-server.jar**：这是要运行的Java程序的JAR文件名。

6. **> /dev/null 2>&1 &**：这部分是重定向命令，用于将标准输出和标准错误输出重定向到`/dev/null`。这样，程序的所有输出（无论是正常的输出还是错误信息）都会被丢弃，不会显示在终端上。`&`将整个命令放到后台运行。

7. telnet命令和ping命令

   **telnet** 是一个网络协议，也是一个命令行工具，用于测试TCP连接。通过telnet，用户可以远程登录到另一台计算机并执行命令   如   telnet   36.134.140.149 30221

   **ping** 是一个用于测试网络连接的工具。它发送ICMP（Internet Control Message Protocol）回显请求到目标主机，并等待ICMP回显响应   如  ping    36.134.140.149:30221

### docker

```dockerfile
FROM registry-vecps-ns.gaccloud.com.cn/tenant-hwub/openjdk:8-jre-alpine

ADD target/x8v-smart-application.jar x8v-smart-application.jar

EXPOSE 8080

CMD java -jar ${JAR_ARG} ${JAVA_OPTS} -Dspring.profiles.active=${ACTIVE_PROFILE} x8v-smart-application.jar
```

##### 镜像导入导出

```bash
docker save -o <导出的文件名.tar> <镜像名称:标签>

docker load -i <导入的文件名.tar>
```
