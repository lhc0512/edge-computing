package cn.edu.scut.bean;

public enum StoreConstants {

    Byte(8), Kilo(1024), Mega(1024 * 1024), Giga(1024 * 1024 * 1024);

    public Integer value;

    StoreConstants(Integer value) {
        this.value = value;
    }
}
