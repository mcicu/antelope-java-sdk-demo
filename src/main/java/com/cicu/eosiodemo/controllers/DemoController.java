package com.cicu.eosiodemo.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import one.block.eosiojava.implementations.ABIProviderImpl;
import one.block.eosiojava.interfaces.IABIProvider;
import one.block.eosiojava.interfaces.IRPCProvider;
import one.block.eosiojava.interfaces.ISerializationProvider;
import one.block.eosiojava.models.rpcProvider.Action;
import one.block.eosiojava.models.rpcProvider.Authorization;
import one.block.eosiojava.models.rpcProvider.TransactionConfig;
import one.block.eosiojava.models.rpcProvider.request.GetRawAbiRequest;
import one.block.eosiojava.models.rpcProvider.response.GetInfoResponse;
import one.block.eosiojava.models.rpcProvider.response.GetRawAbiResponse;
import one.block.eosiojava.models.rpcProvider.response.SendTransactionResponse;
import one.block.eosiojava.session.TransactionProcessor;
import one.block.eosiojava.session.TransactionSession;
import one.block.eosiojavaabieosserializationprovider.AbiEosSerializationProviderImpl;
import one.block.eosiojavarpcprovider.implementations.EosioJavaRpcProviderImpl;
import one.block.eosiosoftkeysignatureprovider.SoftKeySignatureProviderImpl;
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
