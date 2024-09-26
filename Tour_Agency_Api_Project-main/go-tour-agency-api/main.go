package main

import (
    "context"
    "crypto/sha256"
    "encoding/hex"
    "encoding/json"
    "fmt"
    "io/ioutil"
    "log"
    "net/http"
    "sync"
    "time"
    "strings"

    "github.com/dgrijalva/jwt-go"
    "github.com/gin-gonic/gin"
    "github.com/yourbasic/bloom"
    "go.mongodb.org/mongo-driver/bson"
    "go.mongodb.org/mongo-driver/mongo"
    "go.mongodb.org/mongo-driver/mongo/options"
    "gopkg.in/yaml.v2"
    "os"
)

const (
    mongoURI           = "mongodb+srv://mehmet34:mehmet175e@atlascluster.j3z8vqq.mongodb.net"
    dbName             = "mydatabase"
    userCollection     = "users2"
    bloomFilterSize    = 100000
    bloomFilterHashCount = 5
    yamlFilePath       = "servers.yaml"
)

var (
    mongoClient *mongo.Client
    bloomFilter *bloom.Filter
    ctx         = context.TODO()
    serverConfig ServerConfig
    jwtKey      = []byte("my_secret_key") // JWT secret key
)

type Claims struct {
    Username string `json:"username"`
    jwt.StandardClaims
}

type Agency struct {
    Name       string `yaml:"name"`
    URL        string `yaml:"url"`
    DistanceKM int    `yaml:"distance_km"`
}

type ServerConfig struct {
    Agencies []Agency `yaml:"servers"`
}

func init() {
    var err error

    // Connect to MongoDB
    mongoClient, err = mongo.Connect(ctx, options.Client().ApplyURI(mongoURI))
    if err != nil {
        log.Fatalf("MongoDB connection error: %v", err)
    }

    // Start bloomFilter
    bloomFilter = bloom.New(bloomFilterSize, bloomFilterHashCount)

    // Read YAML file
    file, err := os.Open(yamlFilePath)
    if err != nil {
        log.Fatalf("YAML file opening error: %v", err)
    }
    defer file.Close()

    decoder := yaml.NewDecoder(file)
    err = decoder.Decode(&serverConfig)
    if err != nil {
        log.Fatalf("YAML file reading error: %v", err)
    }
}

func authenticateUser(username, password string) bool {

      if bloomFilter.Test(username) {
          userCollection := mongoClient.Database(dbName).Collection(userCollection)
          hashedPassword := hashPassword(password)
          var result bson.M
          err := userCollection.FindOne(ctx, bson.M{"username": username, "password": hashedPassword}).Decode(&result)
          if err != nil {
              return false
          }
          return true
      }
      return false

}

func hashPassword(password string) string {
    hasher := sha256.New()
    hasher.Write([]byte(password))
    return hex.EncodeToString(hasher.Sum(nil))
}

func generateJWT(username string) (string, error) {
    expirationTime := time.Now().Add(24 * time.Hour)

    claims := &Claims{
        Username: username,
        StandardClaims: jwt.StandardClaims{
            ExpiresAt: expirationTime.Unix(),
        },
    }

    token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
    tokenString, err := token.SignedString(jwtKey)
    if err != nil {
        return "", err
    }

    return tokenString, nil
}

func fetchJSON(url string, delayMS int) (interface{}, error) {
    // Log latency
    log.Printf("Simulated delay time: %d ms\n", delayMS)

    time.Sleep(time.Duration(delayMS) * time.Millisecond)

    resp, err := http.Get(url)
    if err != nil {
        return nil, fmt.Errorf("HTTP request error: %v", err)
    }
    defer resp.Body.Close()

    body, err := ioutil.ReadAll(resp.Body)
    if err != nil {
        return nil, fmt.Errorf("Response reading error: %v", err)
    }
    log.Printf("Response: %s\n", body)

    var result interface{}
    if err := json.Unmarshal(body, &result); err != nil {
        return nil, fmt.Errorf("JSON parse error: %v", err)
    }
    return result, nil
}

func main() {
    r := gin.Default()

     r.POST("/register", func(c *gin.Context) {
         var user struct {
             Username string `json:"username"`
             Password string `json:"password"`
         }

         if err := c.BindJSON(&user); err != nil {
             c.JSON(http.StatusBadRequest, gin.H{"error": "invalid data"})
             return
         }

         collection := mongoClient.Database(dbName).Collection(userCollection)


         hashedPassword := hashPassword(user.Password)

         _, err := collection.InsertOne(ctx, bson.M{
             "username": user.Username,
             "password": hashedPassword,
         })

         if err != nil {
             c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to register user"})
             return
         }
          bloomFilter.Add(user.Username)
         c.JSON(http.StatusCreated, gin.H{"message": "Registration Successful"})

     })


    r.POST("/login", func(c *gin.Context) {
      var user struct {
                Username string `json:"username"`
                Password string `json:"password"`
            }

            if err := c.BindJSON(&user); err != nil {
                c.JSON(http.StatusBadRequest, gin.H{"error": "invalid data"})
                return
            }


                if !bloomFilter.Test(user.Username) {
                    c.JSON(http.StatusUnauthorized, gin.H{"error": "User not found"})
                    return
                }

            if !authenticateUser(user.Username, user.Password) {
                c.JSON(http.StatusUnauthorized, gin.H{"error": "Invalid username or password"})
                return
            }

            token, err := generateJWT(user.Username)
            if err != nil {
                c.JSON(http.StatusInternalServerError, gin.H{"error": "Token could not be created"})
                return
            }

            c.JSON(http.StatusOK, gin.H{"token": token})

    })

    r.GET("/fetch", func(c *gin.Context) {
        token := c.GetHeader("Authorization")
        if token == "" {
            c.JSON(http.StatusUnauthorized, gin.H{"error": "Token missing"})
            return
        }
         token = strings.TrimPrefix(token, "Bearer ")
        claims := &Claims{}
        parsedToken, err := jwt.ParseWithClaims(token, claims, func(token *jwt.Token) (interface{}, error) {
            return jwtKey, nil
        })

        if err != nil || !parsedToken.Valid {
            c.JSON(http.StatusUnauthorized, gin.H{"error": "Invalid Token"})
            return
        }

        url := c.Query("url")

        if url == "" {
            c.JSON(http.StatusBadRequest, gin.H{"error": "URL parameter required"})
            return
        }

        var selectedAgency *Agency
        for _, agency := range serverConfig.Agencies {
            if agency.URL == url {
                selectedAgency = &agency
                break
            }
        }

        if selectedAgency == nil {
            c.JSON(http.StatusNotFound, gin.H{"error": "Agency not found"})
            return
        }

        delayMS := selectedAgency.DistanceKM / 10
        jsonData, err := fetchJSON(url+"/rooms", delayMS)
        if err != nil {
            c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
            return
        }

        c.JSON(http.StatusOK, gin.H{
            "agency":      selectedAgency.Name,
            "distance_km": selectedAgency.DistanceKM,
            "data":        jsonData,
        })
    })

    r.GET("/fetch-all", func(c *gin.Context) {
        token := c.GetHeader("Authorization")
        if token == "" {
            c.JSON(http.StatusUnauthorized, gin.H{"error": "Token missing"})
            return
        }

        token = strings.TrimPrefix(token, "Bearer ")

        claims := &Claims{}
        parsedToken, err := jwt.ParseWithClaims(token, claims, func(token *jwt.Token) (interface{}, error) {
            return jwtKey, nil
        })

        if err != nil || !parsedToken.Valid {
            c.JSON(http.StatusUnauthorized, gin.H{"error": "invalid token"})
            return
        }

        var wg sync.WaitGroup
        results := make(chan map[string]interface{}, len(serverConfig.Agencies))

        for _, agency := range serverConfig.Agencies {
            wg.Add(1)
            delayMS := agency.DistanceKM / 10
            go func(agency Agency) {
                defer wg.Done()
                jsonData, err := fetchJSON(agency.URL+"/rooms", delayMS)
                if err != nil {
                    log.Printf("Error with agency %s: %v", agency.Name, err)
                    return
                }
                results <- map[string]interface{}{
                    "agency":      agency.Name,
                    "distance_km": agency.DistanceKM,
                    "data":        jsonData,
                }
            }(agency)
        }

        go func() {
            wg.Wait()
            close(results)
        }()

        var allResults []map[string]interface{}
        for result := range results {
            allResults = append(allResults, result)
        }

        c.JSON(http.StatusOK, allResults)
    })

    r.Run(":8080")
}
