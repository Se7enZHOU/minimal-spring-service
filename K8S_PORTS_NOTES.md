# Docker 和 Kubernetes 端口关系笔记

这份笔记专门整理当前项目里最容易混淆的几个端口概念：

- `docker run -p`
- `containerPort`
- `Service.port`
- `Service.targetPort`

---

## 1. 一张总图

### Docker 场景

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

---

### Kubernetes 场景

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

## 2. 逐个解释

### `docker run -p 8080:8080`

格式是：

```text
宿主机端口:容器内应用端口
```

例如：

```bash
docker run -p 8080:8080 minimal-spring-service:0.0.1-SNAPSHOT
```

意思是：

- 访问你电脑的 `localhost:8080`
- Docker 转发到容器里的 `8080`

如果写成：

```bash
docker run -p 9090:8080 minimal-spring-service:0.0.1-SNAPSHOT
```

那就是：

- 本机访问 `9090`
- 容器里应用仍然监听 `8080`

---

### `containerPort`

在当前项目 [`k8s/deployment.yaml`](/Users/sevenzhou/Code/微服务/k8s/deployment.yaml) 里：

```yaml
ports:
  - containerPort: 8080
```

它的含义是：

- 声明这个容器里的应用预期监听 `8080`

它不是端口映射命令，更像是资源描述信息。

真正决定应用监听哪个端口的，还是应用本身，比如 Spring Boot 的：

```yaml
server.port
```

---

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

---

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

也就是说，集群里其他服务访问这个 Service 时，会访问它的 `80`。

---

### `Service.targetPort`

同样在 [`k8s/service.yaml`](/Users/sevenzhou/Code/微服务/k8s/service.yaml) 里：

```yaml
targetPort: 8080
```

它表示：

- Service 接到请求后，转发到后端 Pod 的 `8080`

也就是：

```text
Service:80 -> Pod:8080
```

---

## 3. 这几个端口在当前项目里的对应关系

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

那整体链路就是：

```text
localhost:8080
  -> Service:80
  -> Pod:8080
  -> Container 里的 Spring Boot:8080
```

---

## 4. 最常见的误区

### 误区 1：`containerPort` 会自动把端口暴露出来

不会。

`containerPort` 只是声明这个容器通常监听哪个端口，不会自动让集群外面访问到它。

真正让别人访问到它，还要靠：

- `Service`
- `Ingress`
- `kubectl port-forward`

---

### 误区 2：Pod 自己“监听端口”

更准确地说：

- 真正监听端口的是容器里的应用进程
- Pod 提供的是共享网络环境

因为同一个 Pod 内的容器共享网络，所以大家经常会口头说“Pod 的 8080 端口”。

---

### 误区 3：`Service.port` 和 `targetPort` 必须一样

不必须。

例如：

```yaml
port: 80
targetPort: 8080
```

这非常常见，表示：

- Service 对外暴露 `80`
- 实际转发到后端应用的 `8080`

---

### 误区 4：Docker 的 `-p` 和 Kubernetes 的 `Service` 是一回事

不是一回事，但功能上都和“把流量转给应用”有关。

- `docker run -p`
  是宿主机到容器的端口映射

- `Service`
  是 Kubernetes 集群内部对 Pod 的稳定访问入口

---

## 5. 一句话记忆

- `docker -p`
  宿主机端口映射到容器端口

- `containerPort`
  声明容器里的应用通常监听哪个端口

- `Service.port`
  Service 对外提供的端口

- `Service.targetPort`
  Service 转发到 Pod / 容器应用的端口

---

## 6. 当前项目的统一视角

可以把当前项目的端口链路直接记成：

```text
浏览器
  -> localhost:8080
  -> kubectl port-forward 8080:80
  -> Service.port 80
  -> Service.targetPort 8080
  -> Pod IP:8080
  -> Container 里的 Spring Boot:8080
```

如果是纯 Docker 运行：

```text
浏览器
  -> localhost:8080
  -> docker -p 8080:8080
  -> Container 里的 Spring Boot:8080
```
