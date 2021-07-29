package com.example.bigdatademo.hbase;

import javafx.util.Pair;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HbaseUtils {
    private static Connection connection;

    static {
        Configuration configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.property.clientPort", "2181");
        // 如果是集群 则主机名用逗号分隔
        configuration.set("hbase.zookeeper.quorum", "jikehadoop01,jikehadoop02,jikehadoop03");
        try {
            connection = ConnectionFactory.createConnection(configuration);
        } catch (IOException  e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建 Namespace
     * @param namespaceName namespace 名称
     */
    public static boolean createNamespace(String namespaceName) {
        try {
            HBaseAdmin admin = (HBaseAdmin) connection.getAdmin();
            admin.createNamespace(NamespaceDescriptor.create(namespaceName).build());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 创建 HBase 表
     *
     * @param tableName      表名
     * @param columnFamilies 列族的数组
     */
    public static boolean createTable(String tableName, List<String> columnFamilies) {
        try {
            HBaseAdmin admin = (HBaseAdmin) connection.getAdmin();
            if (admin.tableExists(TableName.valueOf(tableName))) {
                return false;
            }
            // 表描述器构造器
            TableDescriptorBuilder tableDescriptor = TableDescriptorBuilder.newBuilder(TableName.valueOf(tableName));
            columnFamilies.forEach(columnFamily -> {
                // 列族描述构造器
                ColumnFamilyDescriptorBuilder cfDescriptorBuilder = ColumnFamilyDescriptorBuilder.newBuilder(Bytes.toBytes(columnFamily));
                // cfDescriptorBuilder.setMaxVersions(1);
                ColumnFamilyDescriptor familyDescriptor = cfDescriptorBuilder.build();
                //添加列族
                tableDescriptor.setColumnFamily(familyDescriptor);
            });
            admin.createTable(tableDescriptor.build());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 获取所有的表名
     *
     * @return 表列表
     */
    public static List<String> listTableNames() {
        List<String> tableNames = new ArrayList<>();
        try {
            HBaseAdmin admin = (HBaseAdmin) connection.getAdmin();
            TableName[] namesRecord = admin.listTableNames();
            for (TableName tableName : namesRecord) {
                tableNames.add(tableName.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tableNames;
    }


    /**
     * 删除 hBase 表
     *
     * @param tableName 表名
     */
    public static boolean deleteTable(String tableName) {
        try {
            HBaseAdmin admin = (HBaseAdmin) connection.getAdmin();
            // 删除表前需要先禁用表
            admin.disableTable(TableName.valueOf(tableName));
            admin.deleteTable(TableName.valueOf(tableName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 插入数据
     *
     * @param tableName        表名
     * @param rowKey           唯一标识
     * @param columnFamilyName 列族名
     * @param qualifier        列标识
     * @param value            数据
     */
    public static boolean putRow(String tableName, String rowKey, String columnFamilyName, String qualifier,
                                 String value) {
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Put put = new Put(Bytes.toBytes(rowKey));
            put.addColumn(Bytes.toBytes(columnFamilyName), Bytes.toBytes(qualifier), Bytes.toBytes(value));
            table.put(put);
            table.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 检索全表
     *
     * @param tableName 表名
     */
    public static ResultScanner scanTable(String tableName) {
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Scan scan = new Scan();
            System.out.println("Scan for all rows: ");
            return table.getScanner(scan);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据 rowKey 获取指定行的数据
     *
     * @param tableName 表名
     * @param rowKey    唯一标识
     */
    public static Result getRow(String tableName, String rowKey) {
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Get get = new Get(Bytes.toBytes(rowKey));
            return table.get(get);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 获取指定行指定列 (cell) 的最新版本的数据
     *
     * @param tableName    表名
     * @param rowKey       唯一标识
     * @param columnFamily 列族
     * @param qualifier    列标识
     */
    public static String getCell(String tableName, String rowKey, String columnFamily, String qualifier) {
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Get get = new Get(Bytes.toBytes(rowKey));
            if (!get.isCheckExistenceOnly()) {
                get.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(qualifier));
                Result result = table.get(get);
                byte[] resultValue = result.getValue(Bytes.toBytes(columnFamily), Bytes.toBytes(qualifier));
                return Bytes.toString(resultValue);
            } else {
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 删除指定行记录
     *
     * @param tableName 表名
     * @param rowKey    唯一标识
     */
    public static boolean deleteRow(String tableName, String rowKey) {
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Delete delete = new Delete(Bytes.toBytes(rowKey));
            table.delete(delete);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }


    /**
     * 删除指定行指定列
     *
     * @param tableName  表名
     * @param rowKey     唯一标识
     * @param familyName 列族
     * @param qualifier  列标识
     */
    public static boolean deleteColumn(String tableName, String rowKey, String familyName,
                                       String qualifier) {
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Delete delete = new Delete(Bytes.toBytes(rowKey));
            delete.addColumn(Bytes.toBytes(familyName), Bytes.toBytes(qualifier));
            table.delete(delete);
            table.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
