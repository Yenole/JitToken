package com.yenole.blockchain.wallet.transaction.eos.chain;

import com.yenole.blockchain.foundation.utils.NumericUtil;
import com.yenole.blockchain.wallet.transaction.eos.types.EosType;
import com.yenole.blockchain.wallet.transaction.eos.types.TypeAccountName;
import com.yenole.blockchain.wallet.transaction.eos.types.TypeActionName;
import com.yenole.blockchain.wallet.transaction.eos.types.TypePermissionLevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Action implements EosType.Packer {
    private TypeAccountName account;

    private TypeActionName name;

    private List<TypePermissionLevel> authorization = null;

    private String data;

    public Action(String account, String name, TypePermissionLevel authorization, String data) {
        this.account = new TypeAccountName(account);
        this.name = new TypeActionName(name);
        this.authorization = new ArrayList<>();
        if (null != authorization) {
            this.authorization.add(authorization);
        }

        if (null != data) {
            this.data = data;
        }
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Action(String account, String name) {
        this(account, name, null, null);
    }

    public Action() {
        this(null, null, null, null);
    }

    public String getAccount() {
        return account.toString();
    }

    public void setAccount(String account) {
        this.account = new TypeAccountName(account);
    }

    public String getName() {
        return name.toString();
    }

    public void setName(String name) {
        this.name = new TypeActionName(name);
    }

    public List<TypePermissionLevel> getAuthorization() {
        return authorization;
    }

    public void setAuthorization(List<TypePermissionLevel> authorization) {
        this.authorization = authorization;
    }

    public void setAuthorization(TypePermissionLevel[] authorization) {
        this.authorization.addAll(Arrays.asList(authorization));
    }

    public void setAuthorization(String accountWithPermLevel) {
        if (null == accountWithPermLevel) {
            return;
        }
        String[] split = accountWithPermLevel.split("@", 2);
        authorization.add(new TypePermissionLevel(split[0], split[1]));
    }


    @Override
    public void pack(EosType.Writer writer) {
        account.pack(writer);
        name.pack(writer);

        writer.putCollection(authorization);

        if (null != data) {
            byte[] dataAsBytes = NumericUtil.hexToBytes(data);
            writer.putVariableUInt(dataAsBytes.length);
            writer.putBytes(dataAsBytes);
        } else {
            writer.putVariableUInt(0);
        }
    }
}