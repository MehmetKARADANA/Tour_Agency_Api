# Temel imaj olarak OpenJDK 17'yi kullanıyoruz
FROM openjdk:17-jdk-slim

# Uygulamanın çalışacağı dizini belirliyoruz
WORKDIR /app

# Maven build ile oluşturulan JAR dosyasını container içine kopyalıyoruz
COPY target/java-api-0.0.1-SNAPSHOT.jar /app/application.jar

# Uygulamanın çalıştırılacağı komutu belirtiyoruz
ENTRYPOINT ["java", "-jar", "application.jar"]

# Spring Boot'un çalışacağı portu açıyoruz
EXPOSE 8081
