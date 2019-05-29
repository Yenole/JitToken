package com.yenole.blockchain.wallet.transaction.eos.chain;

import com.yenole.blockchain.foundation.utils.NumericUtil;
import com.yenole.blockchain.wallet.transaction.eos.types.EosByteWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Created by swapnibble on 2017-09-12.
 */

public class PackedTransaction {
    public enum CompressType {none, zlib}

    final List<String> signatures;

    final String compression;

    private String packed_context_free_data;

    private String packed_trx;

    public PackedTransaction(SignedTransaction stxn, CompressType compressType) {
        compression = compressType.name();
        signatures = stxn.getSignatures();

        packed_trx = NumericUtil.bytesToHex(packTransaction(stxn, compressType));

        byte[] packed_ctx_free_bytes = packContextFreeData(stxn.getCtxFreeData(), compressType);
        packed_context_free_data = (packed_ctx_free_bytes.length == 0) ? "" : NumericUtil.bytesToHex(packed_ctx_free_bytes);
    }

    private byte[] packTransaction(Transaction transaction, CompressType compressType) {
        EosByteWriter byteWriter = new EosByteWriter(512);
        transaction.pack(byteWriter);

        // pack -> compress
        return compress(byteWriter.toBytes(), compressType);
    }


    private byte[] packContextFreeData(List<String> ctxFreeData, CompressType compressType) {
        EosByteWriter byteWriter = new EosByteWriter(64);

        int ctxFreeDataCount = (ctxFreeData == null) ? 0 : ctxFreeData.size();
        if (ctxFreeDataCount == 0) {
            return byteWriter.toBytes();
        }

        byteWriter.putVariableUInt(ctxFreeDataCount);

        for (String hexData : ctxFreeData) {
            byteWriter.putBytes(NumericUtil.hexToBytes(hexData));
        }

        return compress(byteWriter.toBytes(), compressType);
    }


    public PackedTransaction(SignedTransaction stxn) {
        this(stxn, CompressType.none);
    }


    private byte[] compress(byte[] uncompressedBytes, CompressType compressType) {
        if (compressType == null || !CompressType.zlib.equals(compressType)) {
            return uncompressedBytes;
        }

        // zip!
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        deflater.setInput(uncompressedBytes);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(uncompressedBytes.length);
        deflater.finish();
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer); // returns the generated code... index
            outputStream.write(buffer, 0, count);
        }

        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return uncompressedBytes;
        }

        return outputStream.toByteArray();
    }

    private byte[] decompress(byte[] compressedBytes) {
        Inflater inflater = new Inflater();
        inflater.setInput(compressedBytes);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(compressedBytes.length);
        byte[] buffer = new byte[1024];

        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            outputStream.close();
        } catch (DataFormatException | IOException e) {
            e.printStackTrace();
            return compressedBytes;
        }
        return outputStream.toByteArray();
    }

    public String toJSON() {
        return String.format("{\"compression\":\"%s\",\"packed_context_free_data\":\"%s\",\"packed_trx\":\"%s\",\"signatures\":[\"%s\"]}", compression, packed_context_free_data, packed_trx, signatures.get(0));
    }
}

