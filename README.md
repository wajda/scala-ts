# scala-ts

Mavenized version of [miloszpp/scala-ts](https://github.com/miloszpp/scala-ts)

TODO: better documentation is coming.


tl;dr
=========
##### Requirements
- Scala 2.11

##### Getting started using Maven
```xml
<dependency>
  <groupId>com.github.wajda</groupId>
  <artifactId>scala-ts_2.11</artifactId>
  <version>0.4.1.1</version>
</dependency>
```

##### Usage
###### ... in a command Line
```shell
    java -jar scala-ts-0.4.1.1-dist.jar
```

###### ... as a Maven plugin
```xml
    <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>exec-maven-plugin</artifactId>
        <version>1.5.0</version>
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
                <artifactId>scala-ts_2.11</artifactId>
            </executableDependency>
            <mainClass>com.mpc.scalats.CLI</mainClass>
            <arguments>
                <argument>--out</argument>
                <argument>${project.basedir}/target/generated-ts/my_model.ts</argument>
                <argument>--emit-interfaces</argument>
                <argument>--option-to-nullable</argument>
                <argument>my.model.MyCaseClass</argument>
                <argument>my.model.MyTrait</argument>
                <argument>my.model.MyOtherTopLevelCaseClassesOrTraits</argument>
            </arguments>
        </configuration>
        <dependencies>
            <dependency>
                <groupId>com.github.wajda</groupId>
                <artifactId>scala-ts_2.11</artifactId>
                <version>0.4.1.1</version>
            </dependency>
        </dependencies>
    </plugin>
```
