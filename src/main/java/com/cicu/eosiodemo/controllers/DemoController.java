package com.cicu.eosiodemo.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.antelope.javarpcprovider.error.EosioJavaRpcProviderCallError;
import io.antelope.javasdk.error.rpcProvider.SendTransactionRpcError;
import io.antelope.javasdk.error.session.TransactionSendTransactionError;
import io.antelope.javasdk.error.session.TransactionSignAndBroadCastError;
import io.antelope.javasdk.implementations.ABIProviderImpl;
import io.antelope.javasdk.interfaces.IABIProvider;
import io.antelope.javasdk.interfaces.IRPCProvider;
import io.antelope.javasdk.interfaces.ISerializationProvider;
import io.antelope.javasdk.models.rpcProvider.Action;
import io.antelope.javasdk.models.rpcProvider.Authorization;
import io.antelope.javasdk.models.rpcProvider.TransactionConfig;
import io.antelope.javasdk.models.rpcProvider.request.GetRawAbiRequest;
import io.antelope.javasdk.models.rpcProvider.response.GetInfoResponse;
import io.antelope.javasdk.models.rpcProvider.response.GetRawAbiResponse;
import io.antelope.javasdk.models.rpcProvider.response.RPCResponseError;
import io.antelope.javasdk.models.rpcProvider.response.SendTransactionResponse;
import io.antelope.javasdk.session.TransactionProcessor;
import io.antelope.javasdk.session.TransactionSession;
import io.antelope.antelopejavaabieosserializationprovider.AbiEosSerializationProviderImpl;
import io.antelope.javarpcprovider.implementations.EosioJavaRpcProviderImpl;
import io.antelope.softkeysignatureprovider.SoftKeySignatureProviderImpl;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/demo")
public class DemoController {

    @Autowired
    ObjectMapper objectMapper;

    @GetMapping(path = "/info", produces = "application/json")
    public GetInfoResponse blockchainInfo() throws Exception {
        IRPCProvider rpcProvider = new EosioJavaRpcProviderImpl("https://telos.testnet.eosdublin.io");
        ISerializationProvider serializationProvider = new AbiEosSerializationProviderImpl();

        return rpcProvider.getInfo();
    }

    @GetMapping(path = "/abi/{accountName}", produces = "application/json")
    public GetRawAbiResponse getRawAbi(@PathVariable("accountName") String accountName) throws Exception {
        IRPCProvider rpcProvider = new EosioJavaRpcProviderImpl("https://telos.testnet.eosdublin.io");
        ISerializationProvider serializationProvider = new AbiEosSerializationProviderImpl();

        GetRawAbiRequest rawAbiRequest = new GetRawAbiRequest(accountName);
        return rpcProvider.getRawAbi(rawAbiRequest);
    }

    @GetMapping(path = "/transaction")
    public String testTransaction() throws Exception {

        // THIS CODE IS FOR DEMO PURPOSES, don't copy-paste into production

        IRPCProvider rpcProvider = new EosioJavaRpcProviderImpl("https://telos.testnet.eosdublin.io");
        ISerializationProvider serializationProvider = new AbiEosSerializationProviderImpl();
        IABIProvider abiProvider = new ABIProviderImpl(rpcProvider, serializationProvider);
        SoftKeySignatureProviderImpl signatureProvider = new SoftKeySignatureProviderImpl();

        String privateKey = "5JYKemgGEbA9CMgiZ7vuB36VcQrWGHfPyBcJbM2YQMqnrZwumU2";
        signatureProvider.importKey(privateKey);

        TransactionSession session = new TransactionSession(
                serializationProvider,
                rpcProvider,
                abiProvider,
                signatureProvider
        );

        TransactionProcessor processor = session.getTransactionProcessor();

        // Now the TransactionConfig can be altered, if desired
        TransactionConfig transactionConfig = processor.getTransactionConfig();

        // Use blocksBehind (default 3) the current head block to calculate TAPOS
        transactionConfig.setUseLastIrreversible(false);
        // Set the expiration time of transactions 600 seconds later than the timestamp
        // of the block used to calculate TAPOS
        transactionConfig.setExpiresSeconds(600);

        // Update the TransactionProcessor with the config changes
        processor.setTransactionConfig(transactionConfig);

        String jsonData = "{\n" +
                "\"from\": \"cicutestleap\",\n" +
                "\"to\": \"cicumihai222\",\n" +
                "\"quantity\": \"1.0000 TLOS\",\n" +
                "\"memo\" : \"Test transfer using java sdk\"\n" +
                "}";

        List<Authorization> authorizations = new ArrayList<>();
        authorizations.add(new Authorization("cicutestleap", "active"));
        List<Action> actions = new ArrayList<>();
        actions.add(new Action("eosio.token", "transfer", authorizations, jsonData));

        processor.prepare(actions);

        try {
            SendTransactionResponse sendTransactionResponse = processor.signAndBroadcast();
            ArrayList<Object> actionReturnValues = sendTransactionResponse.getActionValues();
        } catch (TransactionSignAndBroadCastError error) {
            //errors are wrapped at this point, we need to dig for specific causes (only for demo)
            if (error.getCause() instanceof TransactionSendTransactionError) {
                TransactionSendTransactionError sendTransactionError = (TransactionSendTransactionError) error.getCause();
                if (sendTransactionError.getCause() instanceof SendTransactionRpcError) {
                    SendTransactionRpcError sendTransactionRpcError = (SendTransactionRpcError) sendTransactionError.getCause();
                    if (sendTransactionRpcError.getCause() instanceof EosioJavaRpcProviderCallError) {
                        EosioJavaRpcProviderCallError eosioJavaRpcProviderCallError = (EosioJavaRpcProviderCallError) sendTransactionRpcError.getCause();
                        RPCResponseError rpcResponseError = eosioJavaRpcProviderCallError.getRpcResponseError();
                        return objectMapper.writeValueAsString(rpcResponseError);
                    }
                }
            }

            //root cause not solved above, just throw back the error (only for demo)
            throw error;
        }

        return "Transaction Completed";
    }

    @GetMapping(path = "/create-account")
    public String createAccount() throws Exception {

        // THIS CODE IS FOR DEMO PURPOSES, don't copy-paste into production

        IRPCProvider rpcProvider = new EosioJavaRpcProviderImpl("https://telos.testnet.eosdublin.io");
        ISerializationProvider serializationProvider = new AbiEosSerializationProviderImpl();
        IABIProvider abiProvider = new ABIProviderImpl(rpcProvider, serializationProvider);
        SoftKeySignatureProviderImpl signatureProvider = new SoftKeySignatureProviderImpl();

        String privateKey = "5JYKemgGEbA9CMgiZ7vuB36VcQrWGHfPyBcJbM2YQMqnrZwumU2";
        signatureProvider.importKey(privateKey);

        TransactionSession session = new TransactionSession(
                serializationProvider,
                rpcProvider,
                abiProvider,
                signatureProvider
        );

        TransactionProcessor processor = session.getTransactionProcessor();

        // Now the TransactionConfig can be altered, if desired
        TransactionConfig transactionConfig = processor.getTransactionConfig();

        // Use blocksBehind (default 3) the current head block to calculate TAPOS
        transactionConfig.setUseLastIrreversible(false);
        // Set the expiration time of transactions 600 seconds later than the timestamp
        // of the block used to calculate TAPOS
        transactionConfig.setExpiresSeconds(600);

        // Update the TransactionProcessor with the config changes
        processor.setTransactionConfig(transactionConfig);

        List<Authorization> authorizations = new ArrayList<>();
        authorizations.add(new Authorization("cicutestleap", "active"));


        String accountCreator = "cicutestleap"; //any account can create other accounts, if it has resources
        String newAccountName = "newaccntcic1";

        //CREATE EOSIO::NEWACCOUNT ACTION
        String jsonDataNewAccount = "{\n" +
                "  \"creator\": \"" + accountCreator + "\",\n" +
                "  \"name\": \"" + newAccountName + "\",\n" +
                "  \"owner\": {\n" +
                "    \"threshold\": 1,\n" +
                "    \"keys\": [\n" +
                "      {\n" +
                "        \"key\": \"EOS8HTG9k6LP6nqzGBix6CoWhH8M9nFoiyfu1Z8hsPMe1VbQnv5BA\",\n" +
                "        \"weight\": 1\n" +
                "      }\n" +
                "    ],\n" +
                "    \"accounts\": [],\n" +
                "    \"waits\": []\n" +
                "  },\n" +
                "  \"active\": {\n" +
                "    \"threshold\": 1,\n" +
                "    \"keys\": [\n" +
                "      {\n" +
                "        \"key\": \"EOS8HTG9k6LP6nqzGBix6CoWhH8M9nFoiyfu1Z8hsPMe1VbQnv5BA\",\n" +
                "        \"weight\": 1\n" +
                "      }\n" +
                "    ],\n" +
                "    \"accounts\": [],\n" +
                "    \"waits\": []\n" +
                "  }\n" +
                "}";


        Action newAccountAction = new Action("eosio", "newaccount", authorizations, jsonDataNewAccount);


        //CREATE EOSIO:BUYRAM ACTION for new account
        String jsonDataBuyRam = "{\"payer\":\"" + accountCreator + "\",\"receiver\":\"" + newAccountName + "\",\"quant\":\"0.2000 TLOS\"}";

        Action buyRamAction = new Action("eosio", "buyram", authorizations, jsonDataBuyRam);

        List<Action> actions = new ArrayList<>();
        actions.add(newAccountAction);
        actions.add(buyRamAction);
        processor.prepare(actions);

        try {
            SendTransactionResponse sendTransactionResponse = processor.signAndBroadcast();
            ArrayList<Object> actionReturnValues = sendTransactionResponse.getActionValues();
        } catch (TransactionSignAndBroadCastError error) {
            //errors are wrapped at this point, we need to dig for specific causes (only for demo)
            if (error.getCause() instanceof TransactionSendTransactionError) {
                TransactionSendTransactionError sendTransactionError = (TransactionSendTransactionError) error.getCause();
                if (sendTransactionError.getCause() instanceof SendTransactionRpcError) {
                    SendTransactionRpcError sendTransactionRpcError = (SendTransactionRpcError) sendTransactionError.getCause();
                    if (sendTransactionRpcError.getCause() instanceof EosioJavaRpcProviderCallError) {
                        EosioJavaRpcProviderCallError eosioJavaRpcProviderCallError = (EosioJavaRpcProviderCallError) sendTransactionRpcError.getCause();
                        RPCResponseError rpcResponseError = eosioJavaRpcProviderCallError.getRpcResponseError();
                        return objectMapper.writeValueAsString(rpcResponseError);
                    }
                }
            }

            //root cause not solved above, just throw back the error (only for demo)
            throw error;
        }

        return "Transaction Completed - Account created for " + newAccountName;
    }

    @GetMapping(path = "/stake-net-and-cpu")
    public String stakeNetAndCpu() throws Exception {

        // THIS CODE IS FOR DEMO PURPOSES, don't copy-paste into production

        IRPCProvider rpcProvider = new EosioJavaRpcProviderImpl("https://telos.testnet.eosdublin.io");
        ISerializationProvider serializationProvider = new AbiEosSerializationProviderImpl();
        IABIProvider abiProvider = new ABIProviderImpl(rpcProvider, serializationProvider);
        SoftKeySignatureProviderImpl signatureProvider = new SoftKeySignatureProviderImpl();

        String privateKey = "5JYKemgGEbA9CMgiZ7vuB36VcQrWGHfPyBcJbM2YQMqnrZwumU2";
        signatureProvider.importKey(privateKey);

        TransactionSession session = new TransactionSession(
                serializationProvider,
                rpcProvider,
                abiProvider,
                signatureProvider
        );

        TransactionProcessor processor = session.getTransactionProcessor();

        // Now the TransactionConfig can be altered, if desired
        TransactionConfig transactionConfig = processor.getTransactionConfig();

        // Use blocksBehind (default 3) the current head block to calculate TAPOS
        transactionConfig.setUseLastIrreversible(false);
        // Set the expiration time of transactions 600 seconds later than the timestamp
        // of the block used to calculate TAPOS
        transactionConfig.setExpiresSeconds(600);

        // Update the TransactionProcessor with the config changes
        processor.setTransactionConfig(transactionConfig);

        List<Authorization> authorizations = new ArrayList<>();
        authorizations.add(new Authorization("cicutestleap", "active"));

        //CREATE EOSIO::DELEGATEBW ACTION
        String jsonDataStakeResources = "{\n" +
                "                \"from\": \"cicutestleap\",\n" +
                "                \"receiver\": \"cicumihai222\",\n" +
                "                \"stake_net_quantity\": \"1.0000 TLOS\",\n" +
                "                \"stake_cpu_quantity\": \"1.0000 TLOS\",\n" +
                "                \"transfer\": true\n" +
                "            }";


        Action stakeResourcesAction = new Action("eosio", "delegatebw", authorizations, jsonDataStakeResources);

        List<Action> actions = new ArrayList<>();
        actions.add(stakeResourcesAction);
        processor.prepare(actions);

        try {
            SendTransactionResponse sendTransactionResponse = processor.signAndBroadcast();
            ArrayList<Object> actionReturnValues = sendTransactionResponse.getActionValues();
        } catch (TransactionSignAndBroadCastError error) {
            //errors are wrapped at this point, we need to dig for specific causes (only for demo)
            if (error.getCause() instanceof TransactionSendTransactionError) {
                TransactionSendTransactionError sendTransactionError = (TransactionSendTransactionError) error.getCause();
                if (sendTransactionError.getCause() instanceof SendTransactionRpcError) {
                    SendTransactionRpcError sendTransactionRpcError = (SendTransactionRpcError) sendTransactionError.getCause();
                    if (sendTransactionRpcError.getCause() instanceof EosioJavaRpcProviderCallError) {
                        EosioJavaRpcProviderCallError eosioJavaRpcProviderCallError = (EosioJavaRpcProviderCallError) sendTransactionRpcError.getCause();
                        RPCResponseError rpcResponseError = eosioJavaRpcProviderCallError.getRpcResponseError();
                        return objectMapper.writeValueAsString(rpcResponseError);
                    }
                }
            }

            //root cause not solved above, just throw back the error (only for demo)
            throw error;
        }

        return "Transaction Completed - Resources stacked";
    }
}
