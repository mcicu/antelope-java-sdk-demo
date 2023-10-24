# Demo of connecting to an antelope (eosio) blockchain with new antelope java sdk libraries

## Requirements:
 - Maven (recommended 3.6.3)
 - Java (recommended 11.0.6)

## Setup and run

Make sure to have Maven and Java installed and available when executing the next steps.

- `mvn clean compile`
  - this will compile the project locally
- `mvn spring-boot:run`
  - this will start the spring boot instance

## Test

The spring boot instance will be available at http://localhost:8080/eosio (root path).

To test the transaction example, call http://localhost:8080/eosio/demo/transaction, it should return `Transaction Completed`
if everything is working.

To the check on the blockchain, verify account https://explorer-test.telos.net/account/cicumihai222 and look on the last transaction (check timestamp also, there are multiple transactions there).

## Antelope libraries

New antelope libraries are being used:

```xml
<!-- antelope libraries -->
<dependency>
  <groupId>io.github.mcicu</groupId>
  <artifactId>antelope-java-sdk</artifactId>
  <version>0.0.1</version>
</dependency>

<dependency>
  <groupId>io.github.mcicu</groupId>
  <artifactId>antelope-java-softkey-signature-provider</artifactId>
  <version>0.0.1</version>
</dependency>

<dependency>
  <groupId>io.github.mcicu</groupId>
  <artifactId>antelope-java-rpc-provider</artifactId>
  <version>0.0.1</version>
</dependency>

<!-- make sure to use the proper abieos-serialization-provider library for your platform -->
<!-- currently available platforms: linux, darwin; (windows is not supported so far)-->
<dependency>
  <groupId>io.github.mcicu</groupId>
  <artifactId>antelope-java-abieos-serialization-provider-linux</artifactId>
  <version>0.0.1</version>
</dependency>

```
