# scala-ts

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.wajda/scala-ts_2.12/badge.svg)](https://search.maven.org/search?q=g:com.github.wajda)


A fork of [miloszpp/scala-ts](https://github.com/miloszpp/scala-ts) with some improvements and bug fixes.

It's available as a **standalone jar** or as a **Maven plugin**.

It supports Scala 2.11, 2.12, 2.13.

##### Usage
###### ... as a command line tool
```shell
# java -jar scala-ts_{SCALA_BINARY_VERSION}-{SCALA_TS_VERSION}-dist.jar  
# For example or Scala 2.13:

java -jar scala-ts_2.13-0.4.1.9-dist.jar
```

###### ... as a Maven plugin
```xml

<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
    <version>3.3.0</version>
    <executions>
        <execution>
            <phase>process-classes</phase>
            <goals>
                <goal>java</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <includeProjectDependencies>true</includeProjectDependencies>
        <includePluginDependencies>true</includePluginDependencies>
        <executableDependency>
            <groupId>com.github.wajda</groupId>
            <artifactId>scala-ts_${scala.binary.version}</artifactId>
        </executableDependency>
        <mainClass>com.mpc.scalats.CLI</mainClass>
        <arguments>
            <!-- destination of the generated TypeScript files -->
            <argument>--out</argument>
            <argument>${project.basedir}/target/generated-ts/my_model.ts</argument>
            <!-- other options -->
            <argument>--trait-to-type</argument>
            <argument>--option-to-nullable</argument>
            <!-- list of Scala entities to generate TS definitions for -->
            <argument>my.model.MyTrait</argument>
            <argument>my.model.MyCaseClass</argument>
            <argument>my.model.MyOtherTopLevelCaseClassesOrTraits</argument>
        </arguments>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>com.github.wajda</groupId>
            <artifactId>scala-ts_${scala.binary.version}</artifactId>
            <version>0.4.1.9</version>
        </dependency>
    </dependencies>
</plugin>
```
