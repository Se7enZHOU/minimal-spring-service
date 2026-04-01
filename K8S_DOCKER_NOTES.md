# Docker 和 Kubernetes 概念速查

这份笔记基于当前项目 `minimal-spring-service`，用于快速回顾本地开发、Docker 和 Kubernetes 里最容易混淆的概念。

## 1. Docker Desktop Dashboard 有什么

Docker Desktop 可以理解成你本机容器世界的控制台。

- `Containers / Apps`
  查看和管理正在运行的容器，可以启动、停止、删除、查看日志、进入终端。

- `Images`
  查看本地镜像。比如当前项目构建出的镜像：
  `minimal-spring-service:0.0.1-SNAPSHOT`

- `Volumes`
  查看数据卷。数据卷用于持久化数据，避免容器删除后数据丢失。

- `Builds`
  查看 `docker build` 的构建记录、每一步日志和缓存命中情况。

- `Docker Hub`
  用于登录镜像仓库，以及管理推送和拉取镜像的账户状态。

- `Kubernetes`
  管理 Docker Desktop 集成的本地 Kubernetes，可以创建 cluster、查看 node 状态。

- `Settings`
  配置 CPU、内存、磁盘、代理、镜像源、`containerd image store`、Kubernetes 类型等。

- `Troubleshoot`
  用于重启 Docker Desktop、重置 Kubernetes、清理数据、导出诊断信息。

## 2. 核心概念

### Image

镜像是一个可运行模板，里面包含：

- 运行环境
- 应用文件
- 启动命令

例如当前项目的镜像：

`minimal-spring-service:0.0.1-SNAPSHOT`

镜像是静态的，不会自己运行。

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

## 4. 当前项目里的对应关系

- 镜像：`minimal-spring-service:0.0.1-SNAPSHOT`
- Deployment：`k8s/deployment.yaml`
- Service：`k8s/service.yaml`
- Pod：类似 `minimal-spring-service-6654d9f46b-k6fqz`
- 应用接口：`/api/hello`

## 5. Docker 端口和 K8s 端口

### Docker

示例命令：

```bash
docker run --rm -p 8080:8080 minimal-spring-service:0.0.1-SNAPSHOT
```

含义：

- 左边 `8080`：你 Mac 上的端口
- 右边 `8080`：容器内部应用监听的端口

### EXPOSE 8080

`Dockerfile` 里的：

```dockerfile
EXPOSE 8080
```

它表示镜像声明容器里的应用通常监听 `8080`，更像说明信息，不等于自动映射到宿主机。

真正做端口映射的是 `docker run -p`。

### Kubernetes

当前 Service 配置里：

```yaml
port: 80
targetPort: 8080
```

含义：

- `port: 80`：Service 提供的端口
- `targetPort: 8080`：转发到 Pod 内容器的端口

## 6. 为什么 docker images 里有镜像，Pod 还可能起不来

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

## 7. 常用命令

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

## 8. 安全收工

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

## 9. 一句话总结

- `Image`：应用模板
- `Container`：镜像跑起来的实例
- `Cluster`：Kubernetes 集群
- `Node`：集群里的节点
- `Pod`：Kubernetes 最小运行单元
- `Deployment`：管理 Pod 的方式
- `Service`：稳定访问入口

当前项目的完整链路可以记成：

`代码 -> Docker Image -> Pod -> Service -> 浏览器访问`
