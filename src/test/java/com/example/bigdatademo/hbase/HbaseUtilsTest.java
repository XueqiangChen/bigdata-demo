package com.example.bigdatademo.hbase;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HbaseUtilsTest {

    private final static String NAMESPACE = "chenxueqiang";
    private final static String TABLE_NAME = "chenxueqiang:student";

    @Test
    public void testCreateTable() {
        // create namespace & table
        HbaseUtils.createNamespace(NAMESPACE);
        boolean result = HbaseUtils.createTable(TABLE_NAME, Arrays.asList("info", "score"));
        assertTrue(result);
    }

    @Test
    public void testGetTable() {
        List<String> tableNames = HbaseUtils.listTableNames();
        assertTrue(tableNames.contains(TABLE_NAME));
    }

    @Test
    public void testInsert() {
        HbaseUtils.putRow(TABLE_NAME, "Tom", "info", "student_id", "20210000000001");
        HbaseUtils.putRow(TABLE_NAME, "Tom", "info", "class", "1");
        HbaseUtils.putRow(TABLE_NAME, "Tom", "score", "understanding", "75");
        HbaseUtils.putRow(TABLE_NAME, "Tom", "score", "programming", "82");

        HbaseUtils.putRow(TABLE_NAME, "Jerry", "info", "student_id", "20210000000002");
        HbaseUtils.putRow(TABLE_NAME, "Jerry", "info", "class", "1");
        HbaseUtils.putRow(TABLE_NAME, "Jerry", "score", "understanding", "85");
        HbaseUtils.putRow(TABLE_NAME, "Jerry", "score", "programming", "67");

        HbaseUtils.putRow(TABLE_NAME, "Jack", "info", "student_id", "20210000000003");
        HbaseUtils.putRow(TABLE_NAME, "Jack", "info", "class", "2");
        HbaseUtils.putRow(TABLE_NAME, "Jack", "score", "understanding", "80");
        HbaseUtils.putRow(TABLE_NAME, "Jack", "score", "programming", "80");

        HbaseUtils.putRow(TABLE_NAME, "Rose", "info", "student_id", "20210000000004");
        HbaseUtils.putRow(TABLE_NAME, "Rose", "info", "class", "2");
        HbaseUtils.putRow(TABLE_NAME, "Rose", "score", "understanding", "60");
        HbaseUtils.putRow(TABLE_NAME, "Rose", "score", "programming", "61");

        HbaseUtils.putRow(TABLE_NAME, "chenxueqiang", "info", "student_id", "G20210735010515");
        HbaseUtils.putRow(TABLE_NAME, "chenxueqiang", "info", "class", "3");
        HbaseUtils.putRow(TABLE_NAME, "chenxueqiang", "score", "understanding", "100");
        HbaseUtils.putRow(TABLE_NAME, "chenxueqiang", "score", "programming", "100");
    }

    @Test
    public void testScanTable() throws IOException {
        ResultScanner scanner = HbaseUtils.scanTable(TABLE_NAME);
        for (Result result = scanner.next(); result != null; result = scanner.next()) {
            System.out.println(result);
        }
    }

    @Test
    public void testGetRow() {
        Result result = HbaseUtils.getRow(TABLE_NAME, "Tom");
        assertEquals("20210000000001", Bytes.toString(result.getValue(Bytes.toBytes("info"), Bytes.toBytes("student_id"))));
        assertEquals("1", Bytes.toString(result.getValue(Bytes.toBytes("info"), Bytes.toBytes("class"))));

        result = HbaseUtils.getRow(TABLE_NAME, "chenxueqiang");
        assertEquals("G20210735010515", Bytes.toString(result.getValue(Bytes.toBytes("info"), Bytes.toBytes("student_id"))));
        assertEquals("100", Bytes.toString(result.getValue(Bytes.toBytes("score"), Bytes.toBytes("programming"))));
    }

    @Test
    public void testDeleteRow() {
        boolean result = HbaseUtils.deleteRow(TABLE_NAME, "Tom");
        assertTrue(result);

        Result res = HbaseUtils.getRow(TABLE_NAME,"Tom");
        assertNull(res.getRow());
    }

    @Test
    public void testDeleteColumn() {
        boolean result = HbaseUtils.deleteColumn(TABLE_NAME, "Jerry", "info", "class");
        assertTrue(result);

        Result res = HbaseUtils.getRow(TABLE_NAME, "Jerry");
        String value = Bytes.toString(res.getValue(Bytes.toBytes("info"), Bytes.toBytes("class")));
        assertNull(value);
    }

    @Test
    public void testDeleteTable() {
        boolean result = HbaseUtils.deleteTable(TABLE_NAME);
        assertTrue(result);

        List<String> tableNames = HbaseUtils.listTableNames();
        assertFalse(tableNames.contains(TABLE_NAME));
    }
}