# VMware Tanzu GemFire @Cacheable REST Demo

VMware Tanzu GemFire is a distributed, in-memory, key-value store that performs read and write operations at blazingly fast speeds. It offers highly available parallel message queues, continuous availability, and an event-driven architecture you can scale dynamically, with no downtime. As your data size requirements increase to support high-performance, real-time apps, Tanzu GemFire can scale linearly with ease.

VMware Tanzu GemFire for Kubernetes brings the power of GemFire to Kubernetes.    Tanzu GemFire for Kubernetes enables users create, update, scale and manage GemFire with ease.

This project assumes that one has setup a Tanzu GemFire for Kubernetes operator.   If you need to learn more about Tanzu GemFire for Kubernetes check out these following resources:
  * https://tanzu.vmware.com/gemfire
  * https://network.pivotal.io/products/tanzu-gemfire-for-kubernetes/
  * https://youtu.be/vbskafFrJ_I - How to install the GemFire Operator
  * https://youtu.be/JsuOV4gu6hg - How to derive the locator addresses in Kubernetes.
  * https://spring.io/
  * **@Cacheable** is part of the Spring Context Library - https://spring.io/projects/spring-framework


# How to Deploy to Kubernetes 

1 - Create a Cluster
```
$ cat tanzu-gemfire.yml
apiVersion: gemfire.tanzu.vmware.com/v1
kind: GemFireCluster
metadata:
  name: tanzu-gemfire
  namespace: gemfire-cluster
spec:
  image: registry.pivotal.io/tanzu-gemfire-for-kubernetes/gemfire-k8s:1.0.0
$ kubectl apply -f tanzu-gemfire.yml
```
2 - Using the GemFire command line tool ``gfsh`` create the region with the policy that we want for the data.

```
$ kubectl -n gemfire-cluster exec -it tanzu-gemfire-locator-0 -- gfsh
    _________________________     __
   / _____/ ______/ ______/ /____/ /
  / /  __/ /___  /_____  / _____  / 
 / /__/ / ____/  _____/ / /    / /  
/______/_/      /______/_/    /_/    

Monitor and Manage Pivotal GemFire

gfsh>connect 
gfsh>create region --name=bikecache --type=PARTITION  --entry-time-to-live-expiration=60 --entry-time-to-live-expiration-action=destroy --enable-statistics=true
```
3 - Download and deploy Spring Classes Used as Keys in GemFire `org.springframework.cache.interceptor.SimpleKey`
```
$ kubectl -n gemfire-cluster exec -it tanzu-gemfire-locator-0 -- bash
root@tanzu-gemfire-locator-0:/data# curl -s https://repo1.maven.org/maven2/org/springframework/spring-context/5.2.10.RELEASE/spring-context-5.2.10.RELEASE.jar -o spring-context-5.2.10.RELEASE.jar
root@tanzu-gemfire-locator-0:/data# gfsh
    _________________________     __
   / _____/ ______/ ______/ /____/ /
  / /  __/ /___  /_____  / _____  / 
 / /__/ / ____/  _____/ / /    / /  
/______/_/      /______/_/    /_/    1.13.1

Monitor and Manage Apache Geode
gfsh>connect
Connecting to Locator at [host=localhost, port=10334] ..
Connecting to Manager at [host=tanzu-gemfire-locator-0.tanzu-gemfire-locator.gemfire-cluster.svc.cluster.local, port=1099] ..
Successfully connected to: [host=tanzu-gemfire-locator-0.tanzu-gemfire-locator.gemfire-cluster.svc.cluster.local, port=1099]

You are connected to a cluster of version: 1.13.1

gfsh>deploy --jar=spring-context-5.2.10.RELEASE.jar

Deploying files: spring-context-5.2.10.RELEASE.jar
Total file size is: 1.17MB

Continue?  (Y/n): y
```
4 - Deploy the application to kubernetes
```
mvn clean package k8s:build k8s:push k8s:resource k8s:apply
```

# Youtube

For a video walk through and demo check out this youtube video: https://youtu.be/jrCwx9qzutM

# Local Development with GemFire

I have added a Spring `application.yml` which has a default `dev` profile.    The `dev` profile will be using a local GemFire instance.  Spring will pick up that YML file and use to configure GemFire client to properly connect a local GemFire.   

By using Spring profiles there are no changes needed when switching between local development and pushing an application to kubernetes.   

To help test I have included a [GemFire start script](scripts/start_gemfire.sh) that starts up GemFire.   

To run with another profile just tell spring the name of the profile:
```
java -Dspring.profiles.active=dev -jar bike-demo-0.0.1-SNAPSHOT.jar 
```
A common reason to run with another profile if there is a shared development cluster that all of the developers can access.   This is done when the data is larger then can fit on a developers machine, or has a complicated data loading process.

## Relevant files:
* [Appliacation YML file for development purposes](src/main/resources/application.yml)
* [Kubernetes yml which over rides "default" profile](src/main/jkube/deployment.yml)
