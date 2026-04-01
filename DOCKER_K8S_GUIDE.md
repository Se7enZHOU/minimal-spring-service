# Docker 和 Kubernetes 入门手册

这份文档基于当前项目 `minimal-spring-service`，把我们前面讨论过的核心概念整理成一份完整笔记，方便后续集中查阅。

---

## 1. Docker Desktop Dashboard 有什么

Docker Desktop 可以理解成你本机容器世界的控制台。

### `Containers / Apps`

查看和管理正在运行的容器，可以：

- 启动容器
- 停止容器
- 删除容器
- 查看日志
- 进入容器终端

### `Images`

查看本地镜像。

例如当前项目构建出的镜像：

`minimal-spring-service:0.0.1-SNAPSHOT`

### `Volumes`

查看数据卷。数据卷用于持久化数据，避免容器删除后数据丢失。

### `Builds`

查看 `docker build` 的构建记录、每一步日志和缓存命中情况。

### `Docker Hub`

用于登录镜像仓库，以及管理推送和拉取镜像的账户状态。

### `Kubernetes`

管理 Docker Desktop 集成的本地 Kubernetes，可以创建 cluster、查看 node 状态。

### `Settings`

配置 CPU、内存、磁盘、代理、镜像源、`containerd image store`、Kubernetes 类型等。

### `Troubleshoot`

用于重启 Docker Desktop、重置 Kubernetes、清理数据、导出诊断信息。

---

## 2. 核心概念

### Image

镜像是一个可运行模板，里面通常包含：

- 运行环境
- 应用文件
- 启动命令

例如当前项目的镜像：

`minimal-spring-service:0.0.1-SNAPSHOT`

镜像本身是静态的，不会自己运行。

### Container

容器是镜像启动后的运行实例。

可以这样理解：

- `Image` 像模板
- `Container` 像模板创建出来并正在运行的实例

同一个镜像可以启动多个容器。

### Cluster

集群是 Kubernetes 管理的一组节点。

你本地 Docker Desktop 里的 Kubernetes 就是一个本地 cluster。

### Node

节点是集群里的机器。

在本地 Docker Desktop + kind 场景里，node 并不是真实物理机，而是 Docker Desktop 帮你管理的 Kubernetes 节点容器。

当前本地 node 名称示例：

`desktop-control-plane`

### Pod

Pod 是 Kubernetes 里最小的部署单位。

一个 Pod 里通常运行一个或多个容器。当前项目里通常是：

- 1 个 Pod
- 1 个 Spring Boot 容器

### Deployment

Deployment 是用来管理 Pod 的资源。

它负责：

- 创建 Pod
- 指定镜像
- 指定副本数
- Pod 挂了自动重建
- 更新镜像时执行滚动更新

当前项目的 Deployment 文件：

`k8s/deployment.yaml`

### Service

Service 是给 Pod 提供稳定访问入口的资源。

因为 Pod 的名字和 IP 可能会变，所以一般不直接长期访问某个 Pod，而是通过 Service 转发。

当前项目的 Service 文件：

`k8s/service.yaml`

---

## 3. 这些概念之间的关系

### 运行关系

`Deployment -> Pod -> Container -> Spring Boot 应用`

解释：

- `Deployment` 管理 Pod
- `Pod` 里装着 Container
- `Container` 里跑着 Spring Boot

### 访问关系

`浏览器 -> kubectl port-forward -> Service -> Pod -> Container -> Spring Boot`

解释：

- 浏览器访问本机端口
- `kubectl port-forward` 把流量转发到集群里的 Service
- `Service` 再把流量转发给后端 Pod
- Pod 里的容器处理请求

---

## 4. 当前项目里的对应关系

- 镜像：`minimal-spring-service:0.0.1-SNAPSHOT`
- Deployment：`k8s/deployment.yaml`
- Service：`k8s/service.yaml`
- Pod：类似 `minimal-spring-service-6654d9f46b-k6fqz`
- 应用接口：`/api/hello`

---

## 5. Docker 和 Kubernetes 端口关系

这部分最容易混，单独拆开看。

### Docker 场景总图

```text
浏览器 / Postman
        |
        v
Mac 本机端口 8080
        |
        v
docker run -p 8080:8080
        |
        v
容器里的应用端口 8080
        |
        v
Spring Boot
```

这里：

- 左边第一个 `8080` 是宿主机端口
- 右边第二个 `8080` 是容器里应用监听的端口

### Kubernetes 场景总图

```text
浏览器 / Postman
        |
        v
kubectl port-forward 8080:80
        |
        v
Service.port = 80
        |
        v
Service.targetPort = 8080
        |
        v
Pod IP:8080
        |
        v
Container 里的 Spring Boot:8080
```

---

## 6. 逐个解释端口概念

### `docker run -p`

格式：

```text
宿主机端口:容器内应用端口
```

例如：

```bash
docker run -p 8080:8080 minimal-spring-service:0.0.1-SNAPSHOT
```

表示：

- 访问你电脑的 `localhost:8080`
- Docker 转发到容器里的 `8080`

如果写成：

```bash
docker run -p 9090:8080 minimal-spring-service:0.0.1-SNAPSHOT
```

那就是：

- 本机访问 `9090`
- 容器里的应用仍然监听 `8080`

### `EXPOSE 8080`

`Dockerfile` 里的：

```dockerfile
EXPOSE 8080
```

表示镜像声明容器里的应用通常监听 `8080`。它更像说明信息，不等于自动映射到宿主机。

真正做端口映射的是 `docker run -p`。

### `containerPort`

在当前项目 [`k8s/deployment.yaml`](/Users/sevenzhou/Code/微服务/k8s/deployment.yaml) 里：

```yaml
ports:
  - containerPort: 8080
```

它的含义是：

- 声明这个容器里的应用预期监听 `8080`

它不是端口映射命令，更像资源描述信息。

真正决定应用监听哪个端口的，还是应用本身，比如 Spring Boot 的：

```yaml
server.port
```

### Pod 和 `containerPort` 的关系

关键点：

- Pod 是 Kubernetes 的最小运行单元
- Pod 里可以装一个或多个容器
- 同一个 Pod 里的容器共享网络
- 也就是共享同一个 Pod IP

所以如果一个 Pod 里有多个容器：

- 容器 A 可以监听 `8080`
- 容器 B 可以监听 `9090`

它们都在同一个 Pod IP 下，只是端口不同。

因此，访问 `PodIP:8080`，本质上是在访问 Pod 里某个容器监听的 `8080`。

### `Service.port`

在当前项目 [`k8s/service.yaml`](/Users/sevenzhou/Code/微服务/k8s/service.yaml) 里：

```yaml
ports:
  - name: http
    port: 80
    targetPort: 8080
```

这里的 `port: 80` 表示：

- Service 自己对外提供 `80` 端口

### `Service.targetPort`

同样在 [`k8s/service.yaml`](/Users/sevenzhou/Code/微服务/k8s/service.yaml) 里：

```yaml
targetPort: 8080
```

它表示：

- Service 接到请求后，转发到后端 Pod 的 `8080`

也就是：

`Service:80 -> Pod:8080`

---

## 7. 当前项目里的端口统一视角

当前项目里，Spring Boot 默认监听：

[`src/main/resources/application.yml`](/Users/sevenzhou/Code/微服务/src/main/resources/application.yml)

```yaml
server:
  port: ${SERVER_PORT:8080}
```

所以默认情况下：

- 应用端口：`8080`
- `containerPort`：`8080`
- `Service.targetPort`：`8080`
- `Service.port`：`80`

如果再加上本地调试的端口转发：

```bash
kubectl port-forward service/minimal-spring-service 8080:80
```

整体链路就是：

```text
localhost:8080
  -> Service:80
  -> Pod:8080
  -> Container 里的 Spring Boot:8080
```

如果是纯 Docker 运行：

```text
localhost:8080
  -> docker -p 8080:8080
  -> Container 里的 Spring Boot:8080
```

---

## 8. 为什么 docker images 里有镜像，Pod 还可能起不来

因为：

- `docker images` 看到的是 Docker 这一侧的镜像视图
- Kubernetes 节点启动 Pod 时，用的是节点运行时的镜像视图

在 Docker Desktop + kind 场景里，这两者不一定完全等价。

所以可能出现：

- 本地 `docker build` 成功
- `docker images` 能看见镜像
- Pod 仍然报 `ErrImageNeverPull` 或 `ImagePullBackOff`

线上更稳定的做法通常是：

1. `docker build`
2. `docker push` 到镜像仓库
3. K8s 从镜像仓库拉取镜像

---

## 9. 常用命令

### 查看镜像

```bash
docker images
docker image inspect minimal-spring-service:0.0.1-SNAPSHOT
```

### 查看 K8s 资源

```bash
kubectl get pods
kubectl get deployments
kubectl get svc
kubectl get nodes
```

### 查看 Pod 详情和日志

```bash
kubectl describe pod <pod-name>
kubectl logs <pod-name>
kubectl logs -f <pod-name>
```

### 进入 Pod 容器

```bash
kubectl exec -it <pod-name> -- sh
```

### 重启 Deployment

```bash
kubectl rollout restart deployment/minimal-spring-service
```

### 删除 Pod 让它自动重建

```bash
kubectl delete pod <pod-name>
```

---

## 10. 安全收工

### 停止 port-forward

在执行 `kubectl port-forward` 的终端里按：

```bash
Ctrl + C
```

### 删除当前应用，但保留集群

```bash
kubectl delete -f k8s/
```

这里的 `k8s/` 是相对路径，前提是你当前目录在项目根目录。

### 完全退出 Docker Desktop

在 macOS 状态栏点击 Docker 图标，然后选择：

`Quit Docker Desktop`

效果：

- Docker 停止
- 本地 Kubernetes 停止
- 本地容器停止

---

## 11. 一句话总结

- `Image`：应用模板
- `Container`：镜像跑起来的实例
- `Cluster`：Kubernetes 集群
- `Node`：集群里的节点
- `Pod`：Kubernetes 最小运行单元
- `Deployment`：管理 Pod 的方式
- `Service`：稳定访问入口

当前项目的完整链路可以记成：

`代码 -> Docker Image -> Pod -> Service -> 浏览器访问`
