# Hướng dẫn Build Project ServerTap

## Yêu cầu

- **Java JDK 17** (project sử dụng Java 17)
- **Maven 3.6+** (để build project)

## Kiểm tra môi trường

### Kiểm tra Java

```bash
java -version
# Cần Java 17 hoặc cao hơn
```

### Kiểm tra Maven

```bash
mvn -version
# Cần Maven 3.6 hoặc cao hơn
```

## Cài đặt Maven (nếu chưa có)

### Trên Arch Linux

```bash
sudo pacman -S maven
```

### Trên Ubuntu/Debian

```bash
sudo apt update
sudo apt install maven
```

### Trên macOS (với Homebrew)

```bash
brew install maven
```

### Cài đặt thủ công

1. Tải Maven từ: https://maven.apache.org/download.cgi
2. Giải nén và thêm vào PATH

## Build Project

### 1. Build JAR file (khuyến nghị)

```bash
cd /home/demonducky/zProject/minecraft/catmine/servertap/servertap-folia
mvn clean package
```

File JAR sẽ được tạo tại: `target/ServerTap-0.6.2-SNAPSHOT.jar`

### 2. Build và skip tests (nếu không muốn chạy tests)

```bash
mvn clean package -DskipTests
```

### 3. Chỉ compile (không tạo JAR)

```bash
mvn clean compile
```

### 4. Build với verbose output (để debug)

```bash
mvn clean package -X
```

## Các lệnh Maven hữu ích khác

### Clean build directory

```bash
mvn clean
```

### Install vào local Maven repository

```bash
mvn clean install
```

### Chạy tests

```bash
mvn test
```

### Xem dependency tree

```bash
mvn dependency:tree
```

## Kết quả Build

Sau khi build thành công, file JAR sẽ nằm trong thư mục `target/`:

- `target/ServerTap-0.6.2-SNAPSHOT.jar` - Plugin JAR file (đã được shade với dependencies)

## Sử dụng Plugin

1. Copy file JAR từ `target/` vào thư mục `plugins/` của server Minecraft
2. Khởi động server
3. Plugin sẽ tự động tạo file config tại `plugins/ServerTap/config.yml`

## Troubleshooting

### Lỗi: "mvn: command not found"

- Cài đặt Maven hoặc thêm Maven vào PATH

### Lỗi: "JAVA_HOME is not set"

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk  # Đường dẫn có thể khác
```

### Lỗi: "Unsupported class file major version"

- Đảm bảo đang sử dụng Java 17 hoặc cao hơn

### Lỗi compile

- Kiểm tra lại Java version: `java -version`
- Clean và build lại: `mvn clean package`

## Build với IDE

### IntelliJ IDEA

1. File → Open → Chọn thư mục project
2. Maven sẽ tự động sync
3. Build → Build Project (hoặc Ctrl+F9)

### Eclipse

1. File → Import → Maven → Existing Maven Projects
2. Chọn thư mục project
3. Right-click project → Run As → Maven build → `package`

### VS Code

1. Cài extension "Java Extension Pack"
2. Mở thư mục project
3. Terminal → Run Task → Maven: package
