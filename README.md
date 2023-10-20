# Demo of connecting to an eosio blockchain with block.one eosio libraries

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
