package com.use.demo.data;


import java.io.Serializable;
import java.util.List;
@lombok.Data
public class DataSet implements Serializable {
    String url;
    String name;
    String category;
    int nums;
}
