1、基础包
    1）更新jdk版本到OpenJdk17：https://adoptium.net/zh-CN/temurin/releases/?package=any&arch=x64&version=17
    2）arthas：https://github.com/alibaba/arthas/releases/download/arthas-all-3.7.2/arthas-bin.zip

2、pom
<java.version>17</java.version>

3、配置文件变更
1)common配置文件
# log
logging.file.name=${FPC_LOG}/${FPC_APP}/${FPC_APP}-log.txt
logging.file.path=${FPC_LOG}/${FPC_APP}
logging.logback.rollingpolicy.max-file-size=10MB
logging.logback.rollingpolicy.max-history=10

# cycle
spring.main.allow-circular-references=true

spring.mvc.hiddenmethod.filter.enabled=true

# jasypt
jasypt.encryptor.iv-generator-classname=org.jasypt.iv.NoIvGenerator
jasypt.encryptor.algorithm=PBEWithMD5AndDES

2)app
spring.resources.static-locations > spring.web.resources.static-locations

4、更新启动服务脚本
TFA:
ExecStart=/opt/java/bin/java -Xmx2g -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${FPC_LOG}/${FPC_APP} -XX:ErrorFile=${FPC_LOG}/${FPC_APP}/hs_err.log -Xlog:gc*:${FPC_LOG}/${FPC_APP}/gc.log -jar -Dloader.main=com.machloop.fpc.manager.boot.ManagerApplication -Dspring.config.location=${FPC_CONFIG}/fpc-apps/fpc-common.properties,${FPC_CONFIG}/fpc-apps/fpc-manager.properties -Djna.library.path=/opt/components/fpc-libs /opt/components/fpc-apps/fpc.jar

CMS：
ExecStart=/opt/java/bin/java -Xmx2g -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${FPC_LOG}/${FPC_APP} -XX:ErrorFile=${FPC_LOG}/${FPC_APP}/hs_err.log -Xlog:gc*:${FPC_LOG}/${FPC_APP}/gc.log -jar -Dloader.main=com.machloop.fpc.cms.center.boot.CenterApplication -Dspring.config.location=${FPC_CONFIG}/fpc-apps/fpc-cms-common.properties,${FPC_CONFIG}/fpc-apps/fpc-cms-center.properties -Djna.library.path=/opt/components/fpc-libs /opt/components/fpc-apps/fpc-cms-center.jar


5、CMS兼容grpc
<grpc.version>1.60.0</grpc.version>
<dependency>
    <groupId>org.apache.tomcat</groupId>
    <artifactId>annotations-api</artifactId>
    <version>6.0.53</version>
    <scope>compile</scope>
</dependency>

6、全局替换
org.apache.commons.lang > org.apache.commons.lang3
javax > jakarta

javax.servlet.http.HttpServletRequest > jakarta.servlet.http.HttpServletRequest
javax.servlet.http.HttpServletResponse > jakarta.servlet.http.HttpServletResponse
javax.validation.constraints.NotEmpty > jakarta.validation.constraints.NotEmpty
javax.annotation.PostConstruct > jakarta.annotation.PostConstruct
javax.annotation.PreDestroy > jakarta.annotation.PreDestroy
javax.validation.constraints.Digits > jakarta.validation.constraints.Digits
javax.servlet.ServletContext > jakarta.servlet.ServletContext
javax.servlet.ServletOutputStream > jakarta.servlet.ServletOutputStream
javax.validation.constraints.Pattern > jakarta.validation.constraints.Pattern

6、offsetDateTime使用fastjson序列号
mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
mapper.registerModules(new JavaTimeModule(), new SimpleModule());
