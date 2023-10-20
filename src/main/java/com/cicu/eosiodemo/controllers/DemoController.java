package com.cicu.eosiodemo.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import io.antelope.javasdk.models.rpcProvider.response.SendTransactionResponse;
import io.antelope.javasdk.session.TransactionProcessor;
import io.antelope.javasdk.session.TransactionSession;
import io.antelope.antelopejavaabieosserializationprovider.AbiEosSerializationProviderImpl;
import io.antelope.javarpcprovider.implementations.EosioJavaRpcProviderImpl;
import io.antelope.softkeysignatureprovider.SoftKeySignatureProviderImpl;
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

        SendTransactionResponse sendTransactionResponse = processor.signAndBroadcast();
        ArrayList<Object> actionReturnValues = sendTransactionResponse.getActionValues();


        return "Transaction Completed";
    }
}
